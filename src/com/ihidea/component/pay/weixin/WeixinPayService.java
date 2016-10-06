package com.ihidea.component.pay.weixin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ihidea.component.pay.PayPropertySupport;
import com.ihidea.component.pay.PayUtils;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.DigitalUtils;
import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.SignatureUtils;
import com.ihidea.core.util.XMLUtilsEx;

@Service
public class WeixinPayService {

	private static Log logger = LogFactory.getLog(WeixinPayService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 得到签名
	 * @param params
	 * @return
	 */
	public String getSign(Map<String, String> params) {

		// 参数排序
		String preSingStr = PayUtils.createLinkString(params);

		// 拼接API密钥
		preSingStr = preSingStr + "&key=" + PayPropertySupport.getProperty("pay.weixin.key");

		return DigitalUtils.byte2hex(SignatureUtils.md5(preSingStr));
	}

	/**
	 * 查询订单状态<br>
	 * SUCCESS—支付成功<br>
	 * REFUND—转入退款<br>
	 * NOTPAY—未支付<br>
	 * CLOSED—已关闭<br>
	 * REVOKED—已撤销<br>
	 * USERPAYING--用户支付中<br>
	 * NOPAY--未支付(输入密码或确认支付超时)<br>
	 * PAYERROR--支付失败(其他原因，如银行返回失败)<br>
	 * @param transactionId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String queryOrder(String orderId) throws Exception {

		Map<String, String> params = new HashMap<String, String>();

		// 公众账号ID
		params.put("appid", PayPropertySupport.getProperty("pay.weixin.appid"));

		// 商户号
		params.put("mch_id", PayPropertySupport.getProperty("pay.weixin.mchId"));

		// 商户订单号
		params.put("out_trade_no", orderId);

		// 随机字符串
		params.put("nonce_str", String.valueOf(new Date().getTime() / 1000));

		// 签名
		params.put("sign", getSign(params));

		String resultJSON = HttpClientUtils.post("https://api.mch.weixin.qq.com/pay/orderquery", "<xml>" + XMLUtilsEx.serialize(params)
				+ "</xml>", "UTF-8", "UTF-8");

		Map<String, String> resultMap = XMLUtilsEx.deserialize(resultJSON, Map.class);

		if (resultMap.containsKey("trade_state")) {

			// 如果成功,记录信息
			if ("SUCCESS".equals(resultMap.get("trade_state"))) {
				orderSuccess(resultMap);
			}

			return (String) resultMap.get("trade_state");
		} else {
			throw null;
		}

	}

	/**
	 * 得到统一下单的PrepayId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getPayPrepayId(String orderId, String orderName, BigDecimal price, String ipAddress, String payType, String openId) {

		// 如果已存在prepayId，则直接返回
		try {
			Map<String, Object> payInfo = jdbcTemplate.queryForMap("select * from b_pay_weixin where pay_type = ? and order_id = ?",
					new Object[] { payType, orderId });

			if (payInfo != null && StringUtils.isNotBlank((String) payInfo.get("pre_pay_id"))) {
				return (String) payInfo.get("pre_pay_id");
			}
		} catch (EmptyResultDataAccessException e) {
		}

		Map<String, String> params = new HashMap<String, String>();

		// 公众账号ID
		params.put("appid", PayPropertySupport.getProperty("pay.weixin.appid"));

		// 商户号
		params.put("mch_id", PayPropertySupport.getProperty("pay.weixin.mchId"));

		// 随机字符串
		params.put("nonce_str", String.valueOf(new Date().getTime()));

		// 商品描述
		params.put("body", orderName);

		// 商户订单号
		params.put("out_trade_no", orderId);

		// 总金额(x100)
		params.put("total_fee", String.valueOf(price.multiply(BigDecimal.valueOf(100)).setScale(0)));

		// 终端IP
		params.put("spbill_create_ip", ipAddress);

		// 通知地址
		params.put("notify_url", PayPropertySupport.getProperty("pay.weixin.notifyUrl"));

		// 交易类型
		params.put("trade_type", payType);

		if (StringUtils.isNotBlank(openId)) {
			params.put("openid", openId);
		}

		// 签名
		params.put("sign", getSign(params));

		String resultJSON = HttpClientUtils.post("https://api.mch.weixin.qq.com/pay/unifiedorder", "<xml>" + XMLUtilsEx.serialize(params)
				+ "</xml>", "UTF-8", "UTF-8");

		Map<String, Object> resultMap = XMLUtilsEx.deserialize(resultJSON, Map.class);

		if (resultMap.containsKey("return_code") && StringUtils.isNotBlank((String) resultMap.get("prepay_id"))) {

			String prepayId = (String) resultMap.get("prepay_id");

			jdbcTemplate
					.update("insert into b_pay_weixin(order_id, order_name, price, ip_address, status, create_time, open_id, pre_pay_id,pay_type) values(?,?,?,?,0,now(),?,?,?)",
							new Object[] { orderId, orderName, price, ipAddress, openId, prepayId, payType });

			return prepayId;
		} else {
			logger.error(resultJSON);
			return null;
		}
	}

	public Map<String, String> getJSPayParam(String orderId, String orderName, BigDecimal price, String ipAddress, String openId) {

		Map<String, String> payParam = new HashMap<String, String>();

		payParam.put("appId", PayPropertySupport.getProperty("pay.weixin.appid"));

		payParam.put("timeStamp", String.valueOf((new Date().getTime() / 1000)));

		payParam.put("nonceStr", orderId);

		String prepayId = getPayPrepayId(orderId, orderName, price, ipAddress, "JSAPI", openId);

		payParam.put("package", "prepay_id=" + prepayId);

		payParam.put("signType", "MD5");

		payParam.put("paySign", getSign(payParam));

		return payParam;
	}

	public Map<String, String> getAPPPayParam(String orderId, String orderName, BigDecimal price, String ipAddress) {

		Map<String, String> payParam = new HashMap<String, String>();

		// 公众账号ID
		payParam.put("appid", PayPropertySupport.getProperty("pay.weixin.appid"));

		// 商户号
		payParam.put("partnerid", PayPropertySupport.getProperty("pay.weixin.mchId"));

		// 时间戳
		payParam.put("timestamp", String.valueOf((new Date().getTime() / 1000)));

		// 预支付交易会话ID
		payParam.put("prepayid", getPayPrepayId(orderId, orderName, price, ipAddress, "APP", null));

		// 扩展字段
		payParam.put("package", "Sign=WXPay");

		// 随机字符串
		payParam.put("noncestr", orderId);

		// 签名
		payParam.put("sign", getSign(payParam));

		return payParam;
	}

	protected synchronized void orderSuccess(Map<String, String> result) throws Exception {

		JdbcTemplate jdbcTemplate = SpringContextLoader.getBean(JdbcTemplate.class);

		// 订单编号
		String orderId = result.get("out_trade_no");

		// 微信支付订单号
		String transactionId = result.get("transaction_id");

		Map<String, Object> orderInfo = jdbcTemplate.queryForMap("select * from b_pay_weixin where order_id = ?", new Object[] { orderId });

		if (((Integer) orderInfo.get("status")) != 9) {

			// 更新成功标记
			jdbcTemplate.update("update b_pay_weixin set outer_order_id = ?, success_time = now(), status=9 where order_id = ?",
					new Object[] { transactionId, orderId });

			// 调用服务
			{
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("orderId", orderId);

				String[] serviceInfo = PayPropertySupport.getProperty("pay.weixin.callback").split("\\.");
				ClassUtilsEx.invokeMethod(serviceInfo[0], serviceInfo[1], param);
			}
		}

	}

	public static void main(String[] args) throws Exception {

		// System.out.println(WeixinPayService.queryOrder("2015092614243158647"));
		// System.out.println(JSONUtilsEx.serialize(WeixinPayService.getAPPPayParam("2015092614243158647", "测试订单3", new BigDecimal("0.11"),
		// "10.0.0.66")));
		// System.out.println(JSONUtilsEx.serialize(WeixinPayService.getAPPPayParam("0000000002", "测试订单3", new BigDecimal("0.11"),
		// "10.0.0.66")));

	}
}
