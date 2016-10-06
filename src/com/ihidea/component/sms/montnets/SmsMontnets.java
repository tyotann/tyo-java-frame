package com.ihidea.component.sms.montnets;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.component.sms.ISms;
import com.ihidea.component.sms.SmsReportEntity;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.HttpClientUtils;

/**
 * 梦网
 * @author TYOTANN
 */
public class SmsMontnets implements ISms {

	private Log logger = LogFactory.getLog(SmsMontnets.class);

	private String url;

	private String userId;

	private String password;

	public SmsMontnets(String url, String userId, String password) {
		this.url = url;
		this.userId = userId;
		this.password = password;
	}

	@Override
	public void send(String[] mobileId, String msg) {

		Map<String, String> params = new HashMap<String, String>();

		params.put("userId", userId);
		params.put("password", password);

		params.put("pszMobis", StringUtils.join(mobileId, ","));

		String encodeMsg = msg;
		try {
			encodeMsg = URLEncoder.encode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		params.put("iMobiCount", String.valueOf(mobileId.length));
		params.put("pszSubPort", "*");

		String result = HttpClientUtils.get(url + "/MongateCsSpSendSmsNew?userId=" + userId + "&password=" + password + "&pszMobis="
				+ StringUtils.join(mobileId, ",") + "&pszMsg=" + encodeMsg + "&iMobiCount=" + mobileId.length + "&pszSubPort=*");

		String code = null;
		try {
			int sIdx = result.indexOf("<string xmlns=\"http://tempuri.org/\">") + "<string xmlns=\"http://tempuri.org/\">".length();
			code = result.substring(sIdx, result.lastIndexOf("</string>"));
		} catch (Exception e) {
			logger.error("解析报文出现异常:" + e.getMessage() + "报文内容:" + result, e);
		}

		if (code.length() < 10) {
			throw new ServiceException("短信发送失败，错误代码:" + code);
		}

	}

	public BigDecimal balance() {

		String result = HttpClientUtils.get(url + "/MongateQueryBalance?userId=" + userId + "&password=" + password);

		// HttpClientUtils.get(url + "/MWGate/wmgw.asmx/MongateCsGetStatusReportExEx?userId=" + userId + "&password=" + password);

		int sIdx = result.indexOf("<int xmlns=\"http://tempuri.org/\">") + "<int xmlns=\"http://tempuri.org/\">".length();
		String code = result.substring(sIdx, result.lastIndexOf("</int>"));

		return new BigDecimal(code);
	}

	public List<SmsReportEntity> report() {

		String result = HttpClientUtils.get(url + "/MongateGetDeliver?iReqType=2&userId=" + userId + "&password=" + password);

		String code = null;
		try {
			int sIdx = result.indexOf("xmlns=\"http://tempuri.org/\">") + "xmlns=\"http://tempuri.org/\">".length();
			code = result.substring(sIdx, result.lastIndexOf("</ArrayOfString>"));
		} catch (Exception e) {
			logger.error("解析报文出现异常:" + e.getMessage() + "报文内容:" + result, e);
		}

		List<SmsReportEntity> resultList = new ArrayList<SmsReportEntity>();

		String[] subInfoArray = code.split("</string>");

		if (subInfoArray.length > 0) {
			for (String subInfo : subInfoArray) {

				subInfo = subInfo.replace("<string>", "").replace("\r\n", "");

				if (StringUtils.isNotBlank(subInfo)) {

					String[] infoArray = subInfo.split(",");

					try {
						resultList.add(new SmsReportEntity(DateUtilsEx.formatToDate(infoArray[1], "yyyy-MM-dd HH:mm:ss"), infoArray[2],
								infoArray[3], infoArray[4], infoArray[7], "DELIVRD".equals(infoArray[8]) ? "" : infoArray[8]));
					} catch (ParseException e) {
					}
				}

			}
		}

		return resultList;
	}

	public static void main(String[] args) throws Exception {

		SmsMontnets smsMontnets = new SmsMontnets("http://ws.montnets.com:9003/MWGate/wmgw.asmx", "J01938", "226351");
		smsMontnets.report();

	}

}
