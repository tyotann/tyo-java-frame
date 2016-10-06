package com.ihidea.component.model;

import java.util.List;

import com.ihidea.core.base.CoreEntity;

public class CptImportCfgEx extends CoreEntity {

	private String id;

	private String templatePath;

	private String serviceName;

	private String name;

	private String appid;

	private String blobid;

	private String importTable;

	// 错误信息列
	private String importErrorColumnName = "C90";

	private List<CptImportCfgDtlEx> importCptDtls;

	public CptImportCfgEx() {
	}

	public List<CptImportCfgDtlEx> getImportCptDtls() {
		return importCptDtls;
	}

	public void setImportCptDtls(List<CptImportCfgDtlEx> importCptDtls) {
		this.importCptDtls = importCptDtls;
	}

	public CptImportCfgEx(String id, String appid) {
		this.id = id;
		this.appid = appid;
	}

	public String getImportTable() {
		return importTable;
	}

	public void setImportTable(String importTable) {
		this.importTable = importTable;
	}

	public String getBlobid() {
		return blobid;
	}

	public void setBlobid(String blobid) {
		this.blobid = blobid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getImportErrorColumnName() {
		return importErrorColumnName;
	}

	public void setImportErrorColumnName(String importErrorColumnName) {
		this.importErrorColumnName = importErrorColumnName;
	}

}
