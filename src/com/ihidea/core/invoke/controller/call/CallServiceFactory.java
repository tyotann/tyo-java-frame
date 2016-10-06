package com.ihidea.core.invoke.controller.call;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;

@Component
public class CallServiceFactory {

	/**
	 * 得到调用类型
	 * @param serviceName
	 * @return
	 */
	public static CallController getInstance(String serviceName) {

		CallController service = null;

		if (serviceName.indexOf("@") == -1) {
			if (serviceName.indexOf(".") > -1) {
				service = (CallController) SpringContextLoader.getBean("callJavaController");
			} else if (serviceName.length() == 6) {

				// 存储过程传入的服务为functionCode,6位代码
				service = (CallController) SpringContextLoader.getBean("callProcedureController");
			}
		} else {

			String protocol = StringUtils.substringAfter(serviceName, "@");

			// TODO
			// 默认使用http协议请求
			if ("ESB".equals(protocol)) {
				protocol = "ESB.http";
			}

			if ("ESB.http".equals(protocol)) {
				service = (CallController) SpringContextLoader.getBean("callEsbHttpController");
			} else if ("ESB.file".equals(protocol)) {
				service = (CallController) SpringContextLoader.getBean("callEsbFileController");
			} else {
				throw new ServiceException("没有找到对应的协议解析器:" + protocol + "!");
			}
		}

		if (service == null) {
			throw new ServiceException("服务名:" + serviceName + ",没有找到对应的解析器!");
		}

		return service;
	}
}
