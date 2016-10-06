package com.ihidea.core.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureUtils {

	public static final String HMAC_SHA1 = "HmacSHA1";

	public static String SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static byte[] signature(String type, String baseString, String keyString) throws GeneralSecurityException,
			UnsupportedEncodingException {

		Mac mac = Mac.getInstance(type);
		SecretKeySpec secret = new SecretKeySpec(keyString.getBytes(), type);
		mac.init(secret);

		return mac.doFinal(baseString.getBytes("UTF-8"));
	}

	public static byte[] md5(String baseString) {
		return md5(baseString, "UTF-8");
	}

	public static byte[] md5(String baseString, String charset) {

		try {
			byte[] btInput = baseString.getBytes(charset);

			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");

			// 使用指定的字节更新摘要
			mdInst.update(btInput);

			return mdInst.digest();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(DigitalUtils.byte2hex(md5("byxz120203" + "2015020311111")).toLowerCase());
	}

}
