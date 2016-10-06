package com.ihidea.component.sms.ihuyi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ihidea.component.sms.ISms;
import com.ihidea.component.sms.SmsReportEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.HttpClientUtils;

public class SmsIhuyi implements ISms {

	private String userId;

	private String password;

	public SmsIhuyi(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}

	@Override
	public void send(String[] mobileId, String msg) {

		Map<String, String> param = new HashMap<String, String>();
		param.put("account", userId);
		param.put("password", password);

		param.put("content", msg);

		String errorCodes = "";

		for (String mobile : mobileId) {
			param.put("mobile", mobile);
			String result = HttpClientUtils.post("http://106.ihuyi.cn/webservice/sms.php?method=Submit", param, "GB2312");

			if (StringUtils.isNotBlank(result)) {

				int sIdx = result.indexOf("<code>") + "<code>".length();
				String code = result.substring(sIdx, result.lastIndexOf("</code>"));

				if (!"2".equals(code)) {
					errorCodes += code + ",";
				}
			}
		}

		if (StringUtils.isNotBlank(errorCodes)) {
			throw new ServiceException("短信发送失败，错误次数:" + (errorCodes.split(",").length - 1) + "错误代码:" + errorCodes);
		}

	}

	public BigDecimal balance() {
		return null;
	}

	@Override
	public List<SmsReportEntity> report() {
		// TODO Auto-generated method stub
		return null;
	}

}
