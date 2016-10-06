package com.ihidea.component.datastore.fileio;

import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.model.CptDataStore;
import com.ihidea.core.support.SpringContextLoader;
import com.ihidea.core.support.exception.ServiceException;

public class FileIoFactory {

	public static IFileIo getInstance(String dataStoreName) {

		IFileIo fileIo = null;

		DataStoreService dataStoreService = SpringContextLoader.getBean(DataStoreService.class);

		CptDataStore cptDataStore = dataStoreService.getInfoByName(dataStoreName);

		if ("1".equals(cptDataStore.getType())) {
			fileIo = SpringContextLoader.getBean(FileIoDb.class);
		} else if ("2".equals(cptDataStore.getType())) {
			fileIo = SpringContextLoader.getBean(FileIoLocal.class);
		} else if ("3".equals(cptDataStore.getType())) {
			fileIo = SpringContextLoader.getBean(FileIoOSS.class);
		} else {
			throw new ServiceException("没有找到类型为:" + cptDataStore.getType() + "的数据存储方式!");
		}

		return fileIo;
	}
}
