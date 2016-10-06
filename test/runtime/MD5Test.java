package runtime;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Encoder;

public class MD5Test {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

//		byte[] BYTES_KEY = { -99, 118, 97, -105, -51, -17, 81, 14 };
//
//		System.out.println(new String(BYTES_KEY, "ISO-8859-1"));

		// y/3S2ua7ebjLEMX7F74plw==
		// y/3S2ua7ebjLEMX7F74plw==
		System.out.println(md5("aa", "ss"));
	}

	/**
	 * 依赖包：org.apache.commons.lang
	 * @param strEncrypt
	 * @param salt
	 * @return
	 */
	public static String md5(String strEncrypt, String salt) {

		strEncrypt = StringUtils.defaultIfEmpty(strEncrypt, StringUtils.EMPTY);

		salt = StringUtils.defaultIfEmpty(salt, StringUtils.EMPTY);

		strEncrypt = strEncrypt + salt;

		byte[] BYTES_KEY = { -99, 118, 97, -105, -51, -17, 81, 14 };

		// byte BYTES_KEY[] = { -99, 118, 97, -105, -51, -17, 81, 14 };

		try {
			byte[] b = strEncrypt.getBytes("UTF8");

			// 构建DES密钥
			SecretKey key = new SecretKeySpec(BYTES_KEY, "DES");
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
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
			strEncrypt = StringUtils.EMPTY;
		}
		return strEncrypt;
	}
}
