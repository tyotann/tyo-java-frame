package com.ihidea.component.mail;

import java.util.HashMap;
import java.util.Map;

/**
 * @作者 黎江
 * @创建日期 Sep 17, 2013
 * @创建时间 9:41:48 AM
 * @版本号 V 1.0
 * @description-
 */
public class MailInfo {

	public MailInfo() {
	}

	public MailInfo(String from, String[] to, String subject, String text) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
	}

	public MailInfo(String from, String[] to, String subject, String text, Map<String, byte[]> attachments) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
		this.attachments = attachments;
	}

	public MailInfo(String from, String[] to, String subject, String text, Map<String, byte[]> attachments, String senderHost,
			String senderUserName, String senderPassword) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
		this.attachments = attachments;
		this.senderHost = senderHost;
		this.senderUserName = senderUserName;
		this.senderPassword = senderPassword;
	}

	public MailInfo(String from, String[] to, String subject, String text, Map<String, byte[]> attachments, String senderHost,
			String senderUserName, String senderPassword, String nickName) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.text = text;
		this.attachments = attachments;
		this.senderHost = senderHost;
		this.senderUserName = senderUserName;
		this.senderPassword = senderPassword;
		this.nickName = nickName;
	}

	private String senderHost;

	private Integer senderPort;

	private String senderUserName;

	private String senderPassword;

	// 发件人昵称
	private String nickName;

	// 邮件接收者(可多个)
	private String[] to;

	// 邮件发送者邮箱
	private String from;

	// 邮件主题
	private String subject;

	// 邮件内容
	private String text;

	// 附件列表
	private Map<String, byte[]> attachments = new HashMap<String, byte[]>();

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Map<String, byte[]> getAttachmentList() {
		return attachments;
	}

	public void setAttachmentList(Map<String, byte[]> attachmentList) {
		this.attachments = attachmentList;
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String getFrom() {
		return from == null ? "" : from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject == null ? "" : subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(Integer senderPort) {
		this.senderPort = senderPort;
	}

	public String getText() {
		return text == null ? "" : text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSenderHost() {
		return senderHost;
	}

	public void setSenderHost(String senderHost) {
		this.senderHost = senderHost;
	}

	public String getSenderUserName() {
		return senderUserName;
	}

	public void setSenderUserName(String senderUserName) {
		this.senderUserName = senderUserName;
	}

	public String getSenderPassword() {
		return senderPassword;
	}

	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}
}
