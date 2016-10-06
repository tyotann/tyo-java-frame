package com.ihidea.component.pay.weixin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.XMLUtilsEx;

@Controller
public class WeixinPayController {

	@Autowired
	private WeixinPayService weixinPayService;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/pay.weixin.notify.do")
	public void notify(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String xmlStr = IOUtils.toString(request.getInputStream());

		Map<String, String> requestMap = XMLUtilsEx.deserialize(xmlStr, Map.class);

		String signStr = requestMap.get("sign");

		requestMap.remove("sign");

		Map<String, String> responseMap = new HashMap<String, String>();

		if (StringUtils.isNotBlank(signStr) && signStr.equals(weixinPayService.getSign(requestMap))) {

			System.out.println("支付成功");

			responseMap.put("return_code", "SUCCESS");

			// 调用回掉业务逻辑
			weixinPayService.orderSuccess(requestMap);
		} else {
			responseMap.put("return_code", "FAIL");
			responseMap.put("return_msg", "签名失败");
		}

		ServletUtilsEx.renderText(response, "<xml>" + XMLUtilsEx.serialize(responseMap) + "</xml>");
	}

}
