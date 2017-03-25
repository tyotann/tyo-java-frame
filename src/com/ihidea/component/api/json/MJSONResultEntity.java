package com.ihidea.component.api.json;

import java.io.Serializable;

import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;

/**
 * controller调用返回对象
 * 
 * @author TYOTANN
 */
public class MJSONResultEntity implements Serializable {

	private static final long serialVersionUID = -115509026625589704L;

	// 正常
	public final static String RESULT_SUCCESS = "200";

	// 警告
	public final static String RESULT_WARN = "300";

	// 业务逻辑异常
	public final static String RESULT_LOGIC_ERROR = "400";

	// 系统异常
	public final static String RESULT_EXCEPTION = "401";

	// Session 超时
	public final static String RESULT_SESSION_ERROR = "402";

	// 签名信息异常
	public final static String REQUEST_SIGN_ERROR = "403";

	// 签名时间异常
	public final static String REQUEST_SIGN_TIME_ERROR = "404";

	/**
	 * <pre>
	 * 请求结果状态位
	 * </pre>
	 */
	private String code = MJSONResultEntity.RESULT_SUCCESS;

	/**
	 * 警告或者异常信息，正常无信息
	 */
	private String text;

	/**
	 * 正常结束返回值
	 */
	private Object data;

	private PageLimit pageLimit;

	/**
	 * 分页信息
	 * 
	 * @return
	 */
	public PageLimit getPageLimit() {

		if (pageLimit == null) {
			pageLimit = PageLimitHolderFilter.getContext();
		}

		return pageLimit.limited() ? pageLimit : null;
	}

	public void setPageLimit(PageLimit pageLimit) {
		this.pageLimit = pageLimit;
	}

	// ------------------------------构造函数------------------------------//
	public MJSONResultEntity() {
	}

	public MJSONResultEntity(String code) {
		this.code = code;
	}

	public MJSONResultEntity(Object data) {
		this.data = data;
	}

	public MJSONResultEntity(String code, String text) {
		this.code = code;
		this.text = text;
	}

	public MJSONResultEntity(String code, String text, Object data) {
		this.code = code;
		this.text = text;
		this.data = data;
	}

	// ------------------------------构造函数------------------------------//

	public String getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}

	public String getText() {
		return text;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setText(String text) {
		this.text = text;
	}

}
