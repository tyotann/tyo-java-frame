package com.ihidea.component.pay.upmp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ihidea.component.pay.PayPropertySupport;

/**
 * 类名：MD5签名处理类 功能：MD5签名处理核心类 版本：1.0 日期：2012-10-11 作者：中国银联UPMP团队 版权：中国银联
 * 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 */
public class UpmpMd5Encrypt {

	/**
	 * get the md5 hash of a string
	 * 
	 * @param str
	 * @return
	 */
	public static String md5(String str) {

		if (str == null) {
			return null;
		}

		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance(PayPropertySupport.getProperty("pay.upmp.sign.type"));
			messageDigest.reset();
			messageDigest.update(str.getBytes(PayPropertySupport.getProperty("pay.upmp.charset")));
		} catch (NoSuchAlgorithmException e) {

			return str;
		} catch (UnsupportedEncodingException e) {
			return str;
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

}