package com.ihidea.component.datastore.fileio;

public interface IFileIo {

	public void save(FileIoEntity entity);

	public void saveBak(FileIoEntity entity);

	public void updateContent(String id, final byte[] content);

	public boolean remove(FileIoEntity entity);

	public byte[] get(String id);

	public void execute(FileIoEntity fileIoEntity, IFileInputStream fileInputStreamImpl) throws Exception;

}
