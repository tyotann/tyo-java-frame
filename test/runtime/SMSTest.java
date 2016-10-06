package runtime;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.SignatureUtils;
import com.ihidea.core.util.StringUtilsEx;

public class SMSTest {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		
		String timestamp = String.valueOf(new Date().getTime()).substring(0, 10);

		Map<String, String> param = new HashMap<String, String>();
		param.put("account", "cf_czxc0519");
		param.put("password", "Quup4W");



		param.put("mobile", "18651983688");
		// param.put("tempid", "MB-2013102300");

		param.put("content", "您的验证码是：【1523】。请不要把验证码泄露给其他人。");

		String tmp = HttpClientUtils.post("http://106.ihuyi.cn/webservice/sms.php?method=Submit", param, "GB2312");

		System.out.println(tmp);
		
		

//		String timestamp = String.valueOf(new Date().getTime()).substring(0, 10);
//
//		Map<String, String> param = new HashMap<String, String>();
//		param.put("cpid", "10186");
//		param.put("password", StringUtilsEx.bytes2Hex(SignatureUtils.md5("xc_0519!" + "_" + timestamp + "_topsky")).toLowerCase());
//
//		param.put("timestamp", timestamp);
//
//		param.put("channelid", "13153");
//
//		param.put("tele", "18651983688,13775091602,13337892511,15295168095");
//		// param.put("tempid", "MB-2013102300");
//
//		param.put("msg", "您好，您的登录密码为：9521");
//
//		String tmp = HttpClientUtils.post("http://admin.sms9.net/houtai/sms.php", param, "GB2312");
//
//		System.out.println(tmp);

		// Map<String, String> param = new HashMap<String, String>();
		// param.put("username", "JSMB260632");
		// param.put("scode", "536272");
		//
		// param.put("mobile", "18651983688,13775091602,13337892511,15295168095");
		// param.put("tempid", "MB-2013102300");
		//
		// param.put("content", "@1@=92ws");
		//
		// String tmp = HttpClientUtils.post("http://mssms.cn:8000/msm/sdk/http/sendsmsutf8.jsp", param);
		//
		// System.out.println(tmp);
	}
}
