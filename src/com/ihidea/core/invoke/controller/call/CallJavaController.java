package com.ihidea.core.invoke.controller.call;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.util.ClassUtilsEx;

/**
 * 调用JAVA服务
 * @author TYOTANN
 */
@Component
public class CallJavaController implements CallController {

	/**
	 * 调用JAVA服务
	 * @param serviceName 服务名
	 * @param params 参数
	 * @return ResultEntity结果集
	 * @throws Exception 异常
	 */
	public ResultEntity call(String serviceName, Map<String, Object> params) throws Exception {

		String[] serviceInfo = serviceName.split("\\.");

		Object data = ClassUtilsEx.invokeMethod(serviceInfo[0], serviceInfo[1], params);

		ResultEntity result = new ResultEntity(data);

		return result;
	}
}
