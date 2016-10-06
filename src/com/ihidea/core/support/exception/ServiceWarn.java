package com.ihidea.core.support.exception;

/**
 * 业务警告
 * @author TYOTANN
 */
public class ServiceWarn extends RuntimeException {

	public ServiceWarn() {
	}

	public ServiceWarn(Throwable e) {
		super(e);
	}

	public ServiceWarn(String message) {
		super(message);
	}

	public ServiceWarn(String message, Throwable e) {
		super(message, e);
	}
}
