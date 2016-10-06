package com.ihidea.component.pay.upmp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ihidea.component.pay.PayPropertySupport;

/**
 * 类名：交易服务类 功能：接口公用函数类 版本：1.0 日期：2012-10-11 作者：中国银联UPMP团队 版权：中国银联 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 */
public class UpmpCore {

	/** = */
	public static final String QSTRING_EQUAL = "=";

	/** & */
	public static final String QSTRING_SPLIT = "&";

	/**
	 * 除去请求要素中的空值和签名参数
	 * @param para 请求要素
	 * @return 去掉空值与签名参数后的请求要素
	 */
	public static Map<String, String> paraFilter(Map<String, String> para) {

		Map<String, String> result = new HashMap<String, String>();

		if (para == null || para.size() <= 0) {
			return result;
		}

		for (String key : para.keySet()) {
			String value = para.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase(PayPropertySupport.getProperty("pay.upmp.signature"))
					|| key.equalsIgnoreCase(PayPropertySupport.getProperty("pay.upmp.signMethod"))) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

	/**
	 * 生成签名
	 * @param req 需要签名的要素
	 * @return 签名结果字符串
	 */
	public static String buildSignature(Map<String, String> req) {
		String prestr = createLinkString(req, true, false);
		prestr = prestr + QSTRING_SPLIT + UpmpMd5Encrypt.md5(PayPropertySupport.getProperty("pay.upmp.security.key"));
		return UpmpMd5Encrypt.md5(prestr);
	}

	/**
	 * 把请求要素按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * @param para 请求要素
	 * @param sort 是否需要根据key值作升序排列
	 * @param encode 是否需要URL编码
	 * @return 拼接成的字符串
	 */
	public static String createLinkString(Map<String, String> para, boolean sort, boolean encode) {

		List<String> keys = new ArrayList<String>(para.keySet());

		if (sort)
			Collections.sort(keys);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = para.get(key);

			if (encode) {
				try {
					value = URLEncoder.encode(value, PayPropertySupport.getProperty("pay.upmp.charset"));
				} catch (UnsupportedEncodingException e) {
				}
			}

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				sb.append(key).append(QSTRING_EQUAL).append(value);
			} else {
				sb.append(key).append(QSTRING_EQUAL).append(value).append(QSTRING_SPLIT);
			}
		}
		return sb.toString();
	}

	/**
	 * 解析应答字符串，生成应答要素
	 * @param str 需要解析的字符串
	 * @return 解析的结果map
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> parseQString(String str) throws UnsupportedEncodingException {

		Map<String, String> map = new HashMap<String, String>();
		int len = str.length();
		StringBuilder temp = new StringBuilder();
		char curChar;
		String key = null;
		boolean isKey = true;

		for (int i = 0; i < len; i++) {// 遍历整个带解析的字符串
			curChar = str.charAt(i);// 取当前字符

			if (curChar == '&') {// 如果读取到&分割符
				putKeyValueToMap(temp, isKey, key, map);
				temp.setLength(0);
				isKey = true;
			} else {
				if (isKey) {// 如果当前生成的是key
					if (curChar == '=') {// 如果读取到=分隔符
						key = temp.toString();
						temp.setLength(0);
						isKey = false;
					} else {
						temp.append(curChar);
					}
				} else {// 如果当前生成的是value
					temp.append(curChar);
				}
			}
		}

		putKeyValueToMap(temp, isKey, key, map);

		return map;
	}

	private static void putKeyValueToMap(StringBuilder temp, boolean isKey, String key, Map<String, String> map)
			throws UnsupportedEncodingException {
		if (isKey) {
			key = temp.toString();
			if (key.length() == 0) {
				throw new RuntimeException("QString format illegal");
			}
			map.put(key, "");
		} else {
			if (key.length() == 0) {
				throw new RuntimeException("QString format illegal");
			}
			map.put(key, URLDecoder.decode(temp.toString(), PayPropertySupport.getProperty("pay.upmp.charset")));
		}
	}

}
