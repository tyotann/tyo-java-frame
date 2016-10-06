package com.ihidea.component.pay.upmp;

import org.springframework.stereotype.Controller;

import com.ihidea.core.base.CoreController;

@Controller
public class UpmpController extends CoreController {
//
//	@Autowired
//	private CptMobileOrderService cptMobileOrderService;
//
//	@RequestMapping(value = "/pay.upmp.Notify.do")
//	public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.fatal("$$$$收到中国银联notify信息,进入notify流程!");
//
//		try {
//			payAfter(request, response);
//
//			logger.fatal("$$$$中国银联notify请求处理结束!返回的response");
//			ServletUtilsEx.renderText(response, "success");
//		} catch (Exception e) {
//			// TODO
//			logger.fatal("$$$$中国银联支付后业务逻辑异常:" + e.getMessage(), e);
//			ServletUtilsEx.renderText(response, "fail");
//			return;
//		}
//
//	}
//
//	/**
//	 * 支付后的业务操作
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
//System.out.println(params.toString());
//		// 服务器签名验证成功
//		if (UpmpService.verifySignature(params)) {
//
//			// 请在这里加上商户的业务逻辑程序代码
//			// 获取通知返回参数，可参考接口文档中通知参数列表(以下仅供参考)
//
//			// 交易状态：交易成功结束
//			if ("00".equals(params.get("transStatus"))) {
//
//				synchronized (this) {
//
//					// 更新支付信息
//					CptMobileOrder entity = new CptMobileOrder();
//					
//					// 上下文参数保存appid
//					LocalAttributeHolder.getContext().put("appid", params.get("reqReserved"));
//					entity.setAppid(params.get("reqReserved"));
//
//					entity.setId(params.get("orderNumber"));
//					// entity.setBuyAccount(request.getParameter("out_trade_no"));
//					entity.setTradeStatus(new BigDecimal(9));
//					// entity.setTradeNo(trade_no);
//					// entity.setBuyAccount(request.getParameter("buyer_email"));
//					entity.setPayType(new BigDecimal(3));//银联支付方式为3
//					cptMobileOrderService.updateOrderStatus(entity);
//				}
//			} else {
//			}
//
//		} else {
//			throw new ServiceException("服务器签名失败");
//		}
//	}

}
