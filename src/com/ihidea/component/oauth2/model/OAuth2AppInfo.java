package com.ihidea.component.oauth2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class OAuth2AppInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String appId;

	private String appName;

	private String productId;

	private String productName;

	private String styleId;

	private String styleName;

	private String icon;

	private String loginUrl;

	private Date endDate;

	private String appStatus;

	private Map<String, String> styleVersion;

	public Map<String, String> getStyleVersion() {
		return styleVersion;
	}

	public void setStyleVersion(Map<String, String> styleVersion) {
		this.styleVersion = styleVersion;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getStyleId() {
		return styleId;
	}

	public void setStyleId(String styleId) {
		this.styleId = styleId;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(String appStatus) {
		this.appStatus = appStatus;
	}

}
