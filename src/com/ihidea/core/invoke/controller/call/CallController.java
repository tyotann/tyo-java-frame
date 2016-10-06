package com.ihidea.core.invoke.controller.call;

import java.util.Map;

import com.ihidea.core.invoke.controller.ResultEntity;

public interface CallController {

	/**
	 * <pre>
	 * 调用服务
	 * </pre>
	 * @param serviceName 服务名
	 * @param param 服务参数
	 * @return
	 */
	public ResultEntity call(String serviceName, Map<String, Object> param) throws Exception;

}
