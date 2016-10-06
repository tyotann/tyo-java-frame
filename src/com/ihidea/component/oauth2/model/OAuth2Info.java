package com.ihidea.component.oauth2.model;

import java.io.Serializable;

public class OAuth2Info implements Serializable {

	private static final long serialVersionUID = 1L;

	private OAuth2UserInfo userInfo;

	private String code;

	private String text;

	private String ssoToken;

	private String refreshToken;

	private String accessToken;

	public String getSsoToken() {
		return ssoToken;
	}

	public void setSsoToken(String ssoToken) {
		this.ssoToken = ssoToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public OAuth2UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(OAuth2UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
