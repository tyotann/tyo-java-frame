package com.ihidea.component.report.umeng;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Encoder;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.SignatureUtils;

public class UmengUtils {

	private static Log logger = LogFactory.getLog(UmengUtils.class);

	private static final String UMEN_APPKEY = "yTLriJykw7Y0keQeyA4oQ3vBB0ZGdlnISkbtze0z";

	private static final String UMEN_SECRET = "piCH6Rzamns79vOSBqsNlj7f4JPLyoZafKhX5hBZ";

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static UmengEntity queryEntity(String url, Map<String, String> params, boolean isGet) throws Exception {

		String resultJson = queryString(url, params, isGet);

		if (!resultJson.startsWith("[") && !resultJson.startsWith("{")) {
			throw new ServiceException("请求服务发生异常!" + resultJson);
		} else {
			logger.debug("请求结果:" + resultJson);
			return JSONUtilsEx.deserialize(resultJson, UmengEntity.class);
		}
	}

	public static String queryString(String url, Map<String, String> params, boolean isGet) throws Exception {

		String queryString = queryUrl(url, params);

		if (isGet) {
			return HttpClientUtils.get(queryString);
		} else {
			return HttpClientUtils.post(queryString);
		}
	}

	public static String queryUrl(String url, Map<String, String> params) throws Exception {

		params.put("timestamp", String.valueOf(new Date().getTime()));
		params.put("app_key", UMEN_APPKEY);

		String baseString = sortQueryString(params);

		String signature = URLEncoder.encode(
				new BASE64Encoder().encode(SignatureUtils.signature(SignatureUtils.HMAC_SHA1, baseString, UMEN_SECRET)), "UTF-8");

		return url + "?" + baseString + "&signature=" + signature;
	}

	private static String sortQueryString(Map<String, String> params) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);

			if (StringUtils.isNotBlank(key)) {

				key = key.trim();

				String value = params.get(key).trim();

				if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
					prestr = prestr + key + "=" + value;
				} else {
					prestr = prestr + key + "=" + value + "&";
				}
			}
		}

		return prestr;
	}

}
