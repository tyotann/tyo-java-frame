package runtime;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Encoder;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.JSONUtilsEx;

public class MainTest {

	private static final String HMAC_SHA1 = "HmacSHA1";

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static final String UMEN_APPKEY = "yTLriJykw7Y0keQeyA4oQ3vBB0ZGdlnISkbtze0z";

	private static final String UMEN_SECRET = "piCH6Rzamns79vOSBqsNlj7f4JPLyoZafKhX5hBZ";

//	public static void test() {
//		System.currentTimeMillis();
//		System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
//	}

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

//		ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
//		
//		System.out.println(jse.eval("20.6*10-6")); 

//		System.out.println(System.currentTimeMillis());
//
//		String jsonStr = FileUtils.readFileToString(new File("E:/test.txt"));
//
//		System.out.println(System.currentTimeMillis());
//
//		List<Object> jsonList = JSONUtilsEx.deserialize(jsonStr, List.class);
//
//		System.out.println(System.currentTimeMillis());
		//
		// Retn.Msg_Retn.Builder returnA = Retn.Msg_Retn.newBuilder();
		//
		// MLog.Msg_D_Log.Builder returnBuilder = MLog.Msg_D_Log.newBuilder();
		// returnBuilder.setWith(0);
		//
		// returnA.setErrorCode(0);
		// returnA.setErrorMsg("");
		// returnA.setReturnMethod("MobileController");
		//
		// //
		// returnA.setRetnMessage(ByteString.copyFrom(returnBuilder.toByteArray()));
		//
		// FileUtils.writeByteArrayToFile(new File("d:/protobuf"), new
		// DES().desEncrypt(ProtobufUtils.serialize(returnA)));

		System.out.println("1");

	}

	/**
	 * 依赖包：org.apache.commons.lang
	 * 
	 * @param strEncrypt
	 * @param salt
	 * @return
	 */
	public static String md5(String strEncrypt, String salt) {

		strEncrypt = StringUtils.defaultIfEmpty(strEncrypt, StringUtils.EMPTY);

		salt = StringUtils.defaultIfEmpty(salt, StringUtils.EMPTY);

		strEncrypt = strEncrypt + salt;

		byte BYTES_KEY[] = { -99, 118, 97, -105, -51, -17, 81, 14 };

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
			throw new ServiceException(e.getMessage());
		}
		return strEncrypt;
	}

	private static byte[] computeSignature(String baseString, String keyString) throws GeneralSecurityException,
			UnsupportedEncodingException {

		String hmac = "";

		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec secret = new SecretKeySpec(keyString.getBytes(), "HmacSHA1");
		mac.init(secret);
		// byte[] digest = mac.doFinal(baseString.getBytes());
		// BigInteger hash = new BigInteger(1, digest);
		// hmac = hash.toString(16);
		//
		// if (hmac.length() % 2 != 0) {
		// hmac = "0" + hmac;
		// }

		return mac.doFinal(baseString.getBytes());
	}

	private static String hmacSHA512(String data, String key) {
		String result = "";
		byte[] bytesKey = key.getBytes();
		final SecretKeySpec secretKey = new SecretKeySpec(bytesKey, HMAC_SHA1);
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			mac.init(secretKey);
			final byte[] macData = mac.doFinal(data.getBytes());
			byte[] hex = new Hex().encode(macData);
			result = new String(hex, "UTF-8");
		} catch (NoSuchAlgorithmException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return result;
	}

	public static String getSignature(String data, String key) throws Exception {
		byte[] keyBytes = key.getBytes();
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : rawHmac) {
			sb.append(byteToHexString(b));
		}
		return sb.toString();
	}

	private static String byteToHexString(byte ib) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] ob = new char[2];
		ob[0] = Digit[(ib >>> 4) & 0X0f];
		ob[1] = Digit[ib & 0X0F];
		String s = new String(ob);
		return s;
	}
}
