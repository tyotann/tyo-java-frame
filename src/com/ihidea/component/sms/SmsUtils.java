package com.ihidea.component.sms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ihidea.component.sms.chuanglan.SmsChuanglan;
import com.ihidea.component.sms.ihuyi.SmsIhuyi;
import com.ihidea.component.sms.montnets.SmsMontnets;
import com.ihidea.component.sms.mssms.SmsMs;

public class SmsUtils {

	private Log logger = LogFactory.getLog(SmsUtils.class);

	private ISms sms;

	private static ThreadPoolTaskExecutor smsTaskExecutor = null;

	static {

		smsTaskExecutor = new ThreadPoolTaskExecutor();

		// 线程池所使用的缓冲队列
		smsTaskExecutor.setQueueCapacity(30);

		// 线程池维护线程的最少数量
		smsTaskExecutor.setCorePoolSize(0);

		// 线程池维护线程的最大数量
		smsTaskExecutor.setMaxPoolSize(Integer.MAX_VALUE);

		// 线程池维护线程所允许的空闲时间
		smsTaskExecutor.setKeepAliveSeconds(60);

		smsTaskExecutor.initialize();
	}

	public SmsUtils(String type, String url, String userId, String password) {

		if (type.equals("montnets")) {
			sms = new SmsMontnets(url, userId, password);
		} else if (type.equals("mssms")) {
			sms = new SmsMs(userId, password);
		} else if (type.equals("ihuyi")) {
			sms = new SmsIhuyi(userId, password);
		} else if (type.equals("chuanglan")) {
			sms = new SmsChuanglan(userId, password);
		}
	}

	public void send(final String[] mobileId, final String msg) {

		try {
			smsTaskExecutor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						sms.send(mobileId, msg);
					} catch (Exception e) {
						logger.error("发送短信时出现异常:" + e.getMessage(), e);
					}
				}
			});

			logger.info("当前短信线程数量：" + smsTaskExecutor.getActiveCount());
		} catch (Exception e) {
			logger.error("短信线程出现异常:" + e.getMessage(), e);
		}
	}

	public List<SmsReportEntity> report() {

		try {
			return sms.report();
		} catch (Exception e) {
			logger.error("接受短信状态时出现异常:" + e.getMessage(), e);
			return new ArrayList<SmsReportEntity>();
		}
	}

	public BigDecimal balance() {

		try {
			return sms.balance();
		} catch (Exception e) {
			logger.error("出现异常:" + e.getMessage(), e);
			return null;
		}
	}

	public static void main(String[] args) throws Exception {

		SmsUtils smsMontnets = new SmsUtils("chuanglan", null, "hantang1", "Tch123456");

		smsMontnets.send(new String[] { "18651983688" }, "新需求通知，用户名：饿，手机号：18661124027");
		smsMontnets.send(new String[] { "18651983688" }, "新需求通知，用户名：饿，手机号：18661124027");
		smsMontnets.send(new String[] { "18651983688" }, "新需求通知，用户名：饿，手机号：18661124027");

	}
}
