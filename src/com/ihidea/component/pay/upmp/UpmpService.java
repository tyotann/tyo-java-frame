package com.ihidea.component.pay.upmp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ihidea.component.pay.PayPropertySupport;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.HttpClientUtils;

/**
 * 类名：订单推送请求接口实例类文件 功能：订单推送请求接口实例 版本：1.0 日期：2012-10-11 作者：中国银联UPMP团队 版权：中国银联
 * 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 */
public class UpmpService {

	/**
	 * 创建订单，获得网银订单编号
	 * @param orderId 订单编号
	 * @param price 订单价格
	 * @param orderTime 订单时间
	 * @param desc 订单描述
	 * @param reservedMap 扩充参数
	 * @return
	 */
	public static String trade(String appid, String orderId, BigDecimal price, Date orderTime, String desc, Map<String, String> reservedMap) {

		Map<String, String> req = new HashMap<String, String>();

		// 版本号
		req.put("version", PayPropertySupport.getProperty("pay.upmp.version"));

		// 字符编码
		req.put("charset", PayPropertySupport.getProperty("pay.upmp.charset"));

		// 交易类型（01 消费； 02 预授权）
		req.put("transType", "01");

		// 商户代码
		req.put("merId", PayPropertySupport.getProperty("pay.upmp.mer.id"));

		// 通知URL
		req.put("backEndUrl", PayPropertySupport.getProperty("pay.upmp.mer.back.end.url") + "?appid=" + appid);

		// 前台通知URL(可选)
		// req.put("frontEndUrl", PayPropertySupport.getProperty("pay.upmp.mer.back.end.url"));

		// 订单描述(可选)
		req.put("orderDescription", desc);

		// 交易开始日期时间yyyyMMddHHmmss
		req.put("orderTime", DateUtilsEx.formatToString(orderTime, "yyyyMMddHHmmss"));

		req.put("orderTimeout", "");// 订单超时时间yyyyMMddHHmmss(可选)

		// 订单号(商户根据自己需要生成订单号)
		req.put("orderNumber", orderId);

		// 订单金额(单位为分)
		req.put("orderAmount", String.valueOf(price.multiply(new BigDecimal(100)).setScale(0)));

		// 交易币种(人民币)
		req.put("orderCurrency", "156");

		// 请求方保留域(可选，用于透传商户信息)
		req.put("reqReserved", appid);

		// 商户保留域(可选)
		if (reservedMap != null && reservedMap.size() > 0) {
			req.put("merReserved", buildReserved(reservedMap));
		}

		return trade(req);
	}

	public static Map<String, String> query(String orderId, Date orderTime) {

		Map<String, String> req = new HashMap<String, String>();

		// 版本号
		req.put("version", PayPropertySupport.getProperty("pay.upmp.version"));

		// 字符编码
		req.put("charset", PayPropertySupport.getProperty("pay.upmp.charset"));

		// 交易类型
		req.put("transType", "01");

		// 商户代码
		req.put("merId", PayPropertySupport.getProperty("pay.upmp.mer.id"));

		// 交易开始日期时间yyyyMMddHHmmss或yyyyMMdd
		req.put("orderTime", DateUtilsEx.formatToString(orderTime, "yyyyMMddHHmmss"));

		// 订单号
		req.put("orderNumber", orderId);
		// 保留域填充方法
		// Map<String, String> merReservedMap = new HashMap<String, String>();
		// merReservedMap.put("test", "test");
		// req.put("merReserved", UpmpService.buildReserved(merReservedMap));// 商户保留域(可选)

		// Map<String, String> resp = new HashMap<String, String>();
		return UpmpService.query(req);
	}

	/**
	 * 交易查询处理
	 * @param req 请求要素
	 * @param resp 应答要素
	 * @return 是否成功
	 */
	private static Map<String, String> query(Map<String, String> req) {
		String nvp = buildReq(req);

		String respString = HttpClientUtils.post(PayPropertySupport.getProperty("pay.upmp.query.url"), nvp, "UTF-8",
				PayPropertySupport.getProperty("pay.upmp.charset"));

		Map<String, String> resp = new HashMap<String, String>();

		boolean verifyFlag = verifyResponse(respString, resp);

		if (!verifyFlag) {
			throw new ServiceException("网银支付订单创建后验证失败");
		}

		return resp;
	}

	/**
	 * 交易接口处理
	 * @param req 请求要素
	 * @param resp 应答要素
	 * @return 是否成功
	 */
	private static String trade(Map<String, String> req) {
		String nvp = buildReq(req);

		String respString = HttpClientUtils.post(PayPropertySupport.getProperty("pay.upmp.trade.url"), nvp, "UTF-8",
				PayPropertySupport.getProperty("pay.upmp.charset"));

		Map<String, String> resp = new HashMap<String, String>();

		boolean verifyFlag = verifyResponse(respString, resp);

		if (!verifyFlag) {
			throw new ServiceException("网银支付订单创建后验证失败");
		}

		if (resp.containsKey("respMsg")) {
			throw new ServiceException(resp.get("respMsg"));
		}

		return (String) resp.get("tn");
	}

	/**
	 * 拼接保留域
	 * @param req 请求要素
	 * @return 保留域
	 */
	private static String buildReserved(Map<String, String> req) {
		StringBuilder merReserved = new StringBuilder();
		merReserved.append("{");
		merReserved.append(UpmpCore.createLinkString(req, false, true));
		merReserved.append("}");
		return merReserved.toString();
	}

	/**
	 * 拼接请求字符串
	 * @param req 请求要素
	 * @return 请求字符串
	 */
	private static String buildReq(Map<String, String> req) {
		// 除去数组中的空值和签名参数
		Map<String, String> filteredReq = UpmpCore.paraFilter(req);
		// 生成签名结果
		String signature = UpmpCore.buildSignature(filteredReq);

		// 签名结果与签名方式加入请求提交参数组中
		filteredReq.put(PayPropertySupport.getProperty("pay.upmp.signature"), signature);
		filteredReq.put(PayPropertySupport.getProperty("pay.upmp.signMethod"), PayPropertySupport.getProperty("pay.upmp.sign.type"));

		return UpmpCore.createLinkString(filteredReq, false, true);
	}

	/**
	 * 异步通知消息验证
	 * @param para 异步通知消息
	 * @return 验证结果
	 */
	public static boolean verifySignature(Map<String, String> para) {
		String respSignature = para.get(PayPropertySupport.getProperty("pay.upmp.signature"));
		// 除去数组中的空值和签名参数
		Map<String, String> filteredReq = UpmpCore.paraFilter(para);
		String signature = UpmpCore.buildSignature(filteredReq);
		if (null != respSignature && respSignature.equals(signature)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 应答解析
	 * @param respString 应答报文
	 * @param resp 应答要素
	 * @return 应答是否成功
	 */
	private static boolean verifyResponse(String respString, Map<String, String> resp) {
		if (respString != null && !"".equals(respString)) {
			// 请求要素
			Map<String, String> para;
			try {
				para = UpmpCore.parseQString(respString);
			} catch (Exception e) {
				return false;
			}
			boolean signIsValid = verifySignature(para);

			resp.putAll(para);

			if (signIsValid) {
				return true;
			} else {
				return false;
			}

		}
		return false;
	}

	public static void main(String[] args) {

		// Map<String, String> reservedMap = new HashMap<String, String>();
		// reservedMap.put("appid", "2");

		// UpmpService.trade("2", "2014092624080932811", new BigDecimal("0.01"), new Date(), null, null);

		// 201409151613340015292
		UpmpService.query("2014092624080932811", new Date());
	}

}
