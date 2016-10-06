package com.ihidea.component.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.util.StringUtilsEx;

public class XSSFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);
	}

	class XSSRequestWrapper extends HttpServletRequestWrapper {

		public XSSRequestWrapper(HttpServletRequest servletRequest) {
			super(servletRequest);
		}

		@Override
		public String[] getParameterValues(String parameter) {

			String[] values = super.getParameterValues(parameter);

			if (values == null) {
				return null;
			}

			int count = values.length;
			String[] encodedValues = new String[count];
			for (int i = 0; i < count; i++) {
				encodedValues[i] = clean(values[i]);
			}

			return encodedValues;
		}

		@Override
		public String getParameter(String parameter) {
			String value = super.getParameter(parameter);

			return clean(value);
		}

		@Override
		public String getHeader(String name) {
			String value = super.getHeader(name);
			return clean(value);
		}

		private String clean(String value) {

			if (StringUtils.isNotBlank(value)) {
				value = StringUtilsEx.escapeXss(value);
			}

			return value;
		}
	}

}