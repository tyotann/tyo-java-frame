package com.ihidea.component.oauth2.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ihidea.core.util.BeanUtilsEx;

public class OAuth2UserInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String openId;

	private String loginName;

	private String loginPwd;

	private String userName;

	private String nickname;

	private String phone;

	private String email;

	private String qq;

	private String appKey;

	private List<OAuth2AppInfo> appInfo;

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	private String birthday;

	private String company;

	private String address;

	private String position;

	private BigDecimal status;

	public BigDecimal getStatus() {
		return status;
	}

	public void setStatus(BigDecimal status) {
		this.status = status;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPosition() {
		return position;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public List<OAuth2AppInfo> getAppInfo() {
		return appInfo;
	}

	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	// TODO 反射没有深入
	public List<OAuth2AppInfo> getAppInfoList(String productId) {

		List<OAuth2AppInfo> result = new ArrayList<OAuth2AppInfo>();

		if (appInfo != null) {
			for (Object info : appInfo) {
				OAuth2AppInfo appinfo = BeanUtilsEx.convert(info, OAuth2AppInfo.class);

				// 如果传入产品编号,则用此编号做筛选
				if (StringUtils.isBlank(productId) || productId.equals(appinfo.getProductId())) {
					result.add(appinfo);
				}
			}
		}

		return result;
	}

	public void setAppInfo(List<OAuth2AppInfo> appInfo) {
		this.appInfo = appInfo;
	}

}
