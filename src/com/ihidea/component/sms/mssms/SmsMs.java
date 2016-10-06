package com.ihidea.component.sms.mssms;

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

/**
 * 美圣<br>
 * http://www.jsmsxx.com/<br>
 * http://www.mssms.cn/msm/index.html<br>
 * @author TYOTANN
 */
public class SmsMs implements ISms {

	private Log logger = LogFactory.getLog(SmsMs.class);

	private String userId;

	private String password;

	public SmsMs(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}

	@Override
	public void send(String[] mobileId, String msg) {

		Map<String, String> param = new HashMap<String, String>();
		param.put("username", userId);
		param.put("scode", password);

		param.put("mobile", StringUtils.join(mobileId, ","));
		// param.put("mobile", "18651983688,13775091602,13337892511,15295168095");
		param.put("tempid", "MB-2013102300");

		param.put("content", "@1@=" + msg);

		String result = HttpClientUtils.post("http://222.185.228.25:8000/msm/sdk/http/sendsmsutf8.jsp", param, "UTF-8");

		if (StringUtils.isBlank(result) || result.trim().indexOf("0#") != 0) {
			logger.error("短信发送失败，错误代码:" + result + ".号码:" + String.valueOf(param.get("mobile")));
			throw new ServiceException("短信发送失败，错误代码:" + result);
		} else {
			logger.info("短信发送成功，手机号码:" + String.valueOf(param.get("mobile")));
		}

	}

	public BigDecimal balance() {
		return null;
	}

	public List<SmsReportEntity> report() {
		return null;
	}
}
