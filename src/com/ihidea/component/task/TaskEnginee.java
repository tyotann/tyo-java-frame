package com.ihidea.component.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.util.DateUtilsEx;

/**
 * <pre>
 * 任务调度服务
 * version 2.5
 * </pre>
 * 
 * @author TYOTANN
 */
@Component
public class TaskEnginee {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private TaskDao taskDao;

	@Autowired
	private TaskExecuter jobExecuter;

	private static Map<String, JobDetail> TASK_MAP = new HashMap<String, JobDetail>();

	public static final String TASK_ID = "taskId";

	public static final String TASK_SEQ = "seq";

	public static final String TASK_CRON = "taskCron";

	private Properties properties = null;

	private Properties getProperties() throws Exception {
		if (properties == null) {
			properties = new Properties();
			properties.load(TaskEnginee.class.getClassLoader().getResourceAsStream("com/ihidea/component/task/quartz.properties"));
		}
		return properties;
	}

	/**
	 * 调度启动
	 * 
	 * @throws Exception
	 */
	public void startScheduler() {

		if (!CoreConstants.componentTaskEnable) {
			logger.info("系统不开启调度任务,如需开启请设置application.properties中参数:component.task.enable");
		} else {

			try {
				logger.info("系统调度加载开始");

				// 创建调度者工厂
				SchedulerFactory sfc = new StdSchedulerFactory(getProperties());

				// 通过工厂创建一个调度者
				Scheduler scheduler = sfc.getScheduler();

				// 清空所有任务
				scheduler.clear();

				// 启动守护进程
				{
					JobDetail job = JobBuilder.newJob(DaemonTask.class).withIdentity("job_daemon", "daemon").build();
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity("triger_daemon", "daemon")
							.withSchedule(CronScheduleBuilder.cronSchedule("0/30 * * * * ?")).build();
					scheduler.scheduleJob(job, trigger);
					logger.info("系统调度-----守护进程开始执行(频率:30s/次)");
				}

				// TODO 设置task状态为等待

				// 启动自定义任务
				{
					List<TaskEntity> jobInfoList = taskDao.getAllActiveTask();

					if (!CollectionUtils.isEmpty(jobInfoList)) {
						for (TaskEntity jobInfo : jobInfoList) {
							start(jobInfo.getTaskId());
						}
					}
				}

				// 开始运行调度程序
				scheduler.start();

				logger.info("系统调度装载成功~");
			} catch (Exception e) {
				logger.error("系统调度装载失败", e);
			}
		}
	}

	/**
	 * 调度关闭
	 * 
	 * @throws Exception
	 */
	public void stopScheduler() {

		try {
			logger.info("系统调度框架卸载开始~");

			if (CoreConstants.componentTaskEnable) {
				// 创建调度者工厂
				SchedulerFactory sfc = new StdSchedulerFactory(getProperties());

				// 通过工厂创建一个调度者
				Scheduler scheduler = sfc.getScheduler();

				scheduler.shutdown();

				TASK_MAP.clear();
			}

			logger.info("系统调度框架卸载成功~");
		} catch (Exception e) {
			logger.error("系统调度框架卸载失败", e);
		}

	}

	/**
	 * <pre>
	 * 临时执行任务
	 * 临时任务不计入调度任务,不引起调度任务状态位变更
	 * 所以会出现临时任务执行时,调度任务也执行的情况,慎用
	 * </pre>
	 * 
	 * @param taskId
	 * @throws Exception
	 */
	public void execute(String taskId) throws Exception {

		TaskEntity task = taskDao.getTaskInfo(taskId);

		taskDao.addTaskLog(String.valueOf(task.getTaskId()), task.getSeq(), BigDecimal.ZERO, "手动任务开始启动,启动用户"
				+ SessionContext.getSessionInfo().getUserName() + "[" + SessionContext.getSessionInfo().getUserId() + "]");

		jobExecuter.run(task);

		taskDao.addTaskLog(String.valueOf(task.getTaskId()), task.getSeq(), BigDecimal.ZERO, "手动任务结束");
	}

	/**
	 * 启动任务
	 * 
	 * @param taskId
	 * @throws Exception
	 */
	public void start(String taskId) throws Exception {

		if (!TASK_MAP.containsKey(taskId)) {

			TaskEntity task = taskDao.getTaskInfo(taskId);

			String taskName = task.getTaskName();

			// 设置任务
			JobDetail job = JobBuilder.newJob(TaskExecuter.class).withIdentity("job_" + taskId, Scheduler.DEFAULT_GROUP).build();
			job.getJobDataMap().put(TaskEnginee.TASK_SEQ, task.getSeq());
			job.getJobDataMap().put(TaskEnginee.TASK_CRON, task.getTaskCfg().getCron());
			job.getJobDataMap().put(TaskEnginee.TASK_ID, taskId);

			// 本机任务下次开始时间=数据库下次任务开始时间-数据库当前时间+本机当前时间
			Date taskNextTime = new Date(task.getTaskNextTime().getTime() - DateUtilsEx.getSysDate().getTime() + (new Date()).getTime());
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("triger_" + taskId, Scheduler.DEFAULT_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(task.getTaskCfg().getCron())).startAt(taskNextTime).build();

			TASK_MAP.put(taskId, job);

			// 调度任务
			SchedulerFactory sfc = new StdSchedulerFactory(getProperties());
			Scheduler scheduler = sfc.getScheduler();
			Date startDate = scheduler.scheduleJob(job, trigger);

			// 更新任务下次执行时间
			taskDao.updateTaskNextTime(taskId, taskNextTime);

			logger.info("任务编号为:" + taskId + "的任务:" + taskName + ", 将于:"
					+ DateUtilsEx.formatToString(startDate, DateUtilsEx.DATE_FORMAT_SEC) + "开始执行!");
		}
	}

	/**
	 * 移除任务
	 * 
	 * @param jobId
	 * @throws Exception
	 */
	public void stop(String taskId) throws Exception {

		if (TASK_MAP.containsKey(taskId)) {

			SchedulerFactory sfc = new StdSchedulerFactory(getProperties());

			Scheduler scheduler = sfc.getScheduler();

			// 停止正在执行的任务
			scheduler.interrupt(TASK_MAP.get(taskId).getKey());

			// 关闭任务
			scheduler.deleteJob(TASK_MAP.get(taskId).getKey());

			// 移除任务
			TASK_MAP.remove(taskId);
		}
	}

	/**
	 * 重新设置任务
	 * 
	 * @param jobId
	 * @param taskCfg
	 */
	public void resetTask(String taskId) throws Exception {

		stop(taskId);

		start(taskId);
	}

	public Map<String, JobDetail> getTaskMap() {
		return TASK_MAP;
	}

	public static String getTaskId(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		return (String) jobDataMap.get(TaskEnginee.TASK_ID);
	}

	public static BigDecimal getTaskSeq(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		return (BigDecimal) jobDataMap.get(TaskEnginee.TASK_SEQ);
	}

	public static void setTaskSeq(JobExecutionContext context, BigDecimal taskSeq) {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		jobDataMap.put(TaskEnginee.TASK_SEQ, taskSeq);
	}

}
