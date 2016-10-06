package com.ihidea.core.support.pageLimit;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 分页过滤器
 * @author TYOTANN
 */
public class PageLimitHolderFilter implements Filter {

	private final static ThreadLocal<PageLimit> threadLocal = new ThreadLocal<PageLimit>();

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		try {
			threadLocal.set(new PageLimit((HttpServletRequest) request));

			chain.doFilter(request, response);
		} finally {
			threadLocal.set(null);
		}
	}

	public static PageLimit getContext() {
		return threadLocal.get();
	}

	public static void setContext(Integer currentPageNo, Integer pageLength, Integer totalCount) {
		threadLocal.set(new PageLimit(currentPageNo, pageLength, totalCount));
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

}
