package com.ihidea.component.pay.abc;

import org.springframework.stereotype.Controller;

import com.ihidea.core.base.CoreController;

@Controller
public class ABCPayController extends CoreController {
//
//	@Autowired
//	private CptMobileOrderService cptMobileOrderService;
//
//	@Autowired
//	private OrderService orderService;
//
//	@RequestMapping(value = "/pay.abc.trade.do")
//	public void trade(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到农行交易信息,进入交易流程!");
//
//		try {
//			// 获取订单数据
//			String orderId = request.getParameter("orderId");
//
//			if (StringUtils.isBlank(orderId)) {
//				ServletUtilsEx.renderText(response, "fail 订单号为必须项");
//				return;
//			}
//
//			Order entity = new Order();
//			entity.setId(orderId);
//			Order order = orderService.get(entity);
//
//			// TODO order
//			ABCPayCore.trade(order.getId(), 0, "暴雨洗车-农行支付账单", order.getPrice(), order.getCreateTime(), "2", PaymentRequest.PAY_TYPE_ABC,
//					PaymentRequest.PRD_TYPE_ONE, "1", PayPropertySupport.getProperty("pay.abc.success.url") + "?appid=2", response);
//			logger.fatal("$$$$农行生成交易处理结束");
//		} catch (Exception e) {
//			logger.fatal("$$$$农行生成交易时出现业务逻辑异常:" + e.getMessage(), e);
//		}
//	}
//
//	@RequestMapping(value = "/pay.abc.sucess.do")
//	public void sucess(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到中国农行sucess信息,进入notify流程!");
//
//		try {
//			payAfter(request, response);
//		} catch (Exception e) {
//			logger.fatal("$$$$支付后业务逻辑异常:" + e.getMessage(), e);
//			ServletUtilsEx.renderText(response, "fail");
//			return;
//		}
//
//	}
//
//	@RequestMapping(value = "/pay.abc.error.do")
//	public void error(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到中国农行error信息,进入notify流程!");
//
//		StringBuffer sbHtml = new StringBuffer();
//
//		// 生成返回HTML
//		{
//			sbHtml.append("<!DOCTYPE HTML><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
//			sbHtml.append("<title>农行交易成功</title></head>");
//
//			sbHtml.append("<script type='text/javascript' src='http://libs.baidu.com/jquery/1.8.3/jquery.min.js'></script>");
//			sbHtml.append("<script type='text/javascript' src='/storm/resources/js/mobile/core.js'></script>");
//			sbHtml.append("<body></body>");
//
//			sbHtml.append("<script type='text/javascript'>AE.ready(function() {MB.call('ABCResult', {});});</script>");
//
//			sbHtml.append("</html>");
//		}
//
//		response.reset();
//		response.setContentType("text/html; charset=utf-8");
//
//		// 返回页面
//		response.getWriter().write(sbHtml.toString());
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
//		// 获取银联POST过来异步通知信息
//		Map<String, String> params = new HashMap<String, String>();
//		Map<String, Object> requestParams = request.getParameterMap();
//		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
//			String name = iter.next();
//			String[] values = (String[]) requestParams.get(name);
//			String valueStr = "";
//			for (int i = 0; i < values.length; i++) {
//				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
//			}
//			params.put(name, valueStr);
//		}
//		System.out.println(params.toString());
//		//{MSG=PE1TRz48TWVzc2FnZT48VHJ4UmVzcG9uc2U+PFJldHVybkNvZGU+MDAwMDwvUmV0dXJuQ29kZT48RXJyb3JNZXNzYWdlPjwvRXJyb3JNZXNzYWdlPjxFQ01lcmNoYW50VHlwZT5CMkM8L0VDTWVyY2hhbnRUeXBlPjxNZXJjaGFudElEPjIzMjAxMDQwMDY1M0EwMTwvTWVyY2hhbnRJRD48VHJ4VHlwZT5QYXlSZXN1bHQ8L1RyeFR5cGU+PE9yZGVyTm8+MjAxNDA4MjcyNDEwMTc0NDg5OTwvT3JkZXJObz48QW1vdW50PjAuMDE8L0Ftb3VudD48UGF5VHlwZT5QQVkwNzwvUGF5VHlwZT48QmF0Y2hObz4wMDAwMDI8L0JhdGNoTm8+PFZvdWNoZXJObz4wMDAwMjI8L1ZvdWNoZXJObz48SG9zdERhdGU+MjAxNC8xMi8wNjwvSG9zdERhdGU+PEhvc3RUaW1lPjEzOjExOjU1PC9Ib3N0VGltZT48Tm90aWZ5VHlwZT4wPC9Ob3RpZnlUeXBlPjxpUnNwUmVmPjU2MTIwNjAwMzIxOTwvaVJzcFJlZj48L1RyeFJlc3BvbnNlPjwvTWVzc2FnZT48U2lnbmF0dXJlLUFsZ29yaXRobT5TSEExd2l0aFJTQTwvU2lnbmF0dXJlLUFsZ29yaXRobT48U2lnbmF0dXJlPm5ramlKWUd3VVRQNzFUMTFPK3J6c09PUWpNeXB2UEEyMlQ4WnJROHluQkhIdUJXOEQzRU94dkd0T042TWh6WVFQWEJETmEzbjE3V1NIeFFpejk3QW9ZdDBGdTM5YW9wQ0RZSklnYkdjdU1WcVY3ZlY0ZHFEaGZwTmNpNzJaemI5YVRmd1F6Vkl0VWQ1OTh6VDlxVC9rZS92T2FtM20yTThDUllKMnZRRllPST08L1NpZ25hdHVyZT48L01TRz4=, submitButton=返回商户网站}
//		// 服务器签名验证成功
//		String tABC = request.getParameter("ABC");
//		System.out.println("tABC         = [" + tABC + "]<br>");
//		String submitButton = request.getParameter("submitButton");
//
//		//1、取得MSG参数，并利用此参数值生成支付结果对象
//		PaymentResult tResult = new PaymentResult(request.getParameter("MSG"));
//
//		//2、判断支付结果状态，进行后续操作
//		if (tResult.isSuccess()) {
//			//3、支付成功
//			  System.out.println("TrxType         = [" + tResult.getValue("TrxType"        ) + "]<br>");
//			  System.out.println("OrderNo         = [" + tResult.getValue("OrderNo"        ) + "]<br>");
//			  System.out.println("Amount          = [" + tResult.getValue("Amount"         ) + "]<br>");
//			  System.out.println("BatchNo         = [" + tResult.getValue("BatchNo"        ) + "]<br>");
//			  System.out.println("VoucherNo       = [" + tResult.getValue("VoucherNo"      ) + "]<br>");
//			  System.out.println("HostDate        = [" + tResult.getValue("HostDate"       ) + "]<br>");
//			  System.out.println("HostTime        = [" + tResult.getValue("HostTime"       ) + "]<br>");
//			  System.out.println("MerchantRemarks = [" + tResult.getValue("MerchantRemarks") + "]<br>");
//			  System.out.println("PayType         = [" + tResult.getValue("PayType"        ) + "]<br>");
//			  System.out.println("NotifyType      = [" + tResult.getValue("NotifyType"     ) + "]<br>");
//			  System.out.println("TrnxNo          = [" + tResult.getValue("iRspRef"        ) + "]<br>");
//			  
//			  synchronized (this) {
//
//					// 更新支付信息
//					CptMobileOrder entity = new CptMobileOrder();
//
//					// 上下文参数保存appid
//					LocalAttributeHolder.getContext().put("appid", "2");
//					entity.setAppid("2");
//
//					entity.setId(tResult.getValue("OrderNo"));
//					// entity.setBuyAccount(request.getParameter("out_trade_no"));
//					entity.setTradeStatus(new BigDecimal(9));
//					// entity.setTradeNo(trade_no);
//					// entity.setBuyAccount(request.getParameter("buyer_email"));
//					cptMobileOrderService.updateOrderStatus(entity);
//				}
//			  
//			  if(StringUtils.equals(submitButton, "返回商户网站")){
//				  
//					StringBuffer sbHtml = new StringBuffer();
//
//					// 生成返回HTML
//					{
//						sbHtml.append("<!DOCTYPE HTML><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
//						sbHtml.append("<title>农行交易成功</title></head>");
//
//						sbHtml.append("<script type='text/javascript' src='http://libs.baidu.com/jquery/1.8.3/jquery.min.js'></script>");
//						sbHtml.append("<script type='text/javascript' src='/storm/resources/js/mobile/core.js'></script>");
//						sbHtml.append("<body></body>");
//
//						sbHtml.append("<script type='text/javascript'>AE.ready(function() {MB.call('ABCResult', {});});</script>");
//
//						sbHtml.append("</html>");
//					}
//
//					response.reset();
//					response.setContentType("text/html; charset=utf-8");
//
//					// 返回页面
//					response.getWriter().write(sbHtml.toString());
//			  }
//		}
//		else {
//		  //4、支付失败
//			System.out.println("ReturnCode   = [" + tResult.getReturnCode  () + "]<br>");
//			System.out.println("ErrorMessage = [" + tResult.getErrorMessage() + "]<br>");
//			
//			throw new ServiceException("服务器签名失败");
//		}
//		
//	}

}
