package com.ihidea.component.jms.ons;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.ihidea.core.util.JSONUtilsEx;

public class ONSSender {

	private final static Logger logger = LoggerFactory.getLogger(ONSSender.class);

	private Producer producer = null;

	public ONSSender(String pid, String ak, String sk) {

		Properties properties = new Properties();
		properties.put(PropertyKeyConst.ProducerId, pid);
		properties.put(PropertyKeyConst.AccessKey, ak);
		properties.put(PropertyKeyConst.SecretKey, sk);

		producer = ONSFactory.createProducer(properties);
		producer.start();
	}

	/**
	 * 往iot发送数据
	 */
	public void send(final String topicId, final String tag, final String key, final Object params, Integer async) {

		try {
			String paramJSON = JSONUtilsEx.serialize(params);

			Message msg = new Message(topicId, tag, paramJSON.getBytes(Charset.forName("UTF-8")));

			if (StringUtils.isNotBlank(key)) {
				msg.setKey(key);
			}

			// 默认使用同步发送
			if (async == null || async == 0) {
				producer.send(msg);
			} else {
				producer.sendOneway(msg);
			}

			logger.debug("【ONS-send】发送topicId:{}，tag:{},消息体:{},消息编号:{}", new Object[] { topicId, tag, paramJSON, msg.getMsgID() });
		} catch (Exception e) {
			logger.error("【ONS-send】发送消息失败:{}", new Object[] { e.getMessage() }, e);
		}
	}

	// 默认使用同步发送
	public void send(final String topicId, final String tag, final String key, final Object params) {
		send(topicId, tag, key, params, 0);
	}

	public void send(final String topicId, final String tag, final Object params) {
		send(topicId, tag, null, params);
	}

	public static void main(String[] args) throws Exception {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("serviceName", "stubStuatus");
		params.put("stubId", "1");

		params.put("status", "00");

		new ONSSender("PID_dxp", "E3qlfwBOuWuMMpFw", "IDFgMxbBNx4G7aLSaDkQ1H0JKgSHvy ").send("starchargeData", "dxp", params);
	}

}
