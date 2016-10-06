package com.ihidea.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class CookieUtil {

	/**
	 * 返回Cookie值
	 * @param request
	 * @param cName
	 * @param value
	 * @return
	 */
	public static String getValue(HttpServletRequest request, String cName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cName.equals(cookie.getName())) {
					try {
						return URLDecoder.decode(cookie.getValue(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
					}
				}
			}
		}
		return null;
	}

	/**
	 * get Cookie By cookieName
	 * @param request
	 * @param cName
	 * @return
	 */
	public static Cookie getCookie(HttpServletRequest request, String cName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie != null && cName.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 添加cookie
	 * @param response
	 * @param namecookie的key值
	 * @param valuecookie的value值
	 * @param pathcookie的路径
	 * @param domaincookie的域
	 * @param timeoutcookie的过期时间
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, String domain, String path, Integer timeout) {

		Cookie cookie = null;
		try {
			value = StringUtils.isNotBlank(value) ? URLEncoder.encode(value, "UTF-8") : value;
			cookie = new Cookie(name, value);
		} catch (UnsupportedEncodingException e) {
		}

		if (path == null) {
			path = "/";
		}

		if (StringUtils.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}

		cookie.setPath(path);

		if (timeout != null) {
			cookie.setMaxAge(timeout);
		}

		response.addCookie(cookie);
	}

	public static void addCookie(HttpServletResponse response, String name, String value, String domain, String path) {
		addCookie(response, name, value, domain, null);
	}

	public static void addCookie(HttpServletResponse response, String name, String value, String domain) {
		addCookie(response, name, value, domain, null, null);
	}

	/**
	 * 删除cookie
	 * @param request
	 * @param response
	 * @param namecookie的名称
	 */
	public static void delCookie(HttpServletRequest request, HttpServletResponse response, String name, String domain) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookies != null && (name).equals(cookie.getName())) {
				cookie.setValue(null);
				cookie.setMaxAge(0);
				response.addCookie(cookie);
				return;
			}
		}
	}

	public static void delCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		delCookie(request, response, name, ServletUtilsEx.getHostName(request));
	}

	/**
	 * 修改cookie的value值
	 * @param request
	 * @param response
	 * @param name
	 * @param value
	 */
	public static void updateCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookies != null && (name).equals(cookie.getName())) {
				addCookie(response, name, value, cookie.getPath(), cookie.getDomain(), cookie.getMaxAge());
				return;
			}
		}
	}
}