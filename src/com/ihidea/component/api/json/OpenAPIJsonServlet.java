package com.ihidea.component.api.json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.component.api.IAPIVerify;
import com.ihidea.component.api.MobileInfo;
import com.ihidea.component.api.OpenAPINoneVerify;
import com.ihidea.component.api.annotation.OpenAPI;
import com.ihidea.component.api.annotation.OpenAPIMethod;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.exception.ServiceWarn;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ServletUtilsEx;
import com.ihidea.core.util.StringUtilsEx;
import com.ihidea.core.util.SystemUtilsEx;

/**
 * 手机服务器端请求
 * 
 * @author TYOTANN
 */
public class OpenAPIJsonServlet extends HttpServlet {

	private static final long serialVersionUID = -7813617057723596671L;

	private static Logger logger = LoggerFactory.getLogger(OpenAPIJsonServlet.class);

	private Map<String, Method> mobileMethodMap = new HashMap<String, Method>();

	private Map<String, OpenAPIMethod> mobileMethodAnnoMap = new HashMap<String, OpenAPIMethod>();

	/**
	 * 初始化，扫描系统中所有的mobileMethod
	 */
	@Override
	public void init() throws ServletException {

		try {
			Map<String, Object> openClz = SpringContextLoader.getBeansWithAnnotation(OpenAPI.class);

			if (openClz != null) {
				for (Object clzObj : openClz.values()) {

					List<Method> methodList = ClassUtilsEx.getClassMethodByAnnotation(clzObj.getClass(), OpenAPIMethod.class);

					for (Method method : methodList) {

						OpenAPIMethod methodAnno = method.getAnnotation(OpenAPIMethod.class);

						String methodName = methodAnno.methodName();

						if (StringUtils.isBlank(methodName)) {
							methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
						}

						if (mobileMethodMap.containsKey(methodName) || mobileMethodAnnoMap.containsKey(methodName)) {
							throw new ServiceException("API接口:" + methodName + "有重复定义,请检查代码!");
						} else {
							mobileMethodMap.put(methodName, method);
							mobileMethodAnnoMap.put(methodName, methodAnno);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServletException(e.getMessage(), e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		MJSONResultEntity result = new MJSONResultEntity();

		// 得到参数
		Map<String, Object> paramMap = null;

		// 函数名
		String methodName = null;

		String jsonpcallback = null;

		try {

			// 得到参数
			paramMap = getParam(request);

			// 如果在getParam中出现异常,则不会返回跨域脚本
			jsonpcallback = (String) paramMap.get("jsonpcallback");

			if (StringUtils.isBlank(request.getPathInfo())) {
				throw new ServiceException("请求的接口格式错误");
			}

			// 移除调用函数名
			methodName = request.getPathInfo().substring(1);

			paramMap.put("FRAMEmethodName", methodName);

			OpenAPIMethod mobileMethod = mobileMethodAnnoMap.get(methodName);

			// 基本检查
			{
				if (mobileMethod == null) {
					throw new ServiceException("请求的接口:" + methodName + "在服务器端没有定义,请检查!");
				}
			}

			// 校验检查
			if (mobileMethod.verify() != OpenAPINoneVerify.class) {

				IAPIVerify verifyMethod = SpringContextLoader.getBean(mobileMethod.verify());

				if (verifyMethod != null) {
					boolean isVerify = verifyMethod.verify(paramMap);

					if (!isVerify) {
						throw new ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR, "登录失败,请登录!");
					}
				} else {
					throw new ServiceException("未找到定义的类:" + mobileMethod.verify());
				}
			}

			// 请求方法
			result.setData(requestMethod(methodName, mobileMethod, paramMap));

		} catch (Exception e) {

			Throwable rootThrowable = ExceptionUtils.getRootCause(e) == null ? e : ExceptionUtils.getRootCause(e);

			if (rootThrowable instanceof ServiceException) {
				logger.debug(rootThrowable.getMessage());

				ServiceException se = ((ServiceException) rootThrowable);

				if (StringUtils.isNotBlank(se.getCode())) {
					result.setCode(se.getCode());
				} else {
					result.setCode(MJSONResultEntity.RESULT_LOGIC_ERROR);
				}

				if (se.getData() != null) {
					result.setData(se.getData());
				}
			} else if (rootThrowable instanceof ServiceWarn) {
				logger.debug(rootThrowable.getMessage());
				result.setCode(MJSONResultEntity.RESULT_WARN);
			} else {
				logger.error(rootThrowable.getMessage(), rootThrowable);
				result.setCode(MJSONResultEntity.RESULT_EXCEPTION);
			}

			result.setText(rootThrowable.getMessage() == null ? String.valueOf(rootThrowable) : rootThrowable.getMessage());
		} finally {

			// 如果需要跨域访问,则支持跨域 TODO 后期需要设置IP白名单
			if (StringUtils.isNotBlank(jsonpcallback)) {
				ServletUtilsEx.renderJsonp(response, StringUtilsEx.escapeXss(jsonpcallback), result);
			} else {
				ServletUtilsEx.renderJson(response, result);
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	public void destroy() {
		mobileMethodMap.clear();
		mobileMethodAnnoMap.clear();
	}

	/**
	 * 得到request中的参数
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getParam(HttpServletRequest request) throws Exception {

		// web中接口请求，用jquery的ajax post参数后发现如果参数中有多个且有数组，则参数名是xxx[1]这样的格式
		String paramJsonStr = request.getParameter("FRAMEparams");

		// 得到参数
		Map<String, Object> paramMap = null;

		if (StringUtils.isNotBlank(paramJsonStr)) {
			paramMap = JSONUtilsEx.deserialize(paramJsonStr, Map.class);
		} else {

			paramMap = new HashMap<String, Object>();

			for (String paramName : (Set<String>) request.getParameterMap().keySet()) {

				if (StringUtils.isNotBlank(paramName)) {
					paramMap.put(paramName, request.getParameter(paramName));
				}
			}
		}

		// 分页处理
		if (paramMap.containsKey("page")) {

			if (!paramMap.containsKey("pagecount")) {
				throw new ServiceException("参数列表中存在参数page时,请同时传入分页参数:pagecount!");
			}

			// 手机端如果需要总页数,传入totalCount=null，如果不传则代表不需要总页数信息）(设置为0，这样不会再算一遍count)
			Integer totalCount = null;
			if (paramMap.get("totalcount") != null) {
				totalCount = Integer.valueOf(String.valueOf(paramMap.get("totalcount")));
			}

			PageLimitHolderFilter.setContext(Integer.valueOf(String.valueOf(paramMap.get("page"))),
					Integer.valueOf(String.valueOf(paramMap.get("pagecount"))), totalCount);
			PageLimitHolderFilter.getContext().setLimited(true);
		}

		String appid = StringUtils.isNotBlank(CoreConstants.getProperty("application.appid"))
				? CoreConstants.getProperty("application.appid") : (String) paramMap.get("appid");

		// 建立mobileInfo对象
		MobileInfo mobileInfo = new MobileInfo((String) paramMap.get("userId"), (String) paramMap.get("deviceid"), appid);
		paramMap.put("mobileInfo", mobileInfo);

		// 塞入线程变量
		LocalAttributeHolder.getContext().put("appid", appid);
		LocalAttributeHolder.getContext().put("userid", paramMap.get("userId"));
		LocalAttributeHolder.getContext().put("mobileInfo", mobileInfo);
		LocalAttributeHolder.getContext().put("ipAddress", SystemUtilsEx.getClientIpSingle(request));

		if (!StringUtils.isBlank(request.getHeader("user-agent"))) {
			String userAgent = request.getHeader("user-agent").toLowerCase();

			String platform = "mobile";

			if (userAgent.indexOf("iphone") > -1) {
				platform = "iphone";
			} else if (userAgent.indexOf("ipad") > -1) {
				platform = "ipad";
			} else if (userAgent.indexOf("android") > -1 || userAgent.indexOf("apache-httpclient") > -1) {
				platform = "android";
			}
			LocalAttributeHolder.getContext().put("platform", platform);
		}

		// 如果传入参数是String类型，则做xss过滤
		for (String key : paramMap.keySet()) {
			if (paramMap.get(key) != null && paramMap.get(key) instanceof String) {
				paramMap.put(key, StringUtilsEx.escapeXss((String) paramMap.get(key)));
			}
		}

		return paramMap;
	}

	/**
	 * 请求服务内容并反射后到protobuf
	 * 
	 * @param methodName
	 * @param openMethod
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private Object requestMethod(String methodName, OpenAPIMethod openMethod, Map<String, Object> paramMap) throws Exception {

		// 请求服务
		Method method = mobileMethodMap.get(methodName);

		// 开始支持分页，之前的信息查詢不需要分頁
		PageLimitHolderFilter.getContext().setLimited(false);

		return ClassUtilsEx.invokeMethod(method.getDeclaringClass().getSimpleName(), method.getName(), paramMap);
	}

}
