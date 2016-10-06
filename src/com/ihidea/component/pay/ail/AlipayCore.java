package com.ihidea.component.pay.ail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.ihidea.component.pay.PayPropertySupport;
import com.ihidea.core.util.HttpClientUtils;

/* *
 *类名：AlipayFunction
 *功能：支付宝接口公用函数类
 *详细：该类是请求、通知返回两个文件所调用的公用函数核心处理文件，不需要修改
 *版本：3.3
 *日期：2012-08-14
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayCore {

	/**
	 * 除去数组中的空值和签名参数
	 * @param sArray 签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {

		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0) {
			return result;
		}

		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("sign_type")
					|| key.equalsIgnoreCase("appid")) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

	/**
	 * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * @param params 需要排序并参与字符拼接的参数组
	 * @return 拼接后字符串
	 */
	public static String createLinkString(Map<String, String> params) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr;
	}

	/**
	 * 把数组所有元素按照固定参数排序，以“参数=参数值”的模式用“&”字符拼接成字符串
	 * @param params 需要参与字符拼接的参数组
	 * @return 拼接后字符串
	 */
	public static String createLinkStringNoSort(Map<String, String> params) {

		// 手机网站支付MD5签名固定参数排序，顺序参照文档说明
		StringBuilder gotoSign_params = new StringBuilder();
		gotoSign_params.append("service=" + params.get("service"));
		gotoSign_params.append("&v=" + params.get("v"));
		gotoSign_params.append("&sec_id=" + params.get("sec_id"));
		gotoSign_params.append("&notify_data=" + params.get("notify_data"));

		return gotoSign_params.toString();
	}

	// ///////////////////////////////WAP专用

	/**
	 * 建立请求，以表单HTML形式构造，带文件上传功能
	 * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
	 * @param sParaTemp 请求参数数组
	 * @param strMethod 提交方式。两个值可选：post、get
	 * @param strButtonName 确认按钮显示文字
	 * @param strParaFileName 文件上传的参数名
	 * @return 提交表单HTML文本
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName,
			String strParaFileName) {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\"  enctype=\"multipart/form-data\" action=\"" + ALIPAY_GATEWAY_NEW
				+ "_input_charset=UTF-8\" method=\"" + strMethod + "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("<input type=\"file\" name=\"" + strParaFileName + "\" />");

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");

		return sbHtml.toString();
	}

	/**
	 * 建立请求，以表单HTML形式构造（默认）
	 * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
	 * @param sParaTemp 请求参数数组
	 * @param strMethod 提交方式。两个值可选：post、get
	 * @param strButtonName 确认按钮显示文字
	 * @return 提交表单HTML文本
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW, Map<String, String> sParaTemp, String strMethod, String strButtonName) {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + ALIPAY_GATEWAY_NEW + "_input_charset=UTF-8\" method=\""
				+ strMethod + "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

		return sbHtml.toString();
	}

	/**
	 * 建立请求，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("", "",sParaTemp)
	 * @paramALIPAY_GATEWAY_NEW 支付宝网关地址
	 * @param strParaFileName 文件类型的参数名
	 * @param strFilePath 文件路径
	 * @param sParaTemp 请求参数数组
	 * @return 支付宝处理结果
	 * @throws Exception
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW, String strParaFileName, String strFilePath, Map<String, String> sParaTemp)
			throws Exception {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp);

		String strResult = HttpClientUtils.post(ALIPAY_GATEWAY_NEW + "_input_charset=UTF-8", sPara, "UTF-8", "UTF-8");

		return strResult;
	}

	/**
	 * 生成要请求给支付宝的参数数组
	 * @param sParaTemp 请求前的参数数组
	 * @return 要请求的参数数组
	 */
	private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
		// 除去数组中的空值和签名参数
		Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
		// 生成签名结果
		String mysign = buildRequestMysign(sPara);

		// 签名结果与签名方式加入请求提交参数组中
		sPara.put("sign", mysign);
		if (!sPara.get("service").equals("alipay.wap.trade.create.direct")
				&& !sPara.get("service").equals("alipay.wap.auth.authAndExecute")) {
			sPara.put("sign_type", PayPropertySupport.getProperty("pay.ali.wap.signType"));
		}

		return sPara;
	}

	/**
	 * 生成签名结果
	 * @param sPara 要签名的数组
	 * @return 签名结果字符串
	 */
	public static String buildRequestMysign(Map<String, String> sPara) {
		String prestr = AlipayCore.createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		String mysign = "";
		if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("MD5")) {
			mysign = MD5.sign(prestr, PayPropertySupport.getProperty("pay.ali.wap.key"), "UTF-8");
		}
		if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("0001")) {
			mysign = RSA.sign(prestr, PayPropertySupport.getProperty("pay.ali.wap.privateKey"), "UTF-8");
		}
		return mysign;
	}

	/**
	 * 解析远程模拟提交后返回的信息，获得token
	 * @param text 要解析的字符串
	 * @return 解析结果
	 * @throws Exception
	 */
	public static String getRequestToken(String text) throws Exception {
		String request_token = "";
		// 以“&”字符切割字符串
		String[] strSplitText = text.split("&");
		// 把切割后的字符串数组变成变量与数值组合的字典数组
		Map<String, String> paraText = new HashMap<String, String>();
		for (int i = 0; i < strSplitText.length; i++) {

			// 获得第一个=字符的位置
			int nPos = strSplitText[i].indexOf("=");
			// 获得字符串长度
			int nLen = strSplitText[i].length();
			// 获得变量名
			String strKey = strSplitText[i].substring(0, nPos);
			// 获得数值
			String strValue = strSplitText[i].substring(nPos + 1, nLen);
			// 放入MAP类中
			paraText.put(strKey, strValue);
		}

		if (paraText.get("res_data") != null) {
			String res_data = paraText.get("res_data");
			// 解析加密部分字符串（RSA与MD5区别仅此一句）
			if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("0001")) {
				res_data = RSA.decrypt(res_data, PayPropertySupport.getProperty("pay.ali.wap.privateKey"), "UTF-8");
			}

			// token从res_data中解析出来（也就是说res_data中已经包含token的内容）
			Document document = DocumentHelper.parseText(res_data);
			request_token = document.selectSingleNode("//direct_trade_create_res/request_token").getText();
		}
		return request_token;
	}

	/**
	 * 验证消息是否是支付宝发出的合法消息，验证callback
	 * @param params 通知返回来的参数数组
	 * @return 验证结果
	 */
	public static boolean verifyReturn(Map<String, String> params) {
		String sign = "";
		// 获取返回时的签名验证结果
		if (params.get("sign") != null) {
			sign = params.get("sign");
		}
		// 验证签名
		boolean isSign = AlipayNotify.getWAPSignVeryfy(params, sign, true);

		// 写日志记录（若要调试，请取消下面两行注释）
		// String sWord = "isSign=" + isSign + "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
		// AlipayCore.logResult(sWord);

		// 判断isSign是否为true
		// isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
		if (isSign) {
			return true;
		} else {
			return false;
		}
	}

}
