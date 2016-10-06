package com.ihidea.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.ihidea.core.support.exception.ServiceException;

public class StringUtilsEx {

	/**
	 * MD5加密
	 * 
	 * @param strEncrypt
	 *            需要加密字符串
	 * @param salt
	 *            加密混淆字符串
	 * @return
	 */
	public static String md5(String strEncrypt, String salt) {

		SystemUtilsEx.initJCE();

		strEncrypt = StringUtils.defaultIfEmpty(strEncrypt, StringUtils.EMPTY);

		salt = StringUtils.defaultIfEmpty(salt, StringUtils.EMPTY);

		strEncrypt = strEncrypt + salt;

		byte BYTES_KEY[] = { -99, 118, 97, -105, -51, -17, 81, 14 };

		try {
			byte[] b = strEncrypt.getBytes("UTF8");

			// 构建DES密钥
			SecretKey key = new SecretKeySpec(BYTES_KEY, "DES");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding", SystemUtilsEx.getJCEName());
			cipher.init(1, key);

			// DES加密
			b = cipher.doFinal(b);
			BASE64Encoder encoder = new BASE64Encoder();

			// base64编码成String
			strEncrypt = encoder.encode(b);
			MessageDigest md = MessageDigest.getInstance("MD5");

			// MD5编码
			md.update(strEncrypt.getBytes("UTF8"));

			// base64编码成String
			strEncrypt = encoder.encode(md.digest());
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return strEncrypt;
	}

	public static final byte[] base642byte(String instr) {
		try {
			return new BASE64Decoder().decodeBuffer(instr);
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public static final String byte2base64(byte[] bytes) {
		try {
			return new BASE64Encoder().encode(bytes);
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * 得到UUID
	 * 
	 * @return
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 过滤特殊字符,防止SQL注入
	 */
	public static String filterXML(String inStr) {
		String filtered = inStr;
		if (filtered != null) {
			filtered = filtered.replaceAll("'", "\"").replaceAll("<", "&lt;").replace(">", "&gt;");
		}
		return filtered;
	}

	/**
	 * 过滤特殊字符,防止SQL注入
	 */
	public static String filterSQL(String inStr) {
		String filtered = inStr;
		if (filtered != null) {
			filtered = filtered.replaceAll("'", "\"");
		}
		return filtered;
	}

	/**
	 * 检查字符串str是否为double类型
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isDouble(String str) {
		if (!StringUtils.isNotEmpty(str))
			return false;
		try {
			Double.parseDouble(str.trim());
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 16进制字符串转换成字节数组
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hex2Byte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	/**
	 * 字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String bytes2Hex(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 按字节截取
	 * 
	 * @param str
	 * @param len
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String substringb(String str, int len, String charsetName) throws UnsupportedEncodingException {
		String result = null;
		if (str != null) {
			byte[] a = str.getBytes(charsetName);
			if (a.length <= len) {
				result = str;
			} else if (len > 0) {
				result = new String(a, 0, len, charsetName);
				int length = result.length();
				if (str.charAt(length - 1) != result.charAt(length - 1)) {
					if (length < 2) {
						result = null;
					} else {
						result = result.substring(0, length - 1);
					}
				}
			}
		}
		return result;
	}

	public static String substringb(String str, int len) throws UnsupportedEncodingException {
		return StringUtilsEx.substringb(str, len, "UTF-8");
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	// 使用StringEscapeUtils的escapeHtml,escapeJavaScript会有中文乱码问题
	// 但是spring得没有unescapeJavaScript方法，所以unescapeJavaScript使用apache的包
	public static String escapeXss(String str) {
		return JavaScriptUtils.javaScriptEscape(HtmlUtils.htmlEscape(str));
	}

	public static String unescapeXss(String str) {
		return StringEscapeUtils.unescapeJavaScript(StringEscapeUtils.unescapeHtml(str));
	}

	public static void main(String[] args) throws Exception {

		System.out.println(escapeXss("<script>alert({'name':'草鸡蛋'});</script>"));

		System.out.println(unescapeXss(JavaScriptUtils.javaScriptEscape(HtmlUtils.htmlEscape("<script>alert({'name':'草鸡蛋'});</script>"))));

	}

}
