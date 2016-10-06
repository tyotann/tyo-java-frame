package com.ihidea.component.model;

import java.math.BigDecimal;

import com.ihidea.core.base.CoreEntity;

public class CptImportCfgDtlEx extends CoreEntity {

	private String id;

	private String columnName;

	private String columnCode;

	private String type;

	private BigDecimal dataLength;

	private BigDecimal isNull;

	private BigDecimal displayOrder;

	private String filterName;

	private String appId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnCode() {
		return columnCode;
	}

	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getDataLength() {
		return dataLength;
	}

	public void setDataLength(BigDecimal dataLength) {
		this.dataLength = dataLength;
	}

	public BigDecimal getIsNull() {
		return isNull;
	}

	public void setIsNull(BigDecimal isNull) {
		this.isNull = isNull;
	}

	public BigDecimal getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(BigDecimal displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName == null ? filterName : "";
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}
