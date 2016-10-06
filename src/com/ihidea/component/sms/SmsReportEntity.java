package com.ihidea.component.sms;

import java.util.Date;

public class SmsReportEntity {

	public SmsReportEntity(Date createDate, String msgId, String channelNo, String mobileNo, String status, String errorCode) {
		this.createDate = createDate;
		this.msgId = msgId;
		this.channelNo = channelNo;
		this.mobileNo = mobileNo;
		this.status = status;
		this.errorCode = errorCode;
	}

	private Date createDate;

	private String msgId;

	private String channelNo;

	private String mobileNo;

	private String status;

	private String errorCode;

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
