package com.ihidea.core.support.servlet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihidea.component.jcaptcha.ImageServlet;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.BeanUtilsEx;

public class ServletInfo {

	private final static Logger logger = LoggerFactory.getLogger(ServletInfo.class);

	private HttpServletRequest request;

	private HttpServletResponse response;

	// 请求参数
	private Map<String, Object> paramMap = new HashMap<String, Object>();

	// 请求的过长的clob参数,暂不支持数组的clob
	private Map<String, String> clobParamMap = new HashMap<String, String>();

	// ----------------------------------框架参数---------------------------------//

	// 框架参数,所有请求参数名以"FRAME_PARAM_HEAD"变量开头的即为框架参数
	private Map<String, String> frameParamMap = new HashMap<String, String>();

	// 框架参数标识,
	private static final String FRAME_PARAM_HEAD = "FRAME";

	// 请求服务名(invoke.do使用)
	public String getServiceName() {
		return frameParamMap.get("serviceName");
	}

	// 请求序列号(invoke.do使用，防止重复提交)
	public String getInvokeSeqno() {
		return frameParamMap.get("invokeSeqno");
	}

	// 页面类型[0：普通页面(默认);2：下载请求页面]
	public String getActionMode() {
		return frameParamMap.get("actionMode") == null ? ACTION_MODE_AJAX : frameParamMap.get("actionMode");
	}

	// ----------------------------------框架参数---------------------------------//

	// 普通的AJAX请求
	public static final String ACTION_MODE_AJAX = "0";

	// 页面下载
	public static final String ACTION_MODE_DOWNLOAD = "2";

	public ServletInfo(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;

		// 得到参数
		createParamMap();
	}

	/**
	 * 创建Servlet的参数Map
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> createParamMap() {

		if (isMultipart()) {

			// 含有上传文件
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);

			List<FileItem> fileItemList = null;

			try {
				fileItemList = upload.parseRequest(request);
			} catch (Exception e) {
				throw new ServiceException(e);
			}

			for (FileItem fileItem : fileItemList) {

				if (fileItem.isFormField()) {
					try {
						paramMap.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new ServiceException(e);
					}
				} else if (fileItem.getSize() > 0) {
					if (!paramMap.containsKey(fileItem.getFieldName())) {
						paramMap.put(fileItem.getFieldName(), new ArrayList<FileItem>());
					}
					((List<FileItem>) paramMap.get(fileItem.getFieldName())).add(fileItem);
				}
			}

			// 如果是servlet3.0
			try {
				Collection<Part> parts = request.getParts();
				if (parts != null) {
					for (Part part : parts) {

						String filename = getServlet3FileName(part);

						String fieldName = part.getName();

						// 如果上传了文件
						if (StringUtils.isNotBlank(filename)) {
							if (!paramMap.containsKey(fieldName)) {
								paramMap.put(fieldName, new ArrayList<Object[]>());
							}

							((List<Object[]>) paramMap.get(fieldName))
									.add(new Object[] { filename, IOUtils.toByteArray(part.getInputStream()) });
						}
					}
				}
			} catch (Exception e) {
				logger.error("文件上传解析出现异常:{}", new Object[] { e.getMessage(), e });
			}
		}

		// 把传入的参数放入paramMap(json or get 方式)
		{
			Enumeration paramNames = request.getParameterNames();

			if (paramNames != null) {
				while (paramNames.hasMoreElements()) {

					String paramName = (String) paramNames.nextElement();

					if (!StringUtils.isBlank(paramName)) {

						String paramValue = request.getParameter(paramName);

						if (paramName.equals(FRAME_PARAM_HEAD + "params")) {

							// 框架参数:AE.ServiceEx提交的参数
							Map result = null;

							// 如果提交的参数异常,则无视
							try {
								result = BeanUtilsEx.json2Map(paramValue);

								for (Object key : result.keySet()) {
									paramMap.put(String.valueOf(key), result.get(key));
								}
							} catch (Exception e) {
							}

						} else if (paramName.indexOf(FRAME_PARAM_HEAD) == 0) {
							frameParamMap.put(paramName.substring(FRAME_PARAM_HEAD.length()), paramValue);
						} else {
							paramMap.put(paramName, paramValue);
						}
					}
				}
			}
		}

		return paramMap;
	}

	/**
	 * 火狐或者google浏览器下：tempArr1={form-data,name="file",filename="snmp4j--api.zip"}
	 * IE浏览器下：tempArr1={form-data,name="file",filename="E:\snmp4j--api.zip"}
	 */
	private String getServlet3FileName(Part part) {

		String[] arr = part.getHeader("content-disposition").replaceAll("\"", "").split(";");

		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				if (StringUtils.startsWithIgnoreCase(StringUtils.trim(arr[i]), "filename=")) {
					return StringUtils.substring(arr[i],
							StringUtils.lastIndexOf(arr[i], "\\") > 0 ? (StringUtils.lastIndexOf(arr[i], "\\") + 1) : 10);
				}
			}
		}

		return null;
	}

	/**
	 * 得到提交的参数
	 * 
	 * @return
	 */
	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	/**
	 * 得到提交的过长的CLOB参数
	 * 
	 * @return
	 */
	public Map<String, String> getClobParamMap() {
		return clobParamMap;
	}

	/**
	 * 得到框架参数
	 * 
	 * @return
	 */
	public Map<String, String> getFrameParamMap() {
		return frameParamMap;
	}

	/**
	 * 得到ContentPath
	 * 
	 * @return
	 */
	public String getContentPath() {
		return request.getContextPath();
	}

	public String getJcaptcha() {
		return (String) request.getSession().getAttribute(ImageServlet.JCAPTCHA_NAME);
	}

	public String getServerName() {
		return request.getServerName();
	}

	public String getServerPort() {
		return String.valueOf(request.getServerPort());
	}

	public String getServerIp() {
		return getServerName() + ":" + getServerPort();
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public boolean isMultipart() {
		return !StringUtils.isBlank(request.getContentType()) && request.getContentType().indexOf("multipart/form-data") > -1;
	}

}
