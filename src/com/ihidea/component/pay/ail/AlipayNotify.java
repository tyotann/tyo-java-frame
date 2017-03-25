package com.ihidea.component.pay.ail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.ihidea.component.pay.PayPropertySupport;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.util.SignatureUtils;

/* *
 *类名：AlipayNotify
 *功能：支付宝通知处理类
 *详细：处理支付宝各接口通知返回
 *版本：3.3
 *日期：2012-08-17
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考

 *************************注意*************************
 *调试通知返回时，可查看或改写log日志的写入TXT里的数据，来检查通知返回是否正常
 */
public class AlipayNotify {

	protected static Log logger = LogFactory.getLog(AlipayNotify.class);

	/**
	 * 支付宝消息验证地址
	 */
	private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";

	/**
	 * 验证消息是否是支付宝发出的合法消息
	 * @param params 通知返回来的参数数组
	 * @return 验证结果
	 */
	public static boolean verify(Map<String, String> params) {

		// 判断responsetTxt是否为true，isSign是否为true
		// responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
		// isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
		logger.info("是否走到这边");
		String responseTxt = "true";
		if (params.get("notify_id") != null) {
			String notify_id = params.get("notify_id");
			responseTxt = verifyResponse(notify_id, (String) LocalAttributeHolder.getContext().get("appid"));
		}

		logger.info("verify result:" + responseTxt);

		String sign = "";
		if (params.get("sign") != null) {
			sign = params.get("sign");
		}

		boolean isSign = getSignVeryfy(params, sign);

		// 写日志记录（若要调试，请取消下面两行注释）
		// String sWord = "responseTxt=" + responseTxt + "\n isSign=" + isSign +
		// "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
		// AlipayCore.logResult(sWord);
		System.out.println("responseTxt----" + responseTxt);
		System.out.println("isSign----" + isSign);
		if (isSign && responseTxt.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据反馈回来的信息，生成签名结果
	 * @param Params 通知返回来的参数数组
	 * @param sign 比对的签名结果
	 * @return 生成的签名结果
	 */
	private static boolean getSignVeryfy(Map<String, String> Params, String sign) {
		// 过滤空值、sign与sign_type参数
		Map<String, String> sParaNew = AlipayCore.paraFilter(Params);
		// 获取待签名字符串
		String preSignStr = SignatureUtils.createLinkString(sParaNew);
		// 获得签名验证结果

		// 通用快捷支付通用的支付宝公钥
		String pk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

		boolean isSign = false;

		if (Params.get("sign_type").equals("RSA")) {
			isSign = RSA.verify(preSignStr, sign, pk, "utf-8");
		} else {
			isSign = MD5.verify(preSignStr, sign, getAliDic((String) LocalAttributeHolder.getContext().get("appid"), "pay.ali.key"),
					"utf-8");
		}
		System.out.println("preSignStr-----" + preSignStr);
		System.out.println("isSign-----" + isSign);
		return isSign;
	}

	/**
	 * 获取远程服务器ATN结果,验证返回URL
	 * @param notify_id 通知校验ID
	 * @return 服务器ATN结果 验证结果集： invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 true 返回正确信息 false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
	 * @throws Exception
	 */
	public static String verifyResponse(String notify_id, String appid) {

		// 获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求
		String partner = getAliDic(appid, "pay.ali.partnerId");
		logger.info("pay.ali.partnerId的值-----" + partner);
		logger.info("notify_id的值-----" + notify_id);
		// String partner = PayPropertySupport.getProperty("pay.ali.pid");
		String veryfy_url = HTTPS_VERIFY_URL + "partner=" + partner + "&notify_id=" + notify_id;
		logger.info("veryfy_url的值-----" + veryfy_url);
		return checkUrl(veryfy_url);
	}

	private static String getAliDic(String appid, String dicName) {

		Map<String, String> dicMap = new HashMap<String, String>();
		try {
			//TODO
			throw new ServiceException("重新定义阿里字典");
//			DictionaryService dictionaryService = (DictionaryService) SpringContextLoader.getBean("DictionaryService");
//			dicMap = dictionaryService.getSystemDic(appid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dicMap.get(dicName);

	}

	/**
	 * 获取远程服务器ATN结果
	 * @param urlvalue 指定URL路径地址
	 * @return 服务器ATN结果 验证结果集： invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 true 返回正确信息 false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
	 */
	private static String checkUrl(String urlvalue) {
		String inputLine = "";

		try {
			URL url = new URL(urlvalue);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			inputLine = in.readLine().toString();
		} catch (Exception e) {
			e.printStackTrace();
			inputLine = "";
		}

		return inputLine;
	}

	// /WAP 专用
	/**
	 * 解密
	 * @param inputPara 要解密数据
	 * @return 解密后结果
	 */
	public static Map<String, String> decrypt(Map<String, String> inputPara) throws Exception {
		inputPara.put("notify_data",
				RSA.decrypt(inputPara.get("notify_data"), PayPropertySupport.getProperty("pay.ali.wap.privateKey"), "UTF-8"));
		return inputPara;
	}

	/**
	 * 验证消息是否是支付宝发出的合法消息，验证服务器异步通知
	 * @param params 通知返回来的参数数组
	 * @return 验证结果
	 */
	public static boolean verifyNotify(Map<String, String> params) throws Exception {

		// 获取是否是支付宝服务器发来的请求的验证结果
		String responseTxt = "true";
		try {
			// XML解析notify_data数据，获取notify_id
			Document document = DocumentHelper.parseText(params.get("notify_data"));
			String notify_id = document.selectSingleNode("//notify/notify_id").getText();
			responseTxt = verifyResponse(notify_id, CoreConstants.getProperty("application.appid"));
		} catch (Exception e) {
			responseTxt = e.toString();
		}

		// 获取返回时的签名验证结果
		String sign = "";
		if (params.get("sign") != null) {
			sign = params.get("sign");
		}
		boolean isSign = getWAPSignVeryfy(params, sign, false);

		// 写日志记录（若要调试，请取消下面两行注释）
		// String sWord = "responseTxt=" + responseTxt + "\n isSign=" + isSign + "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
		// AlipayCore.logResult(sWord);

		// 判断responsetTxt是否为true，isSign是否为true
		// responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
		// isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
		if (isSign && responseTxt.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 可以合并！当时要区分出私钥 --by tyotann<br>
	 * 根据反馈回来的信息，生成签名结果
	 * @param Params 通知返回来的参数数组
	 * @param sign 比对的签名结果
	 * @param isSort 是否排序
	 * @return 生成的签名结果
	 */
	public static boolean getWAPSignVeryfy(Map<String, String> Params, String sign, boolean isSort) {
		// 过滤空值、sign与sign_type参数
		Map<String, String> sParaNew = AlipayCore.paraFilter(Params);
		// 获取待签名字符串
		String preSignStr = "";
		if (isSort) {
			preSignStr = SignatureUtils.createLinkString(sParaNew);
		} else {
			preSignStr = AlipayCore.createLinkStringNoSort(sParaNew);
		}
		// 获得签名验证结果
		boolean isSign = false;
		if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("MD5")) {
			isSign = MD5.verify(preSignStr, sign, PayPropertySupport.getProperty("pay.ali.wap.key"), "UTF-8");
		}
		if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("0001")) {
			isSign = RSA.verify(preSignStr, sign, PayPropertySupport.getProperty("pay.ali.wap.aliPublicKey"), "UTF-8");
		}
		return isSign;
	}
}
