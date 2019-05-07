package com.ihidea.core.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletUtilsEx {

	private static Log logger = LogFactory.getLog(ServletUtilsEx.class);

	public static void renderJson(HttpServletResponse response, Object object) {
		ServletUtilsEx.render(response, JSONUtilsEx.serialize(object), "application/json;charset=UTF-8");
	}

	/**
	 * 跨域访问
	 * @param response
	 * @param object
	 */
	public static void renderJsonp(HttpServletResponse response, Object object) {
		ServletUtilsEx.render(response, "jsonpcallback", "text/plain;charset=UTF-8");
	}

	public static void renderJsonp(HttpServletResponse response, String callbackName, Object object) {

		if (StringUtils.isBlank(callbackName)) {
			callbackName = "jsonpcallback";
		}

		ServletUtilsEx.render(response, callbackName + "(" + JSONUtilsEx.serialize(object) + ")", "text/plain;charset=UTF-8");
	}

	public static void renderJsonp(HttpServletResponse response, Object object, String callbackName) {
		ServletUtilsEx.render(response, callbackName + "(" + JSONUtilsEx.serialize(object) + ")", "text/plain;charset=UTF-8");
	}

	public static void render(HttpServletResponse response, String text, String contentType) {
		try {
			response.setContentType(contentType);
			response.getWriter().write(text);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void renderText(HttpServletResponse response, String text) {
		try {
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write(text);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 得到客户端请求的服务器域名
	 * @param request
	 * @return
	 */
	public static String getHostName(HttpServletRequest request) {
		return request.getServerName();
	}

	/**
	 * 得到客户端请求的服务器地址
	 * @param request
	 * @return
	 */
	public static String getHostURL(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
	}

	/**
	 * 得到客户端请求的服务器地址，包含ContextPath
	 * @param request
	 * @return
	 */
	public static String getHostURLWithContextPath(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
	}
}
