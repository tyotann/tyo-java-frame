package com.ihidea.component.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ihidea.core.support.ClusterSupportService;
import com.ihidea.core.util.SystemUtilsEx;

/**
 * 任务守护进程
 * @author TYOTANN
 */
@SuppressWarnings("deprecation")
@Component
public class DaemonTask implements StatefulJob {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private TaskEnginee taskEnginee;

	@Autowired
	private ClusterSupportService clusterSupportService;

	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context) throws JobExecutionException {

		// 集群心跳
		{
			Map<String, Object> hostInfo = clusterSupportService.getHostInfo();

			if (hostInfo == null) {
				hostInfo = new HashMap<String, Object>();
				hostInfo.put("hostId", SystemUtilsEx.getHostId());
				hostInfo.put("ip", SystemUtilsEx.getHostIp());
				hostInfo.put("hostName", SystemUtilsEx.getHostName());
			}

			hostInfo.put("updateDate", new Date());

			clusterSupportService.addClusterNodes(hostInfo);
		}

		// 维护集群状态,移除超时10min的节点
		{
			List<Map<String, Object>> clusterList = clusterSupportService.getClusterNodes();

			for (Iterator<Map<String, Object>> i = clusterList.iterator(); i.hasNext();) {
				Map<String, Object> node = i.next();

				if (node.get("updateDate") != null && (new Date()).getTime() - ((Date) node.get("updateDate")).getTime() > 600000) {
					clusterSupportService.removeClusterNodes(SystemUtilsEx.getHostId());
				}
			}
		}

		// 移除停止的任务,启动开始的任务
		{
			Map<String, JobDetail> taskMap = taskEnginee.getTaskMap();

			// 移除停止的任务
			for (TaskEntity unActiveTask : taskDao.getAllUnActiveTask()) {

				if (taskMap.containsKey(unActiveTask.getTaskId())) {
					try {
						taskDao.addTaskLog(unActiveTask.getTaskId(), unActiveTask.getSeq(), BigDecimal.ZERO, "任务被停止!");
						taskEnginee.stop(unActiveTask.getTaskId());
					} catch (Exception e) {
						logger.error("停止任务时出现异常", e);
					}
				}
			}

			// 排查任务cron是否被修改
			for (TaskEntity activeTask : taskDao.getAllActiveTask()) {

				if (taskMap.containsKey(activeTask.getTaskId())) {

					String oldCron = String.valueOf(taskMap.get(activeTask.getTaskId()).getJobDataMap().get(TaskEnginee.TASK_CRON));

					String newCron = activeTask.getTaskCfg().getCron();

					if (!oldCron.equals(newCron)) {
						try {
							taskDao.addTaskLog(activeTask.getTaskId(), activeTask.getSeq(), BigDecimal.ZERO, "任务触发时间由" + "[" + oldCron
									+ "]变更为[" + newCron + "]" + ",任务重启!");
							taskEnginee.resetTask(activeTask.getTaskId());
						} catch (Exception e) {
							logger.error("发现任务CRON变更,重启任务时出现异常", e);
						}
					}
				}
			}

			// 启动开始的任务
			for (TaskEntity activeTask : taskDao.getAllActiveTask()) {

				if (!taskMap.containsKey(activeTask.getTaskId())) {
					try {
						taskDao.addTaskLog(activeTask.getTaskId(), activeTask.getSeq(), BigDecimal.ZERO, "任务被启动!");
						taskEnginee.start(activeTask.getTaskId());
					} catch (Exception e) {
						logger.error("启动任务时出现异常", e);
					}
				}
			}
		}

		// 任务过期重置
		{
			List<TaskEntity> taskList = taskDao.getTimeOutTask();

			if (!CollectionUtils.isEmpty(taskList)) {
				for (final TaskEntity task : taskList) {

					// 如果任务是自身或者已经被移除
					if (task.getHostId().equals(SystemUtilsEx.getHostId())
							|| CollectionUtils.countMatches(clusterSupportService.getClusterNodes(), new Predicate() {

								public boolean evaluate(Object arg0) {

									boolean result = false;

									if (task.getHostId().equals(((Map<String, Object>) arg0).get("hostId"))) {
										result = true;
									}
									return result;
								}
							}) == 0) {
						taskDao.addTaskLog(task.getTaskId(), task.getSeq(), new BigDecimal(-1), "任务超时,终止任务");

						// 终止本机任务,重新启动
						try {
							taskEnginee.resetTask(task.getTaskId());
						} catch (Exception e) {
							taskDao.addTaskLog(task.getTaskId(), task.getSeq(), new BigDecimal(-1), "任务终止时出现异常");
						}

						taskDao.changeTaskStatus(task.getTaskId(), task.getSeq(), "0");
					}
				}
			}
		}

	}
}
