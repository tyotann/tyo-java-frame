package com.ihidea.component.pay.ail;

import org.springframework.stereotype.Controller;

import com.ihidea.core.base.CoreController;

/**
 * 支付宝
 * 
 * @author TYOTANN
 */
@Controller
public class AliPayMobileController extends CoreController {
//
//	@Autowired
//	private OrderService orderService;
//
//	@Autowired
//	private DictionaryService dictionaryService;
//
//	@Autowired
//	private CptMobileOrderService cptMobileOrderService;
//
//	/**
//	 * 支付宝即时到账交易接口
//	 * 
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/payAli.do")
//	public void pay(String appid, String orderId, HttpServletResponse response, HttpServletRequest request) throws Exception {
//
//		String orderName = null;
//		String amount = null;
//		String orderDesc = "在线支付";
//
//		// 支付前调用业务
//		{
//
//			// 获取必要参数
//			orderName = orderId;
//
//			logger.fatal("$$$$新增支付订单——商户订单号:" + orderId);
//		}
//
//		// 获得支付信息
//		Map<String, String> dicMap = dictionaryService.getSystemDic(appid);
//		String payAli = dicMap.get("pay.ali.partnerId");
//		String sellAccount = dicMap.get("pay.ali.sellerId");
//		String payKey = dicMap.get("pay.ali.key");
//
//		// 订单信息
//		Order entity = new Order();
//		entity.setAppid(appid);
//		entity.setId(orderId);
//		Order order = orderService.get(entity);
//		if (order != null) {
//			amount = String.valueOf(order.getPrice());
//		}
//
//		// 把请求参数打包成数组
//		Map<String, String> param = new HashMap<String, String>();
//
//		param.put("service", "create_direct_pay_by_user");
//
//		// 合作身份者ID
//		param.put("partner", payAli);
//
//		// 编码
//		param.put("_input_charset", "utf-8");
//
//		// 支付类型
//		param.put("payment_type", PayPropertySupport.getProperty("pay.ali.paymentType"));
//
//		// 服务器异步通知页面路径
//		param.put("notify_url", "http://" + ServletUtilsEx.getHostName(request) + "/" + PayPropertySupport.getProperty("pay.ali.notifyUrl")
//				+ "?appid=" + appid);
//
//		// 页面跳转同步通知页面路径
//		param.put("return_url", "http://" + ServletUtilsEx.getHostName(request) + "/" + PayPropertySupport.getProperty("pay.ali.returnUrl")
//				+ "?appid=" + appid);
//
//		// 卖家支付宝帐户
//		param.put("seller_email", sellAccount);
//
//		// 商户订单号
//		param.put("out_trade_no", orderId);
//
//		// 订单名称
//		param.put("subject", orderName);
//
//		// 付款金额
//		param.put("total_fee", amount);
//
//		// 订单描述
//		param.put("body", orderDesc);
//
//		// 商品展示地址 需以http://开头的完整路径，例如：http://www.xxx.com/myorder.html
//		// TODO
//		param.put("show_url", StringUtils.EMPTY);
//
//		// 防钓鱼时间戳 若要使用请调用类文件submit中的query_timestamp函数
//		// TODO
//		param.put("anti_phishing_key", StringUtils.EMPTY);
//
//		// 客户端的IP地址 非局域网的外网IP地址，如：221.0.0.1
//		param.put("exter_invoke_ip", StringUtils.EMPTY);
//
//		// 除去数组中的空值和签名参数
//		param = AlipayCore.paraFilter(param);
//
//		// 生成签名结果
//		{
//			String prestr = AlipayCore.createLinkString(param); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
//			String mysign = MD5.sign(prestr, payKey, "utf-8");
//
//			// 签名结果与签名方式加入请求提交参数组中
//			param.put("sign", mysign);
//			param.put("sign_type", "MD5");
//		}
//
//		StringBuffer sbHtml = new StringBuffer();
//
//		// 生成返回HTML
//		{
//			sbHtml
//					.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
//			sbHtml
//					.append("<title>支付宝即时到账交易接口</title></head><form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"https://mapi.alipay.com/gateway.do?_input_charset=urf-8\" method=\"get\">");
//
//			List<String> keys = new ArrayList<String>(param.keySet());
//
//			for (int i = 0; i < keys.size(); i++) {
//				String name = (String) keys.get(i);
//				String value = (String) param.get(name);
//
//				sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
//			}
//
//			// submit按钮控件请不要含有name属性
//			sbHtml.append("<input type=\"submit\" value=\"确认\" style=\"display:none;\"></form>");
//			sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script><body></body></html>");
//		}
//
//		response.reset();
//		response.setContentType("text/html; charset=utf-8");
//
//		// 返回页面
//		response.getWriter().write(sbHtml.toString());
//	}
//
//	/**
//	 * 获取支付宝GET过来反馈信息
//	 * 
//	 * @param orderId
//	 * @param orderName
//	 * @param amount
//	 * @param orderDesc
//	 * @param response
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/payReturn.do")
//	public String payReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到支付宝return信息,进入return流程!");
//		try {
//			payAfter(request, response);
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//		}
//
//		return "common/autoClose";
//	}
//
//	/**
//	 * 获取支付宝Notify过来反馈信息
//	 * 
//	 * @param request
//	 * @param response
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/payNotify.do")
//	public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到支付宝notify信息,进入notify流程!");
//
//		try {
//			payAfter(request, response);
//			logger.fatal("$$$$收到支付宝notify信息,进入notify流程!返回的response");
//			ServletUtilsEx.renderText(response, "success");
//
//			return;
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//			ServletUtilsEx.renderText(response, "fail");
//			return;
//		}
//
//	}
//
//	/**
//	 * 支付后的业务操作
//	 * 
//	 * @param request
//	 * @param response
//	 * @return
//	 * @throws Exception
//	 */
//	@SuppressWarnings("unchecked")
//	private void payAfter(HttpServletRequest request, HttpServletResponse response) throws Exception {
//
//		try {
//
//			// 获取支付宝POST过来反馈信息
//			Map<String, String> params = new HashMap<String, String>();
//			Map<String, Object> requestParams = request.getParameterMap();
//
//			for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
//				String name = iter.next();
//				String[] values = (String[]) requestParams.get(name);
//				String valueStr = "";
//				for (int i = 0; i < values.length; i++) {
//					valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
//				}
//
//				// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//				// valueStr = new String(valueStr.getBytes("ISO-8859-1"),
//				// "gbk");
//				params.put(name, valueStr);
//			}
//
//			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)
//			// 商户订单号
//			LocalAttributeHolder.getContext().put("appid", request.getParameter("appid"));
//
//			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
//
//			// 支付宝交易号
//			String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
//
//			// 交易状态
//			String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
//
//			logger.fatal("$$$$支付信息——商户订单号:" + out_trade_no + ";支付宝交易号:" + trade_no + ",交易状态:" + trade_status);
//
//			if (AlipayNotify.verify(params)) {
//				// ////////////////////////////////////////////////////////////////////////////////////////
//				// 请在这里加上商户的业务逻辑程序代码
//
//				if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) {
//
//					// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
//					synchronized (this) {
//
//						// 更新支付信息
//						CptMobileOrder entity = new CptMobileOrder();
//						entity.setAppid(request.getParameter("appid"));
//						entity.setId(request.getParameter("out_trade_no"));
//						// entity.setBuyAccount(request.getParameter("out_trade_no"));
//						entity.setTradeStatus(new BigDecimal(9));
//						entity.setTradeNo(trade_no);
//						entity.setBuyAccount(request.getParameter("buyer_email"));
//						entity.setPayType(new BigDecimal(2));// 支付宝支付方式为2
//						cptMobileOrderService.updateOrderStatus(entity);
//					}
//				} else {
//					logger.fatal("$$$$支付交易状态未知——商户订单号:" + out_trade_no + ";支付宝交易号:" + trade_no + ",交易状态:" + trade_status);
//				}
//			} else {
//				logger.fatal("$$$$支付验证失败——商户订单号:" + out_trade_no + ";支付宝交易号:" + trade_no + ",交易状态:" + trade_status);
//			}
//
//			// ServletUtilsEx.renderText(response, "购买成功");
//			// logger.fatal("$$$$项目跳转地址：http://" +
//			// ServletUtilsEx.getHostName(request) +
//			// "/web.wdzq.htm?module=user_orderdetail&orderId="+out_trade_no);
//			// response.sendRedirect("http://" +
//			// ServletUtilsEx.getHostName(request)+"/web.wdzq.htm?module=user_orderdetail&orderId="+out_trade_no);
//			// ServletUtilsEx.renderText(response, "success");
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//			ServletUtilsEx.renderText(response, "fail");
//		}
//	}
//
//	@RequestMapping(value = "/payWAPNotify.do")
//	public void payWAPNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到支付宝WAP notify信息,进入notify流程!");
//
//		try {
//			payWAPAfter(request, response, "payWapNotify");
//
//			System.out.println(request.getParameter("appid"));
//			logger.fatal("$$$$收到支付宝WAP notify信息,进入notify流程!返回的response");
//			ServletUtilsEx.renderText(response, "success");
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//			ServletUtilsEx.renderText(response, "fail");
//		}
//
//	}
//
//	@RequestMapping(value = "/payWAPReturn.do")
//	public String payWAPReturn(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
//		logger.fatal("$$$$收到支付宝WAP return信息,进入return流程!");
//		try {
//			payWAPAfter(request, response, "payWapReturn");
//
//			String appid = WeixinUtil.APPID;
//			String appSecret = WeixinUtil.APP_SECRET;
//			long timestamp = 0L;
//			Date nowDate = new Date();
//			SimpleDateFormat sdf = new SimpleDateFormat("mm");
//			if (Integer.parseInt(sdf.format(nowDate)) >= 30) {
//				timestamp = DateUtilsEx.formatToDate(DateUtilsEx.formatToString(nowDate, "yyyyMMddHH") + "3000", "yyyyMMddHHmmss").getTime() / 1000;
//			} else {
//				timestamp = DateUtilsEx.formatToDate(DateUtilsEx.formatToString(nowDate, "yyyyMMddHH") + "0000", "yyyyMMddHHmmss").getTime() / 1000;
//			}
//			String nonceStr = "cny";
//			String signature = WeixinJService.getSignature(appid, appSecret, timestamp, nonceStr,
//					request.getRequestURL() + "?" + request.getQueryString());
//
//			model.addAttribute("account", request.getParameter("account"));
//			model.addAttribute("appid", appid);
//			model.addAttribute("appSecret", appSecret);
//			model.addAttribute("timestamp", timestamp);
//			model.addAttribute("nonceStr", nonceStr);
//			model.addAttribute("signature", signature);
//			System.out.println("____________________signature = " + signature);
//			
//			return "mweb/activities_weixin/2015hny/success";
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//
//			model.addAttribute("errMsg", "支付异常");
//
//			return "mweb/activities_weixin/2015hny/main";
//		}
//
//	}
//
//	private void payWAPAfter(HttpServletRequest request, HttpServletResponse response, String from) throws Exception {
//
//		// 获取支付宝POST过来反馈信息
//		Map<String, String> params = new HashMap<String, String>();
//		Map requestParams = request.getParameterMap();
//		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
//			String name = (String) iter.next();
//			String[] values = (String[]) requestParams.get(name);
//			String valueStr = "";
//			for (int i = 0; i < values.length; i++) {
//				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
//			}
//			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
//			params.put(name, valueStr);
//		}
//
//		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
//
//		if (StringUtils.equals(from, "payWapNotify")) {
//
//			// RSA签名解密
//			if (PayPropertySupport.getProperty("pay.ali.wap.signType").equals("0001")) {
//
//				if (!params.containsKey("notify_data")) {
//					System.out.println(JSONUtilsEx.serialize(params));
//				}
//				params = AlipayNotify.decrypt(params);
//			}
//
//			// XML解析notify_data数据
//			Document doc_notify_data = DocumentHelper.parseText(params.get("notify_data"));
//
//			// 商户订单号
//			String out_trade_no = doc_notify_data.selectSingleNode("//notify/out_trade_no").getText();
//
//			// 支付宝交易号
//			String trade_no = doc_notify_data.selectSingleNode("//notify/trade_no").getText();
//
//			// 交易状态
//			String trade_status = doc_notify_data.selectSingleNode("//notify/trade_status").getText();
//
//			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
//
//			if (AlipayNotify.verifyNotify(params)) {// 验证成功
//				// ////////////////////////////////////////////////////////////////////////////////////////
//				// 请在这里加上商户的业务逻辑程序代码
//
//				// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
//
//				if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) {
//
//					// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
//					synchronized (this) {
//
//						// 更新支付信息
//						CptMobileOrder entity = new CptMobileOrder();
//						entity.setAppid("2");
//						entity.setId(out_trade_no);
//						// entity.setBuyAccount(request.getParameter("out_trade_no"));
//						entity.setTradeStatus(new BigDecimal(9));
//						entity.setTradeNo(trade_no);
//						entity.setBuyAccount(request.getParameter("buyer_email"));
//						entity.setPayType(new BigDecimal(2));// 支付宝支付方式为2
//						cptMobileOrderService.updateOrderStatus(entity);
//					}
//
//				}
//
//				// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
//
//				// ////////////////////////////////////////////////////////////////////////////////////////
//			} else {
//			}
//
//		} else if (StringUtils.equals(from, "payWapReturn")) {
//
//			// 商户订单号
//			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
//
//			// 支付宝交易号
//			String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
//
//			// 交易状态
//			String result = new String(request.getParameter("result").getBytes("ISO-8859-1"), "UTF-8");
//
//			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
//
//			if (AlipayCore.verifyReturn(params)) {// 验证成功
//				// ////////////////////////////////////////////////////////////////////////////////////////
//				// 请在这里加上商户的业务逻辑程序代码
//
//				// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
//				synchronized (this) {
//
//					// 更新支付信息
//					CptMobileOrder entity = new CptMobileOrder();
//					entity.setAppid("2");
//					entity.setId(out_trade_no);
//					// entity.setBuyAccount(request.getParameter("out_trade_no"));
//					entity.setTradeStatus(new BigDecimal(9));
//					entity.setTradeNo(trade_no);
//					entity.setBuyAccount(request.getParameter("buyer_email"));
//					entity.setPayType(new BigDecimal(2));// 支付宝支付方式为2
//					cptMobileOrderService.updateOrderStatus(entity);
//				}
//
//				// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
//
//				// ////////////////////////////////////////////////////////////////////////////////////////
//			} else {
//			}
//		}
//
//	}

}
