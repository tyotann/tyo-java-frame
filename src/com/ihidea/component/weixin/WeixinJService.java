package com.ihidea.component.weixin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.SignatureUtils;

public class WeixinJService {

	private static Map<String, Object[]> signatureMap = new HashMap<String, Object[]>();

	/**
	 * 得到微信jsdk的签名
	 * @param appid
	 * @param appSecret
	 * @param timestamp
	 * @param nonceStr
	 * @param url
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getSignature(String appid, String appSecret, long timestamp, String nonceStr, String url) {

		Object[] _signature = signatureMap.get(appid + appSecret + timestamp + nonceStr + url);

		// 如果不存在或已经超时，重新取得签名（签名超时25min）
		if (_signature == null || _signature.length != 2 || ((Long) _signature[1]) + 1500000 < new Date().getTime()) {

			String accessTokenJSON = HttpClientUtils.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
					+ appid + "&secret=" + appSecret);

			String accessToken = (String) ((Map) JSONUtilsEx.deserialize(accessTokenJSON, Map.class)).get("access_token");

			String jsapiTicketJSON = HttpClientUtils.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken
					+ "&type=jsapi");

			String jsapiTicket = (String) ((Map) JSONUtilsEx.deserialize(jsapiTicketJSON, Map.class)).get("ticket");

			String signatureStr = "jsapi_ticket=" + jsapiTicket + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url=" + url;

			signatureMap.put(appid + appSecret, new Object[] { SignatureUtils.SHA1(signatureStr), new Date().getTime() });
		}

		return (String) signatureMap.get(appid + appSecret + timestamp + nonceStr + url)[0];
	}

	public static void main(String[] args) throws Exception {

		// long time = new Date().getTime() / 1000;
		//
		// System.out.println(time);

		System.out.println(WeixinJService.getSignature("wx6bd0d27344607052", "172ec2ffc885093e9a8cb3435bea5f32", 1422596711, "test",
				"http://testweixin.iappk.com/storm/wx.act.cny.do?jump=share"));
	}
}
