package com.ihidea.core.support.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 把session信息放入threadLocal
 * @author TYOTANN
 */
public class ServletHolderFilter implements Filter {

	protected Log logger = LogFactory.getLog(getClass());

	private final static ThreadLocal<ServletInfo> threadLocal = new ThreadLocal<ServletInfo>();

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		try {
			setContext(new ServletInfo((HttpServletRequest) request, (HttpServletResponse) response));

			chain.doFilter(request, response);
		} finally {
			setContext(null);
		}
	}

	private static void setContext(ServletInfo sc) {
		threadLocal.set(sc);
	}

	public static ServletInfo getContext() {
		return threadLocal.get();
	}

	public void destroy() {
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
