package com.ihidea.core.support.exception;

/**
 * 业务异常
 * 
 * @author TYOTANN
 */
public class ServiceException extends RuntimeException {

	private String code;

	private Object data;

	public String getCode() {
		return code;
	}

	public Object getData() {
		return data;
	}

	public ServiceException() {
	}

	public ServiceException(Throwable e) {
		super(e);
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String code, String message) {
		super(message);
		this.code = code;
	}

	public ServiceException(Integer code, String message) {
		super(message);
		this.code = String.valueOf(code);
	}

	public ServiceException(String code, String message, Object data) {
		super(message);
		this.code = code;
		this.data = data;
	}

	public ServiceException(String message, Throwable e) {
		super(message, e);
	}
}
