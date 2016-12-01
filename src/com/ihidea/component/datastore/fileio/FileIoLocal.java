package com.ihidea.component.datastore.fileio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.datastore.dao.TCptDataInfoMapper;
import com.ihidea.component.datastore.dao.model.TCptDataInfo;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.DateUtilsEx;

/**
 * 存储本地文件IO
 * @author TYOTANN
 */
@Component
public class FileIoLocal implements IFileIo {

	private Log logger = LogFactory.getLog(FileIoLocal.class);

	@Autowired
	private DataStoreService dataStoreService;

	@Autowired
	private TCptDataInfoMapper dataInfoDao;

	/**
	 * 保存到存储路径
	 */
	@Override
	public void save(FileIoEntity entity) {

		saveFile(entity.getDataInfo().getId(), entity.getContent(),
				getPath(entity.getDataInfo().getStoreName(), entity.getDataInfo().getCreateDate()));

	}

	/**
	 * 保存到备份存储路径
	 */
	public void saveBak(FileIoEntity entity) {

		saveFile(entity.getDataInfo().getId(), entity.getContent(),
				getBakPath(entity.getDataInfo().getStoreName(), entity.getDataInfo().getCreateDate()));

	}

	/**
	 * 持久化
	 */
	private void saveFile(String id, byte[] content, String storePath) {

		File file = new File(storePath + File.separator + id);

		if (file.exists()) {
			throw new ServiceException("文件:" + file.getPath() + "已经存在!");
		}

		// 文件夹不存在的话则创建
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			FileUtils.writeByteArrayToFile(file, content);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public boolean remove(FileIoEntity entity) {

		String path = getPath(entity.getDataInfo().getStoreName(), entity.getDataInfo().getCreateDate());

		File file = new File(path + File.separator + entity.getDataInfo().getId());

		// file.exists()==true?file.delete():true;
		return file.delete();
	}

	public byte[] get(String id) {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		String path = getPath(dataInfo.getStoreName(), dataInfo.getCreateDate());

		try {

			File downloadFile = new File(path + File.separator + id);

			if (downloadFile.exists()) {
				return FileUtils.readFileToByteArray(downloadFile);
			} else {
				return null;
			}

		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void execute(FileIoEntity fileIoEntity, IFileInputStream fileInputStreamImpl) throws Exception {

		String filePath = getPath(fileIoEntity.getDataInfo().getStoreName(), fileIoEntity.getDataInfo().getCreateDate()) + File.separator
				+ fileIoEntity.getDataInfo().getId();
		File downloadFile = new File(filePath);

		if (downloadFile.exists()) {

			FileInputStream fis = null;

			try {
				fis = FileUtils.openInputStream(downloadFile);
				fileInputStreamImpl.execute(fileIoEntity, fis);
			} catch (ClientAbortException e) {
			} catch (IOException e) {
				throw new ServiceException(e);
			} finally {
				if (fis == null) {
					IOUtils.closeQuietly(fis);
				}
			}
		} else {
			logger.info("文件不存在:" + filePath);
			fileInputStreamImpl.execute(null, null);
		}

	}

	private String getPath(String storeName, Date createDate) {

		String storePath = dataStoreService.getInfoByName(storeName).getPath();

		return storePath + DateUtilsEx.formatToString(createDate, DateUtilsEx.DATE_FORMAT_DAY).replace(".", File.separator);
	}

	private String getBakPath(String storeName, Date createDate) {

		String storePath = dataStoreService.getInfoByName(storeName).getBakPath();

		return storePath + DateUtilsEx.formatToString(createDate, DateUtilsEx.DATE_FORMAT_DAY).replace(".", File.separator);
	}

	@Override
	public void updateContent(String id, byte[] content) {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		String path = getPath(dataInfo.getStoreName(), dataInfo.getCreateDate());

		File file = new File(path + File.separator + id);

		file.deleteOnExit();
		try {
			FileUtils.writeByteArrayToFile(file, content);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public String getRealPath(FileIoEntity entity) {
		return null;
	}
}
