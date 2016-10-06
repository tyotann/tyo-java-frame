package com.ihidea.component.task;

import java.math.BigDecimal;
import java.util.Date;

public class TaskEntity {

	private String taskId;

	private String taskName;

	private BigDecimal type;

	private BigDecimal seq;

	private BigDecimal status;

	private BigDecimal delFlag;

	private Date taskLastTime;

	private Date taskNextTime;

	private BigDecimal taskMaxTime;

	private String hostId;

	private TaskCfgEntity taskCfg;

	public TaskCfgEntity getTaskCfg() {
		return taskCfg;
	}

	public void setTaskCfg(TaskCfgEntity taskCfg) {
		this.taskCfg = taskCfg;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public BigDecimal getTaskMaxTime() {
		return taskMaxTime;
	}

	public void setTaskMaxTime(BigDecimal taskMaxTime) {
		this.taskMaxTime = taskMaxTime;
	}

	public Date getTaskLastTime() {
		return taskLastTime;
	}

	public void setTaskLastTime(Date taskLastTime) {
		this.taskLastTime = taskLastTime;
	}

	public Date getTaskNextTime() {
		return taskNextTime;
	}

	public void setTaskNextTime(Date taskNextTime) {
		this.taskNextTime = taskNextTime;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public BigDecimal getType() {
		return type;
	}

	public void setType(BigDecimal type) {
		this.type = type;
	}

	public BigDecimal getSeq() {
		return seq;
	}

	public void setSeq(BigDecimal seq) {
		this.seq = seq;
	}

	public BigDecimal getStatus() {
		return status;
	}

	public void setStatus(BigDecimal status) {
		this.status = status;
	}

	public BigDecimal getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(BigDecimal delFlag) {
		this.delFlag = delFlag;
	}

}
