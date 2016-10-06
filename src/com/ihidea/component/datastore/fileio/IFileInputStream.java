package com.ihidea.component.datastore.fileio;

import java.io.InputStream;

public interface IFileInputStream {

	public void execute(FileIoEntity entity, InputStream is) throws Exception;
}
