package com.ihidea.component.sms.chuanglan;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.component.sms.ISms;
import com.ihidea.component.sms.SmsReportEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.HttpClientUtils;

public class SmsChuanglan implements ISms {

	private Log logger = LogFactory.getLog(SmsChuanglan.class);

	private String url = "http://222.73.117.158/msg/HttpBatchSendSM";

	private String userId;

	private String password;

	public SmsChuanglan(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}

	@Override
	public void send(String[] mobileId, String msg) {

		Map<String, String> param = new HashMap<String, String>();
		param.put("account", userId);
		param.put("pswd", password);
		param.put("mobile", StringUtils.join(mobileId, ","));

		// 是否需要状态报告
		param.put("needstatus", "false");
		param.put("msg", msg);

		if (mobileId != null && mobileId.length > 0) {

			String result = HttpClientUtils.post(url, param, "UTF-8");

			if (!"0".equals(result.split(",")[1])) {
				throw new ServiceException("短信发送失败，错误代码:" + result.split(",")[1]);
			}
		}

	}

	public BigDecimal balance() {
		return null;
	}

	public List<SmsReportEntity> report() {
		return null;
	}

	public static void main(String[] args) throws Exception {

		SmsChuanglan smsMontnets = new SmsChuanglan("hantang", "Tch123456");
		smsMontnets.send(new String[] { "18651983688", "13775091602" }, "AAA:  test 退定回TD");

	}

}
