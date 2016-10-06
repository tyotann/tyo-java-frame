package com.ihidea.core.invoke.controller;

import java.io.Serializable;

import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;

/**
 * controller调用返回对象
 * @author TYOTANN
 */
public class ResultEntity implements Serializable {

	// 正常
	public final static String RESULT_SUCCESS = "0";

	// 警告
	public final static String RESULT_WARN = "1";

	// 业务逻辑异常
	public final static String RESULT_LOGIC_ERROR = "-1";

	// 系统异常
	public final static String RESULT_EXCEPTION = "-2";

	// Session 超时
	public final static String RESULT_SESSION_ERROR = "-98";

	/**
	 * <pre>
	 * 请求结果状态位
	 * </pre>
	 */
	private String code = ResultEntity.RESULT_SUCCESS;

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
	public ResultEntity() {
	}

	public ResultEntity(String code) {
		this.code = code;
	}

	public ResultEntity(Object data) {
		this.data = data;
	}

	public ResultEntity(String code, String text) {
		this.code = code;
		this.text = text;
	}

	public ResultEntity(String code, String text, Object data) {
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
