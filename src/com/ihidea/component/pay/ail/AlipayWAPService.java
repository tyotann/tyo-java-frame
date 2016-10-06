package com.ihidea.component.pay.ail;

import org.springframework.stereotype.Service;

@Service
public class AlipayWAPService {
//
//	@Autowired
//	private DictionaryService dictionaryService;
//
//	// WAP
//	public void payWAP(String appid, String orderId, String orderName, BigDecimal price, String account, HttpServletResponse response,
//			HttpServletRequest request) throws Exception {
//
//		String ALIPAY_GATEWAY_NEW = "http://wappaygw.alipay.com/service/rest.htm?";
//
//		// 获得支付信息
//		Map<String, String> dicMap = dictionaryService.getSystemDic(appid);
//
//		// //////////////////////////////////调用授权接口alipay.wap.trade.create.direct获取授权码token//////////////////////////////////////
//
//		// 返回格式
//		String format = "xml";
//		// 必填，不需要修改
//
//		// 返回格式
//		String v = "2.0";
//		// 必填，不需要修改
//
//		// 请求号
//		String req_id = StringUtilsEx.getUUID();
//		// 必填，须保证每次请求都是唯一
//
//		// 服务器异步通知页面路径
//		String notify_url = ServletUtilsEx.getHostURLWithContextPath(request) + PayPropertySupport.getProperty("pay.ali.wap.notifyUrl")
//				+ "?account=" + account;
//
//		// 需http://格式的完整路径，不能加?id=123这类自定义参数
//
//		// 页面跳转同步通知页面路径
//		String call_back_url = ServletUtilsEx.getHostURLWithContextPath(request) + PayPropertySupport.getProperty("pay.ali.wap.returnUrl")
//				+ "?account=" + account;
//		// 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/
//
//		// 操作中断返回地址 TODO
//		String merchant_url = "";
//		// String merchant_url = "http://127.0.0.1:8080/WS_WAP_PAYWAP-JAVA-UTF-8/xxxxxx.jsp";
//		// 用户付款中途退出返回商户的地址。需http://格式的完整路径，不允许加?id=123这类自定义参数
//
//		// 卖家支付宝帐户
//		String seller_email = new String(dicMap.get("pay.ali.sellerId"));
//
//		// 请求业务参数详细
//		String req_dataToken = "<direct_trade_create_req><notify_url>" + notify_url + "</notify_url><call_back_url>" + call_back_url
//				+ "</call_back_url><seller_account_name>" + seller_email + "</seller_account_name><out_trade_no>" + orderId
//				+ "</out_trade_no><subject>" + orderName + "</subject><total_fee>" + String.valueOf(price) + "</total_fee><merchant_url>"
//				+ merchant_url + "</merchant_url></direct_trade_create_req>";
//
//		// ////////////////////////////////////////////////////////////////////////////////
//
//		// 把请求参数打包成数组
//		Map<String, String> sParaTempToken = new HashMap<String, String>();
//		sParaTempToken.put("service", "alipay.wap.trade.create.direct");
//		sParaTempToken.put("partner", dicMap.get("pay.ali.partnerId"));
//		sParaTempToken.put("_input_charset", "UTF-8");
//		sParaTempToken.put("sec_id", PayPropertySupport.getProperty("pay.ali.wap.signType"));
//		sParaTempToken.put("format", format);
//		sParaTempToken.put("v", v);
//		sParaTempToken.put("req_id", req_id);
//		sParaTempToken.put("req_data", req_dataToken);
//
//		// 建立请求
//		String sHtmlTextToken = AlipayCore.buildRequest(ALIPAY_GATEWAY_NEW, "", "", sParaTempToken);
//		// URLDECODE返回的信息
//		sHtmlTextToken = URLDecoder.decode(sHtmlTextToken, "UTF-8");
//		// 获取token
//		String request_token = AlipayCore.getRequestToken(sHtmlTextToken);
//		// out.println(request_token);
//
//		// //////////////////////////////////根据授权码token调用交易接口alipay.wap.auth.authAndExecute//////////////////////////////////////
//
//		// 业务详细
//		String req_data = "<auth_and_execute_req><request_token>" + request_token + "</request_token></auth_and_execute_req>";
//		// 必填
//
//		// 把请求参数打包成数组
//		Map<String, String> sParaTemp = new HashMap<String, String>();
//		sParaTemp.put("service", "alipay.wap.auth.authAndExecute");
//		sParaTemp.put("partner", dicMap.get("pay.ali.partnerId"));
//		sParaTemp.put("_input_charset", "UTF-8");
//		sParaTemp.put("sec_id", PayPropertySupport.getProperty("pay.ali.wap.signType"));
//		sParaTemp.put("format", format);
//		sParaTemp.put("v", v);
//		sParaTemp.put("req_data", req_data);
//
//		// 建立请求
//		String sHtmlText = AlipayCore.buildRequest(ALIPAY_GATEWAY_NEW, sParaTemp, "get", "确认");
//
//		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head>"
//				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><title>支付宝手机网页支付</title></head>" + sHtmlText
//				+ "<body></body></html>";
//
//		// 输出html文本
//		response.setContentType("text/html");
//		response.getOutputStream().write(html.getBytes());
//	}

}
