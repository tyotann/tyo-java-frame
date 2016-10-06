package com.ihidea.core.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DES {
	private byte[] desKey;

	private static final byte[] iv1 = { 18, 52, 86, 120, -112, -85, -51, -17 };

	public DES() {
		String desKey = "taocz!@#";
		this.desKey = desKey.getBytes();
	}

	public byte[] desEncrypt(byte[] plainText) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(iv1);
		byte[] rawKeyData = this.desKey;
		DESKeySpec dks = new DESKeySpec(rawKeyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(1, key, iv);
		byte[] data = plainText;
		byte[] encryptedData = cipher.doFinal(data);
		return encryptedData;
	}

	public byte[] desDecrypt(byte[] encryptText) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(iv1);
		byte[] rawKeyData = this.desKey;
		DESKeySpec dks = new DESKeySpec(rawKeyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(2, key, iv);
		byte[] encryptedData = encryptText;
		byte[] decryptedData = cipher.doFinal(encryptedData);
		return decryptedData;
	}
}
