package com.ihidea.core.support.session;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.util.CookieUtil;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

public class SessionContext implements Serializable {

	private final static String SESSION_KEY = "com.ihidea.security.securityContext";

	// TODO 不成熟,cookie在每个请求中带入信息太多
	private final static String COOKIE_KEY = "com.ihidea.sessionInfo";

	private final static String COOKIE_CHECK_ID = "com.ihidea.sessionInfo.id";

	private final static String COOKIE_CHECK_TOKEN = "com.ihidea.sessionInfo.token";

	public static void bindSession(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo) {

		// 如果是cookies,设置cookies
		if (CoreConstants.SESSION_ENABLE_COOKIES) {

			// 设置cookie token 无法解决伪造token的问题
			String checkId = StringUtilsEx.getUUID();
			CookieUtil.addCookie(response, COOKIE_CHECK_ID, checkId, ServletUtilsEx.getHostName(request));
			CookieUtil.addCookie(response, COOKIE_CHECK_TOKEN, StringUtilsEx.md5(checkId, "xc"), ServletUtilsEx.getHostName(request));

			CookieUtil.addCookie(response, COOKIE_KEY, JSONUtilsEx.serialize(sessionInfo), ServletUtilsEx.getHostName(request));
		} else {
			request.getSession().setAttribute(SessionContext.SESSION_KEY, sessionInfo);
		}
	}

	/**
	 * 移除session
	 * @param sessionId
	 */
	public static void removeSession(HttpServletRequest request, HttpServletResponse response) {
		if (CoreConstants.SESSION_ENABLE_COOKIES) {
			CookieUtil.delCookie(request, response, COOKIE_KEY);
		} else {
			request.getSession().invalidate();
		}
	}

	/**
	 * 得到当前用户session
	 * @return
	 */
	public static SessionInfo getSessionInfoByReq(HttpServletRequest request) {

		if (CoreConstants.SESSION_ENABLE_COOKIES) {
			String cookieStr = CookieUtil.getValue(request, COOKIE_KEY);

			String checkId = CookieUtil.getValue(request, COOKIE_CHECK_ID);
			String checkToken = CookieUtil.getValue(request, COOKIE_CHECK_TOKEN);

			if (StringUtils.isNotBlank(cookieStr) && StringUtils.isNotBlank(checkId) && StringUtils.isNotBlank(checkToken)
					&& checkToken.equals(StringUtilsEx.md5(checkId, "xc"))) {
				return JSONUtilsEx.deserialize(cookieStr, SessionInfo.class);
			} else {
				return null;
			}
		} else {
			return (SessionInfo) request.getSession().getAttribute(SessionContext.SESSION_KEY);
		}

	}

	/**
	 * 得到过滤器拦截的session信息
	 * @return
	 */
	public static SessionInfo getSessionInfo() {
		return SessionHolderFilter.getContext();
	}

}
