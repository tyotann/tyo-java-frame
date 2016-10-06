package com.ihidea.component.task;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.core.support.SpringContextLoader;

/**
 * <pre>
 * 利用spring初始化quartz的job
 * </pre>
 * @author TYOTANN
 */
public class SpringTaskFactory implements JobFactory {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected Logger getLog() {
		return log;
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {

		JobDetail jobDetail = bundle.getJobDetail();
		Class<? extends Job> jobClass = jobDetail.getJobClass();
		try {
			return SpringContextLoader.getBean(jobClass);
		} catch (Exception e) {
			SchedulerException se = new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass().getName() + "'", e);
			throw se;
		}
	}

}
