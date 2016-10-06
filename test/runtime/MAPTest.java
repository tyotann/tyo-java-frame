package runtime;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Encoder;

public class MAPTest {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		// byte[] BYTES_KEY = { -99, 118, 97, -105, -51, -17, 81, 14 };
		//
		// System.out.println(new String(BYTES_KEY, "ISO-8859-1"));

		// y/3S2ua7ebjLEMX7F74plw==
		// y/3S2ua7ebjLEMX7F74plw==
		bd_decrypt(31.820875,119.99211);
		
		bd_encrypt(31.814608872921184,119.98566603004919);
	}

	
	//地球坐标系 (WGS-84) 到火星坐标系 (GCJ-02) 的转换算法

	//算法代码如下，其中 bd_encrypt 将 GCJ-02 坐标转换成 BD-09 坐标， bd_decrypt 反之。
	
	
	private static double x_pi = (3.14159265358979324 * 3000.0 / 180.0);

	/**
	 * gg-->百度
	 * @param gg_lat
	 * @param gg_lon
	 */
	private static void bd_encrypt(double gg_lat, double gg_lon) {
		double x = gg_lon, y = gg_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);

		System.out.println("lon:" + (z * Math.cos(theta) + 0.0065));
		System.out.println("lat:" + (z * Math.sin(theta) + 0.006));
		// bd_lon = z * Math.cos(theta) + 0.0065;
		// bd_lat = z * Math.sin(theta) + 0.006;
	}

	/**
	 * 百度-->gg
	 * @param bd_lat
	 * @param bd_lon
	 */
	private static void bd_decrypt(double bd_lat, double bd_lon) {
		double x = bd_lon - 0.0065, y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		// gg_lon = z * Math.cos(theta);
		// gg_lat = z * Math.sin(theta);

		System.out.println("lon:" + (z * Math.cos(theta)));
		System.out.println("lat:" + (z * Math.sin(theta)));
	}
}
