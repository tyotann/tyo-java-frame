package com.ihidea.component.datastore.fileio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.model.CptDataStore;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.JSONUtilsEx;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

/**
 * 七牛云存储
 * @author TYOTANN
 */
@Component
@Lazy
public class FileIoQiniu implements IFileIo {

	private static Log logger = LogFactory.getLog(FileIoQiniu.class);

	@Autowired
	private DataStoreService dataStoreService;

	/**
	 * 保存到存储路径
	 */
	@Override
	public void save(FileIoEntity entity) {
		saveFile(entity.getDataInfo().getId(), entity.getContent(), entity.getDataInfo().getStoreName());
	}

	/**
	 * 保存到备份存储路径
	 */
	public void saveBak(FileIoEntity entity) {
		throw new ServiceException("未实现");
	}

	private static Map<String, Map<String, String>> bucketMap = new HashMap<String, Map<String, String>>();

	@SuppressWarnings("unchecked")
	private synchronized Map<String, String> initBucketInfo(String storeName) {

		CptDataStore cptDataStore = dataStoreService.getInfoByName(storeName);
		Map<String, String> bucketInfo = JSONUtilsEx.deserialize(cptDataStore.getPath(), Map.class);
		bucketMap.put(storeName, bucketInfo);

		return bucketInfo;
	}

	/**
	 * 持久化
	 */
	private void saveFile(String id, InputStream is, String storeName) {

		Map<String, String> bucketInfo = bucketMap.get(storeName);

		// 根据storeName得到参数
		if (bucketInfo == null) {
			bucketInfo = initBucketInfo(storeName);
		}

		PutPolicy putPolicy = new PutPolicy(bucketInfo.get("BUCKET_NAME"));

		PutExtra extra = new PutExtra();

		try {
			PutRet ret = IoApi.Put(putPolicy.token(new Mac(bucketInfo.get("ACCESS_KEY"), bucketInfo.get("SECRET_KEY"))), id, is, extra);

			if (!ret.ok()) {
				throw new ServiceException("云存储上传出现异常:" + ret.toString());
			}
		} catch (Exception e) {
			throw new ServiceException("云存储上传出现异常:" + e.getMessage(), e);
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
	}

	private void saveFile(String id, byte[] content, String storeName) {

		try {
			InputStream is = new ByteArrayInputStream(content);
			saveFile(id, is, storeName);
		} catch (Exception e) {
			throw new ServiceException("云存储上传出现异常:" + e.getMessage(), e);
		}
	}

	public boolean remove(String id, String sotreName) {

		try {

			Map<String, String> bucketInfo = bucketMap.get(sotreName);

			// 根据storeName得到参数
			if (bucketInfo == null) {
				bucketInfo = initBucketInfo(sotreName);
			}

			Mac mac = new Mac(bucketInfo.get("ACCESS_KEY"), bucketInfo.get("SECRET_KEY"));
			RSClient client = new RSClient(mac);
			CallRet callRet = client.delete(bucketInfo.get("BUCKET_NAME"), id);
			return true;
		} catch (Exception e) {
			logger.error("移除云端文件[" + id + "]报错:" + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean remove(FileIoEntity entity) {
		return remove(entity.getDataInfo().getId(), entity.getDataInfo().getStoreName());
	}

	public byte[] get(String id) {
		return null;
	}

	@Override
	public void execute(FileIoEntity fileIoEntity, IFileInputStream fileInputStreamImpl) throws Exception {
		throw new ServiceException("未实现");
	}

	/**
	 * 先删除,后上传
	 */
	@Override
	public void updateContent(String id, byte[] content) {
		throw new ServiceException("未实现");
	}
}
