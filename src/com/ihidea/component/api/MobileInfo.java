package com.ihidea.component.api;

public class MobileInfo {

	public MobileInfo() {
	}

	public MobileInfo(String userid, String deviceid, String appid) {
		this.userId = userid;
		this.deviceid = deviceid;
		this.appid = appid;
	}

	public MobileInfo(String userid, String deviceid, String appid, String apiPlatform, String apiVersion) {
		this.userId = userid;
		this.deviceid = deviceid;
		this.appid = appid;
		this.apiPlatform = apiPlatform;
		this.apiVersion = apiVersion;
	}

	private String userId;

	private String deviceid;

	private String appid;

	private String apiPlatform;

	public String getApiPlatform() {
		return apiPlatform;
	}

	public void setApiPlatform(String apiPlatform) {
		this.apiPlatform = apiPlatform;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	private String apiVersion;

	public String getUserId() {
		return userId;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public String getAppid() {
		return appid;
	}

}
