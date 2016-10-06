package com.ihidea.core.support.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalAttributeHolder implements Filter {

	protected Log logger = LogFactory.getLog(super.getClass());

	private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>();

	private static List<String> holderAttributeNameList = new ArrayList<String>();

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			Map<String, Object> param = new HashMap<String, Object>();

			for (String name : holderAttributeNameList) {

				// appid==""的情况下不设置
				if (StringUtils.isNotBlank(request.getParameter(name))) {
					param.put(name, request.getParameter(name));
				}
			}

			threadLocal.set(param);

			try {
				chain.doFilter(request, response);
			} catch (ServletException e) {

				// 防止spring找不到velocity页面报错
				if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().indexOf("Could not resolve view with name") == 0) {

					// TODO 跳转到404页面
					logger.error("spring找不到跳转资源:" + e.getMessage());
				} else {
					throw e;
				}
			}

		} finally {
			threadLocal.set(null);
		}
	}

	public static Map<String, Object> getContext() {

		// 防止部分没有经过此过滤器的地方,调用此方法导致空指针异常
		if (threadLocal.get() == null) {
			threadLocal.set(new HashMap<String, Object>());
		}
		return (Map<String, Object>) threadLocal.get();
	}

	public void destroy() {
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		String holderAttributeName = filterConfig.getInitParameter("holderAttributeName");

		if (!StringUtils.isBlank(holderAttributeName))
			for (String name : holderAttributeName.split(","))
				if (!StringUtils.isBlank(name))
					holderAttributeNameList.add(name.trim());
	}
}