package com.ihidea.component.task;

import java.math.BigDecimal;

public class TaskCfgEntity {

	private BigDecimal taskId;

	private BigDecimal callType;

	private String call;

	private String callParam;

	private String cron;

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public BigDecimal getTaskId() {
		return taskId;
	}

	public void setTaskId(BigDecimal taskId) {
		this.taskId = taskId;
	}

	public BigDecimal getCallType() {
		return callType;
	}

	public void setCallType(BigDecimal callType) {
		this.callType = callType;
	}

	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getCallParam() {
		return callParam;
	}

	public void setCallParam(String callParam) {
		this.callParam = callParam;
	}

}
