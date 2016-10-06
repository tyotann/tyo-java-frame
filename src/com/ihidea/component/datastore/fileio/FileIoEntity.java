package com.ihidea.component.datastore.fileio;

import com.ihidea.component.datastore.dao.model.TCptDataInfo;

public class FileIoEntity {

	private TCptDataInfo dataInfo;

	private byte[] content;

	public FileIoEntity() {
	}

	public FileIoEntity(TCptDataInfo dataInfo) {
		this.dataInfo = dataInfo;
	}

	public TCptDataInfo getDataInfo() {
		return dataInfo;
	}

	public void setDataInfo(TCptDataInfo dataInfo) {
		this.dataInfo = dataInfo;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
