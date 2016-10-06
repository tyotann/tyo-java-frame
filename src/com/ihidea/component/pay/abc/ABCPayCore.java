package com.ihidea.component.pay.abc;


/**
 * 农行核心代码
 * 
 * @author TYOTANN
 */
public class ABCPayCore {

//	public static String trade(String orderId, Integer expiredDate, String orderDesc, BigDecimal price, Date orderTime,
//			String paymentLinkType, String paymentType, String productType, String notifyType, String notifyUrl,
//			HttpServletResponse response) throws Exception {
//
//		// 1、取得支付请求所需要的信息
//		// String tOrderAmountStr = request.getParameter("OrderAmount" );
//		// String tOrderURL = request.getParameter("OrderURL" );
//		// String tProductType = request.getParameter("ProductType" );
//		// String tPaymentType = request.getParameter("PaymentType" );
//		// String tNotifyType = request.getParameter("NotifyType" );
//		// String tResultNotifyURL = request.getParameter("ResultNotifyURL");
//		// String tMerchantRemarks = request.getParameter("MerchantRemarks");
//		// double tOrderAmount = Double.parseDouble(tOrderAmountStr);
//		// String tPaymentLinkType = request.getParameter("PaymentLinkType");
//		// String tBuyIP = request.getParameter("BuyIP");
//
//		// 2、生成订单对象
//		Order tOrder = new Order();
//
//		// 设定订单编号 （必要信息）
//		tOrder.setOrderNo(orderId);
//
//		// 设定订单有效期 （必要信息）
//		tOrder.setExpiredDate(expiredDate);
//
//		// 设定订单说明
//		tOrder.setOrderDesc(orderDesc);
//
//		String orderTimeStr = DateUtilsEx.formatToString(orderTime, "yyyy/MM/dd HH:mm:ss");
//
//		// 设定订单日期 （必要信息 - YYYY/MM/DD）
//		tOrder.setOrderDate(orderTimeStr.substring(0, 10));
//
//		// 设定订单时间 （必要信息 - HH:MM:SS）
//		tOrder.setOrderTime(orderTimeStr.substring(11));
//
//		// 设定订单金额 （必要信息）
//		tOrder.setOrderAmount(price.doubleValue());
//
//		// 设定订单网址
//		// tOrder.setOrderURL (tOrderURL );
//		// tOrder.setBuyIP (tBuyIP ); //设定订单IP
//
//		// 3、生成定单订单对象，并将订单明细加入定单中（可选信息）
//		// tOrder.addOrderItem(new OrderItem("IP000001", "中国移动IP卡", 100.00f,
//		// 1));
//		// tOrder.addOrderItem(new OrderItem("IP000002", "网通IP卡" , 90.00f, 2));
//
//		// 4、生成支付请求对象
//		PaymentRequest tPaymentRequest = new PaymentRequest();
//		tPaymentRequest.setOrder(tOrder); // 设定支付请求的订单 （必要信息）
//
//		// 设定商品种类 （必要信息）
//		// PaymentRequest.PRD_TYPE_ONE：非实体商品，如服务、IP卡、下载MP3、...
//		// PaymentRequest.PRD_TYPE_TWO：实体商品
//		tPaymentRequest.setProductType(productType);
//
//		// 设定支付类型
//		// PaymentRequest.PAY_TYPE_ABC：农行卡支付
//		// PaymentRequest.PAY_TYPE_INT：国际卡支付
//		tPaymentRequest.setPaymentType(paymentType);
//
//		// 设定商户通知方式---- 0：URL页面通知 ；1：服务器通知
//		tPaymentRequest.setNotifyType(notifyType);
//
//		// 设定支付结果回传网址 （必要信息）
//		tPaymentRequest.setResultNotifyURL(notifyUrl);
//
//		// 设定商户备注信息
//		// tPaymentRequest.setMerchantRemarks(tMerchantRemarks);
//
//		// 设定支付接入方式---1：internet网络接入 2：手机网络接入 3:数字电视网络接入 4:智能客户端
//		tPaymentRequest.setPaymentLinkType(paymentLinkType);
//
//		// 5、传送支付请求并取得支付网址
//		// TrxResponse tTrxResponse = tPaymentRequest.postRequest();
//		TrxResponse tTrxResponse = tPaymentRequest.extendPostRequest(1);
//		if (tTrxResponse.isSuccess()) {
//			System.out.println("PaymentURL-->" + tTrxResponse.getValue("PaymentURL"));
//			response.sendRedirect(tTrxResponse.getValue("PaymentURL"));
//		} else {
//			// 7、支付请求提交失败，商户自定后续动作
//			response.setCharacterEncoding("GBK");
//			response.getWriter().write(tTrxResponse.getErrorMessage());
//		}
//
//		return null;
//	}
//
//	public static String queryOrder(String orderNo) throws Exception {
//
//		QueryOrderRequest qOrder = new QueryOrderRequest();
//		qOrder.setOrderNo(orderNo);
//		qOrder.enableDetailQuery(false);// 不查询订单详情
//
//		TrxResponse resultTrxResponse = qOrder.postRequest();
//
//		if (resultTrxResponse.isSuccess()) {
//			// 5、生成订单对象
//			Order tOrder = new Order(new XMLDocument(resultTrxResponse.getValue("Order")));
//
//			// System.out.println(resultTrxResponse.isSuccess());
//			/**
//			 * 00：订单已取消 01：订单已建立，等待支付 02：消费者已支付，等待支付结果 03：订单已支付（支付成功）
//			 * 04：订单已结算（支付成功） 05：订单已退款 99：订单支付失败
//			 */
//			if (StringUtils.equals(tOrder.getOrderStatus(), "03") || StringUtils.equals(tOrder.getOrderStatus(), "04")) {
//				/*
//				 * System.out.println("OrderNo = [" + tOrder.getOrderNo () + "]<br>");
//				 * System.out.println("OrderAmount = [" + tOrder.getOrderAmount () + "]<br>");
//				 * System.out.println("OrderDesc = [" + tOrder.getOrderDesc () + "]<br>");
//				 * System.out.println("OrderDate = [" + tOrder.getOrderDate () + "]<br>");
//				 * System.out.println("OrderTime = [" + tOrder.getOrderTime () + "]<br>");
//				 * System.out.println("OrderURL = [" + tOrder.getOrderURL () + "]<br>");
//				 * System.out.println("PayAmount = [" + tOrder.getPayAmount () + "]<br>");
//				 * System.out.println("RefundAmount = [" +
//				 * tOrder.getRefundAmount() + "]<br>");
//				 * System.out.println("OrderStatus = [" + tOrder.getOrderStatus () + "]<br>");
//				 */
//				return "";
//			}
//			System.out.println(tOrder.getOrderStatus());
//			throw new ServiceException(200, "取消支付");
//			
//		} else {
//			System.out.println(resultTrxResponse.getErrorMessage());
//			throw new ServiceException(200, resultTrxResponse.getErrorMessage());
//		}
//	}
}
