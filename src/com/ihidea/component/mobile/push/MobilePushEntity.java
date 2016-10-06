package com.ihidea.component.mobile.push;

import java.util.HashMap;
import java.util.Map;

public class MobilePushEntity {

	// jpush apiMasterSecret
	private String jpushApiMasterSecret;

	// jpush appkey
	private String jpushAppKey;

	// jpush timeToLive
	private Long jpushTimeToLive;

	// 消息标题
	private String msgTitle;

	// 消息内容
	private String msgContent;

	// 新消息的类型
	private String type;

	// 新消息的值
	private String value;

	// 业务类型
	@Deprecated
	private Integer bType;

	// 业务编号
	@Deprecated
	private String bId;

	// 其他扩展属性
	@Deprecated
	private Map<String, String> attr = new HashMap<String, String>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMsgTitle() {
		return msgTitle;
	}

	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	@Deprecated
	public String getbId() {
		return bId;
	}

	@Deprecated
	public void setbId(String bId) {
		this.bId = bId;
	}

	@Deprecated
	public Integer getbType() {
		return bType;
	}

	@Deprecated
	public void setbType(Integer bType) {
		this.bType = bType;
	}

	@Deprecated
	public Map<String, String> getAttr() {
		return attr;
	}

	@Deprecated
	public void setAttr(Map<String, String> attr) {
		this.attr = attr;
	}

	public String getJpushApiMasterSecret() {
		return jpushApiMasterSecret;
	}

	public void setJpushApiMasterSecret(String jpushApiMasterSecret) {
		this.jpushApiMasterSecret = jpushApiMasterSecret;
	}

	public String getJpushAppKey() {
		return jpushAppKey;
	}

	public void setJpushAppKey(String jpushAppKey) {
		this.jpushAppKey = jpushAppKey;
	}

	public Long getJpushTimeToLive() {
		return jpushTimeToLive;
	}

	public void setJpushTimeToLive(Long jpushTimeToLive) {
		this.jpushTimeToLive = jpushTimeToLive;
	}

}
