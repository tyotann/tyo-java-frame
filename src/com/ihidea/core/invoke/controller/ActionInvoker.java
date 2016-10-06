package com.ihidea.core.invoke.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.base.CoreController;
import com.ihidea.core.invoke.controller.call.CallController;
import com.ihidea.core.invoke.controller.call.CallServiceFactory;
import com.ihidea.core.support.cache.CacheSupport;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.support.servlet.ServletHolderFilter;
import com.ihidea.core.support.servlet.ServletInfo;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

/**
 * 常用的Controller调用，包括页面请求，AJAX请求，页面跳转...
 * 
 * @author TYOTANN
 */
@Controller
public class ActionInvoker extends CoreController {

	@RequestMapping(value = "/**/*.htm", method = RequestMethod.GET)
	public String htm(HttpServletRequest request, ModelMap model) {

		_action(request, model);

		String path = request.getServletPath();
		return StringUtils.substring(path, 1, -4);
	}

	/**
	 * <pre>
	 * 页面请求
	 * 依赖apache模板工具：velocity.jar
	 * 传入的参数中必须有path的变量，用来跳转
	 * 
	 * e.g: get:/action.do?path=dkcl/bzjgl/bzjPre&amp;...(get参数)
	 * 		post:/action.do?path=dkcl/bzjgl/bzjPre
	 * </pre>
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/action.do")
	public String action(HttpServletRequest request, ModelMap model) {
		return _action(request, model);
	}

	@Deprecated
	@RequestMapping("/wap.do")
	public String wap(HttpServletRequest request, ModelMap model) {
		return _action(request, model);
	}

	private String _action(HttpServletRequest request, ModelMap model) {

		// 请求页面的模板位置
		String path = null;

		// 如果是include,forward,error页面，不会经过过滤器，所以此处就没值
		if (ServletHolderFilter.getContext() != null) {

			Map<String, Object> param = ServletHolderFilter.getContext().getParamMap();

			path = (String) param.get("path");

			if (logger.isDebugEnabled()) {
				logger.debug("请求路径 = " + path);
			}

			// 如果启用CDN，则放入CDN，否则使用原有路径
			if (StringUtils.isNotBlank(CoreConstants.getProperty("cdn.url"))) {
				model.addAttribute("frameContextPath", CoreConstants.getProperty("cdn.url"));
			} else {
				model.addAttribute("frameContextPath", request.getContextPath());
			}

			// 页面上一些常用的变量,主要是时间
			{
				// session信息
				model.addAttribute("session", SessionContext.getSessionInfo());

				// 应用实例ID
				model.addAttribute("appid", LocalAttributeHolder.getContext().get("appid"));

				// 应用实例名称
				model.addAttribute("appName", CoreConstants.appName);
			}

			// 设置传入参数
			for (String paramName : param.keySet()) {

				// 进行xss过滤
				if (param.get(paramName) != null && param.get(paramName) instanceof String) {
					model.addAttribute(paramName, StringUtilsEx.escapeXss((String) param.get(paramName)));
				} else {
					model.addAttribute(paramName, param.get(paramName));
				}

			}

		} else {
			path = request.getParameter("path");

			// 如果是servlet异常
			if (request.getAttribute("javax.servlet.error.exception") != null) {
				Exception e = (Exception) request.getAttribute("javax.servlet.error.exception");
				model.addAttribute("exceptionMessage", ExceptionUtils.getRootCauseMessage(e));
				model.addAttribute("exceptionStackTrace", ExceptionUtils.getRootCause(e) == null ? ExceptionUtils.getStackTrace(e)
						: ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e)));
			}
		}
		return path;
	}

	/**
	 * <pre>
	 * ajax直接请求Service，与JS的AE.ServiceEx配合使用
	 * service入口参数为POJO,String
	 * </pre>
	 * 
	 * @param serviceName
	 * @param strings
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/invoke.do")
	public void invoke(HttpServletResponse response) throws Exception {

		ServletInfo servletInfo = ServletHolderFilter.getContext();

		String serviceName = servletInfo.getServiceName();

		String invokeSeqno = servletInfo.getInvokeSeqno();

		Map<String, Object> params = servletInfo.getParamMap();

		if (logger.isDebugEnabled() && SessionContext.getSessionInfo() != null) {

			if (serviceName.indexOf("@ESB") == -1) {
				logger.debug("用户：" + SessionContext.getSessionInfo().getUserName() + ",进行本地请求:" + serviceName + ",参数：" + params);
			} else {
				logger.debug("用户：" + SessionContext.getSessionInfo().getUserName() + ",进行ESB请求:" + serviceName + ",参数：" + params);
			}
		}

		ResultEntity result = null;

		try {

			// 重复提交检查
			{
				String sessionId = SessionContext.getSessionInfo() == null ? StringUtils.EMPTY
						: SessionContext.getSessionInfo().getSessionId();

				if (CacheSupport.get("retryInvokeCache", sessionId + serviceName + invokeSeqno, String.class) != null) {
					throw new ServiceException("请求重复提交!");
				} else {
					CacheSupport.put("retryInvokeCache", sessionId + serviceName + invokeSeqno, invokeSeqno);
				}
			}

			// 多个页面重复登录后，因为IE共享session导致权限错乱
			if (StringUtils.isNotBlank(ServletHolderFilter.getContext().getFrameParamMap().get("logonUserId"))) {
				if (!ServletHolderFilter.getContext().getFrameParamMap().get("logonUserId")
						.equals(SessionContext.getSessionInfo().getUserId())) {
					throw new ServiceException(-98, "多个页面重复登录,需要重新刷新页面!");
				}
			}

			result = invokeService(serviceName, params);
		} catch (Exception e) {

			Throwable ex = e.getCause() == null ? e : ExceptionUtils.getRootCause(e);

			result = new ResultEntity();

			String errMsg = e.getCause() == null ? e.getMessage() : ExceptionUtils.getRootCauseMessage(e);

			// -1:业务系统,-2:系统异常
			if (ex instanceof ServiceException) {

				if (((ServiceException) ex).getCode() != null) {
					result.setCode(String.valueOf(((ServiceException) ex).getCode()));
				} else {
					result.setCode("-1");
				}

				errMsg = errMsg.replaceAll("ServiceException: ", "");
			} else {
				result.setCode("-2");
				logger.error(result.getText(), e.getCause() == null ? e : ExceptionUtils.getRootCause(e));
			}

			result.setText(errMsg);
		}

		// 如果是AJAX请求，response中返回值
		if (ServletInfo.ACTION_MODE_AJAX.equals(ServletHolderFilter.getContext().getActionMode())) {

			// 如果时跨域访问,则使用renderJsonp包装
			if ("true".equals(servletInfo.getFrameParamMap().get("crossDomain"))) {
				ServletUtilsEx.renderJsonp(response, result);
			} else {
				ServletUtilsEx.renderJson(response, result);
			}

		}
	}

	private ResultEntity invokeService(String serviceName, Map<String, Object> params) throws Exception {

		// 调用服务名判断
		if (StringUtils.isBlank(serviceName)) {
			throw new ServiceException("请输入需要请求的服务名!");
		}

		// 取得服务解析器
		CallController serviceHandle = CallServiceFactory.getInstance(serviceName);

		// 如果传入参数是String类型，则做xss过滤
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				if (params.get(key) != null && params.get(key) instanceof String) {
					params.put(key, StringUtilsEx.escapeXss((String) params.get(key)));
				}
			}
		}

		// 服务调用
		return serviceHandle.call(serviceName, params);
	}

}
