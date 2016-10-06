package com.ihidea.component.api.proto;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StreamUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.ihidea.component.api.IAPIVerify;
import com.ihidea.component.api.MobileInfo;
import com.ihidea.component.api.OpenAPINoneVerify;
import com.ihidea.component.api.annotation.OpenAPI;
import com.ihidea.component.api.annotation.OpenAPIMethodProto;
import com.ihidea.component.api.json.MJSONResultEntity;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.exception.ServiceWarn;
import com.ihidea.core.support.local.LocalAttributeHolder;
import com.ihidea.core.util.ClassUtilsEx;
import com.ihidea.core.util.DES;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.ProtobufUtils;
import com.mdx.mobile.base.MRequest;
import com.mdx.mobile.base.Retn;

/**
 * 手机服务器端请求
 * 
 * @author TYOTANN
 */
public class MobileServerProtoServlet extends HttpServlet {

	private Log logger = LogFactory.getLog(MobileServerProtoServlet.class);

	// private Log slowLogger = LogFactory.getLog(ServiceSlowInterceptor.class);

	private Map<String, Method> mobileMethodMap = new HashMap<String, Method>();

	private Map<String, OpenAPIMethodProto> mobileMethodAnnoMap = new HashMap<String, OpenAPIMethodProto>();

	/**
	 * 初始化，扫描系统中所有的mobileMethod
	 */
	@Override
	public void init() throws ServletException {

		try {
			Map<String, Object> mobileClz = SpringContextLoader.getBeansWithAnnotation(OpenAPI.class);

			if (mobileClz != null) {
				for (Object clzObj : mobileClz.values()) {

					List<Method> methodList = ClassUtilsEx.getClassMethodByAnnotation(clzObj.getClass(), OpenAPIMethodProto.class);

					for (Method method : methodList) {

						OpenAPIMethodProto methodAnno = method.getAnnotation(OpenAPIMethodProto.class);

						String methodName = methodAnno.methodName();

						if (StringUtils.isBlank(methodName)) {
							methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
						}

						if (mobileMethodMap.containsKey(methodName) || mobileMethodAnnoMap.containsKey(methodName)) {
							throw new ServiceException("手机接口:" + methodName + "有重复定义,请检查代码!");
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

	@SuppressWarnings("rawtypes")
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Date startTime = new Date();

		boolean debug = "1".equals(request.getParameter("debug"));

		Retn.Msg_Retn.Builder returnBuilder = Retn.Msg_Retn.newBuilder();

		GeneratedMessage.Builder requestMethodResult = null;

		Map<String, Object> paramMap = null;

		String methodName = null;

		try {

			// 得到参数
			paramMap = getParam(request);

			// 移除调用函数名
			methodName = (String) paramMap.get("methodno");
			paramMap.remove("methodno");

			// 移除是否debug
			paramMap.remove("debug");

			OpenAPIMethodProto mobileMethod = mobileMethodAnnoMap.get(methodName);

			// 基本检查
			{
				if (mobileMethod == null) {
					throw new ServiceException("请求的接口:" + methodName + "在服务器端没有定义,请检查!");
				}

				// if (mobileMethod.hasDeviceId() && paramMap.get("deviceid") ==
				// null) {
				// throw new ServiceException("手机端没有传入设备编号!");
				// }
			}

			// 登录检查
			if (mobileMethod.verify() != OpenAPINoneVerify.class) {

				if (StringUtils.isBlank((String) paramMap.get("userid"))) {
					throw new ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR, "此功能需要事先登录,请先登录!");
				}

				if (StringUtils.isBlank((String) paramMap.get("verify"))) {
					throw new ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR, "此功能需要事先登录,请先登录!");
				}

				// 验证用户校验码
				{
					Class<? extends IAPIVerify> verifyMethod = mobileMethod.verify();

					// if (StringUtils.isBlank(verifyMethod)) {
					// throw new
					// ServiceException("请在配置文件application.properties中配置mobile.verify,用以设定手机登录校验服务,传入参数为appid,accountId,verify");
					// }
					//
					// String[] verifyMethodArray = verifyMethod.split("\\.");
					//
					// if (verifyMethodArray.length != 2) {
					// throw new
					// ServiceException("配置文件application.properties中配置的mobile.verify格式不对,格式为:服务名.方法名");
					// }

					Map<String, Object> params = new HashMap<String, Object>();
					params.put("appid", paramMap.get("appid"));
					params.put("accountId", paramMap.get("userid"));
					params.put("verify", paramMap.get("verify"));

					boolean isVerify = SpringContextLoader.getBean(verifyMethod).verify(params);

					if (!isVerify) {
						throw new ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR, "登录失败,请登录!");
					}

					// boolean isVerify = (Boolean)
					// ClassUtilsEx.invokeMethod(verifyMethodArray[0],
					// verifyMethodArray[1], params);
					//
					// if (!isVerify) {
					// throw new
					// ServiceException(MJSONResultEntity.RESULT_SESSION_ERROR,
					// "登录信息失效,请重新登录!");
					// }
				}
			}

			// 请求方法
			requestMethodResult = requestMethod(methodName, mobileMethod, paramMap);

			returnBuilder.setErrorCode(0);
			returnBuilder.setErrorMsg(StringUtils.EMPTY);

			// 写入response，debug=1的问题
			if (!debug && requestMethodResult != null) {
				returnBuilder.setRetnMessage(ByteString.copyFrom(ProtobufUtils.serialize(requestMethodResult)));

				// 这个返回参数android需要
				returnBuilder.setReturnMethod(methodName);
			}
		} catch (Exception e) {

			Throwable rootThrowable = ExceptionUtils.getRootCause(e) == null ? e : ExceptionUtils.getRootCause(e);

			if (rootThrowable instanceof ServiceException) {

				logger.debug(rootThrowable.getMessage());

				String errorCode = ((ServiceException) rootThrowable).getCode();

				if (StringUtils.isNotBlank(errorCode)) {
					returnBuilder.setErrorCode(Integer.valueOf(errorCode));
				} else {
					returnBuilder.setErrorCode(Integer.valueOf(MJSONResultEntity.RESULT_LOGIC_ERROR));
				}

			} else if (rootThrowable instanceof ServiceWarn) {

				logger.debug(rootThrowable.getMessage());
				returnBuilder.setErrorCode(101);
			} else {

				logger.error(rootThrowable.getMessage(), rootThrowable);
				returnBuilder.setErrorCode(109);
			}

			returnBuilder.setErrorMsg(rootThrowable.getMessage() == null ? String.valueOf(rootThrowable) : rootThrowable.getMessage());
		} finally {

			ServletOutputStream sos = response.getOutputStream();

			try {

				// 写入response，debug=1的问题
				if (!debug) {
					sos.write(new DES().desEncrypt(ProtobufUtils.serialize(returnBuilder)));
				} else {

					response.setContentType("text/plain; charset=utf-8");
					response.setCharacterEncoding("UTF-8");

					Map<String, Object> jsonShow = new HashMap<String, Object>();
					jsonShow.put("errorCode", returnBuilder.getErrorCode());
					jsonShow.put("errorMsg", returnBuilder.getErrorMsg());
					jsonShow.put("retnMessage", requestMethodResult);

					sos.write(JSONUtilsEx.serialize(jsonShow).getBytes("UTF-8"));
				}
				sos.flush();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (sos != null) {
					sos.close();
				}

				Date endTime = new Date();

				long time = endTime.getTime() - startTime.getTime();
				if (time > 500) {
					try {
						// slowLogger.info("处理时间:" + time + "毫秒，手机接口[" +
						// methodName + "]属于系统缓慢时间设定。参数"
						// + (paramMap == null ? "无参数" :
						// JSONUtilsEx.serialize(paramMap)));
					} catch (Exception e) {
						// 如果传入参数是proto且其中有深层次的对象,则在json序列化时会有异常
					}
				}
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

		Map<String, Object> paramMap = new HashMap<String, Object>();

		String methodName = null;

		// 得到参数
		for (String paramName : (Set<String>) request.getParameterMap().keySet()) {

			if (StringUtils.isNotBlank(paramName)) {

				String paramValue = request.getParameter(paramName);

				if ("methodno".equals(paramName)) {
					methodName = paramValue;
				}

				paramMap.put(paramName, paramValue);
			}
		}

		// 得到protobuf中的参数，android中部分参数放入protobuf,ios中直接传入参数
		{
			byte[] protobuf = StreamUtils.copyToByteArray(request.getInputStream());

			MRequest.Msg_Request.Builder requestProtobuf = (MRequest.Msg_Request.Builder) ProtobufUtils.deserialize(
					new DES().desDecrypt(protobuf), MRequest.Msg_Request.class);

			// 读取protobuf中的数据
			for (int i = 0; i < requestProtobuf.getPostsCount(); ++i) {
				paramMap.put(requestProtobuf.getPosts(i).getName(), requestProtobuf.getPosts(i).getValue());
			}

			// 如果请求中含有protobuf,根据requestClz来转型
			if (requestProtobuf.getRequestMessage() != null && requestProtobuf.getRequestMessage().size() > 0) {

				OpenAPIMethodProto mobileMethod = mobileMethodAnnoMap.get(methodName);

				if (mobileMethod != null) {
					Class requestClz = mobileMethod.requestClz();

					paramMap.put("requestProto", ProtobufUtils.deserialize(requestProtobuf.getRequestMessage(), requestClz));
				}
			}
		}

		// 分页处理
		if (paramMap.containsKey("page")) {

			if (StringUtils.isBlank((String) paramMap.get("page"))) {
				paramMap.put("page", "1");
			}

			if (!paramMap.containsKey("pagecount") && !paramMap.containsKey("limit")) {
				throw new ServiceException("参数列表中存在参数page时,请同时传入分页参数:pagecount或者limit!");
			}

			if (StringUtils.isBlank((String) paramMap.get("pagecount"))) {
				paramMap.put("pagecount", "20");
			}
		}

		// 建立mobileInfo对象
		MobileInfo mobileInfo = new MobileInfo((String) paramMap.get("userid"), (String) paramMap.get("deviceid"),
				(String) paramMap.get("appid"), (String) paramMap.get("api_platform"), (String) paramMap.get("api_version"));
		paramMap.put("mobileInfo", mobileInfo);

		// 塞入线程变量
		LocalAttributeHolder.getContext().put("appid", paramMap.get("appid"));
		LocalAttributeHolder.getContext().put("userid", paramMap.get("userid"));
		LocalAttributeHolder.getContext().put("mobileInfo", mobileInfo);

		return paramMap;
	}

	/**
	 * 请求服务内容并反射后到protobuf
	 * 
	 * @param methodName
	 * @param mobileMethod
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private GeneratedMessage.Builder requestMethod(String methodName, OpenAPIMethodProto mobileMethod, Map<String, Object> paramMap)
			throws Exception {

		GeneratedMessage.Builder buildResult = null;

		// 参数反射层protobuf

		// 请求服务
		Method method = mobileMethodMap.get(methodName);
		Object methodResult = ClassUtilsEx.invokeMethod(method.getDeclaringClass().getSimpleName(), method.getName(), paramMap);

		// 如果没有定义responseClz,则不进行反射
		if (mobileMethod.responseClz() != GeneratedMessage.class) {

			// 有返回值，返回不为空
			if (methodResult != null) {

				// 把service的结果反射到对应的protobuf类中,如果服务返回就是protobuf的话，直接返回
				if (methodResult instanceof GeneratedMessage.Builder) {
					buildResult = (GeneratedMessage.Builder) methodResult;
				} else {
					buildResult = ProtobufUtils.deserialize(methodResult, mobileMethod.responseClz());
				}
			} else {

				// 定义了返回值，但实际返回为空，需要返回new出来的结果
				Method responseNewmethod = mobileMethod.responseClz().getMethod("newBuilder");
				buildResult = (GeneratedMessage.Builder) responseNewmethod.invoke(mobileMethod.responseClz());
			}
		}

		return buildResult;
	}
}
