package com.ihidea.component.task;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * 任务执行器
 * @author TYOTANN
 */
@SuppressWarnings("deprecation")
@Component
public class TaskExecuter implements StatefulJob, InterruptableJob {

	@Autowired
	private TaskDao taskDao;

	/**
	 * <pre>
	 * 基类入口
	 * </pre>
	 * @throws Exception
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {

		BigDecimal currTaskSeq = TaskEnginee.getTaskSeq(context);

		String taskId = TaskEnginee.getTaskId(context);

		// 1.判断当前系统SEQ是否<自身SEQ,如果小于就跳出
		if (taskDao.getTaskInfo(taskId).getSeq().compareTo(currTaskSeq) < 0) {
			return;
		}

		try {

			taskDao.addTaskLog(taskId, currTaskSeq, BigDecimal.ZERO, "自动任务开始启动");

			// 2.锁表taskSeq=当前taskSeq
			TaskEntity task = taskDao.lockAndUpdateTaskStatus(taskId, currTaskSeq, context.getNextFireTime());

			// 3.如果SEQ匹配且未执行,则执行用户定义逻辑
			if (task != null) {

				try {
					run(task);
				} finally {

					// 4.不管成功失败,改回任务状态,此时SEQ已经+1
					taskDao.changeTaskStatus(taskId, currTaskSeq.add(BigDecimal.ONE), "0");
				}
			} else {
				taskDao.addTaskLog(taskId, currTaskSeq, BigDecimal.ZERO, "自动任务已经被执行,跳过");
			}
		} catch (Exception e) {
			taskDao.addTaskLog(taskId, currTaskSeq, new BigDecimal(-1),
					"任务执行出现异常" + ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e) == null ? e : ExceptionUtils.getRootCause(e)));
		} finally {

			// 5.集群环境下:同步数据库中的SEQ与下次执行时间,此时的SEQ已经+1
			TaskEnginee.setTaskSeq(context, taskDao.getTaskInfo(taskId).getSeq());
		}

		taskDao.addTaskLog(taskId, currTaskSeq, BigDecimal.ZERO, "自动任务结束");
	}

	/**
	 * <pre>
	 * 0:直接调用JAVA
	 * 1:执行输入的SQL
	 * </pre>
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void run(TaskEntity task) throws Exception {

		try {
			taskDao.addTaskLog(String.valueOf(task.getTaskId()), task.getSeq(), BigDecimal.ZERO, "任务开始执行");

			// 0:调用JAVA
			if ("0".equals(String.valueOf(task.getTaskCfg().getCallType()))) {

				String[] classArray = task.getTaskCfg().getCall().split("\\.");

				if (classArray.length == 2) {
					ClassUtilsEx.invokeMethod(classArray[0], classArray[1],
							JSONUtilsEx.deserialize(StringUtils.defaultIfEmpty(task.getTaskCfg().getCallParam(), "{}"), Map.class));
				} else {
					throw new ServiceException("任务调用方法:" + task.getTaskCfg().getCall() + ",方法格式错误。格式需要为:类名.方法名");
				}
			} else {
			}

			taskDao.addTaskLog(String.valueOf(task.getTaskId()), task.getSeq(), BigDecimal.ZERO, "任务正常执行结束");

		} catch (Exception e) {
			taskDao.addTaskLog(String.valueOf(task.getTaskId()), task.getSeq(), new BigDecimal(-1),
					"任务执行出现异常" + ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e) == null ? e : ExceptionUtils.getRootCause(e)));
		}
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		Thread.currentThread().interrupt();
	}

}
