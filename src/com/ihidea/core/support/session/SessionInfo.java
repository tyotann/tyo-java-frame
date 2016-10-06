package com.ihidea.core.support.session;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ihidea.component.oauth2.model.OAuth2Info;

public class SessionInfo implements Serializable {

	private static final long serialVersionUID = 5721822991155494358L;

	private String sessionId;

	private String userId;

	private String userName;

	private String loginname;

	private Date intime = new Date();

	private String onlinetime;

	private OAuth2Info oauth2Info;

	private Map<String, String> attribute = new HashMap<String, String>();

	public void setAttribute(String name, String value) {
		this.attribute.put(name, value);
	}

	public String getAttribute(String name) {
		return this.attribute.get(name);
	}

	public Map<String, String> getAttribute() {
		return this.attribute;
	}

	public OAuth2Info getOauth2Info() {
		return oauth2Info;
	}

	public void setOauth2Info(OAuth2Info oauth2Info) {
		this.oauth2Info = oauth2Info;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLoginname() {
		return this.loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getOnlinetime() {

		long t = (new Date().getTime() - getIntime().getTime()) / 1000;
		long hour = t / 3600;
		long min = (t - hour * 3600) / 60;
		long sec = t - hour * 3600 - min * 60;
		onlinetime = StringUtils.leftPad(String.valueOf(hour), 2, "0") + ":" + StringUtils.leftPad(String.valueOf(min), 2, "0") + ":"
				+ StringUtils.leftPad(String.valueOf(sec), 2, "0");

		return onlinetime;
	}

	public void setOnlinetime(String onlinetime) {
		this.onlinetime = onlinetime;
	}

	public Date getIntime() {
		return this.intime;
	}

	public void setIntime(Date intime) {
		this.intime = intime;
	}

}
