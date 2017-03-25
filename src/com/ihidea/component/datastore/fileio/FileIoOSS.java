package com.ihidea.component.datastore.fileio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.datastore.dao.TCptDataInfoMapper;
import com.ihidea.component.datastore.dao.model.TCptDataInfo;
import com.ihidea.component.model.CptDataStore;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.JSONUtilsEx;

/**
 * 阿里云存储对象OSS
 * 
 * @author wenhao
 */
@Component
public class FileIoOSS implements IFileIo {

	private static Map<String, Map<String, String>> bucketMap = new HashMap<String, Map<String, String>>();

	private static OSSClient ossClient = null;

	@Autowired
	private DataStoreService dataStoreService;

	@Autowired
	private TCptDataInfoMapper dataInfoDao;

	@SuppressWarnings("unchecked")
	private synchronized Map<String, String> initBucketInfo(String storeName) {

		CptDataStore cptDataStore = dataStoreService.getInfoByName(storeName);
		Map<String, String> bucketInfo = JSONUtilsEx.deserialize(cptDataStore.getPath(), Map.class);
		bucketMap.put(storeName, bucketInfo);

		return bucketInfo;
	}

	private synchronized void initOssClient(Map<String, String> bucketInfo) {
		ossClient = new OSSClient(bucketInfo.get("endpoint"), bucketInfo.get("accessKeyId"), bucketInfo.get("accessKeySecret"));
	}

	/**
	 * 保存到OSS
	 */
	@Override
	public void save(FileIoEntity entity) {
		saveFile(entity.getDataInfo().getId(), entity.getDataInfo().getFileName(), entity.getContent(),
				entity.getDataInfo().getStoreName());
	}

	/**
	 * 保存到备份存储OSS
	 */
	public void saveBak(FileIoEntity entity) {
		throw new ServiceException("未实现");
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("endpoint", "oss-cn-qingdao.aliyuncs.com");
		map.put("key", "img/");

		System.out.println(JSONUtilsEx.serialize(map));
	}

	private void saveFile(String id, String name, byte[] content, String storeName) {

		Map<String, String> bucketInfo = bucketMap.get(storeName);

		// 根据storeName得到参数
		if (bucketInfo == null) {
			bucketInfo = initBucketInfo(storeName);
		}

		try {
			if (ossClient == null) {
				initOssClient(bucketInfo);
			}

			ossClient.putObject(bucketInfo.get("bucketName"), bucketInfo.get("key") + id, new ByteArrayInputStream(content));
		} catch (Exception e) {
			throw new ServiceException("阿里云存储对象OSS上传出现异常:" + e.getMessage(), e);
		}

	}

	@Override
	public boolean remove(FileIoEntity entity) {
		throw new ServiceException("未实现");
	}

	public byte[] get(String id) {

		byte[] data = null;

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);
		if (dataInfo != null) {
			Map<String, String> bucketInfo = bucketMap.get(dataInfo.getStoreName());

			if (bucketInfo == null) {
				bucketInfo = initBucketInfo(dataInfo.getStoreName());
			}

			if (ossClient == null) {
				initOssClient(bucketInfo);
			}
			OSSObject ossObject = ossClient.getObject(bucketInfo.get("bucketName"), bucketInfo.get("key") + id);

			try {
				InputStream inputStream = ossObject.getObjectContent();
				data = toByteArray(inputStream);
				inputStream.close();
			} catch (IOException e) {
				throw new ServiceException("阿里云存储对象OSS获得对象出现异常:" + e.getMessage());
			}
		}
		return data;
	}

	@Override
	public void execute(FileIoEntity fileIoEntity, IFileInputStream fileInputStreamImpl) throws Exception {
		throw new ServiceException("未实现");
	}

	@Override
	public void updateContent(String id, byte[] content) {
		throw new ServiceException("未实现");
	}

	private static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	public String getRealPath(FileIoEntity entity) {

		Map<String, String> bucketInfo = bucketMap.get(entity.getDataInfo().getStoreName());

		if (bucketInfo == null) {
			bucketInfo = initBucketInfo(entity.getDataInfo().getStoreName());
		}

		if (bucketInfo != null && bucketInfo.get("realPath") != null) {

			// 拼接压缩参数
			StringBuffer fileImgSizeParams = new StringBuffer();

			if (entity.getFileImgSize() != null) {

				try {

					String[] sizeArray = entity.getFileImgSize().replace(",", "|").replace("x", "|").split("\\|");
					// ,m_lfit,h_100,w_100
					fileImgSizeParams.append("?x-oss-process=image/resize");

					if (Integer.valueOf(sizeArray[0]) != 0 && Integer.valueOf(sizeArray[1]) != 0) {
						// 长宽均不为0，则为固定宽高压缩
						fileImgSizeParams.append(",m_fill");
					}

					if (Integer.valueOf(sizeArray[0]) != 0) {
						// 加入width压缩参数
						fileImgSizeParams.append(",w_" + sizeArray[0]);
					}

					if (Integer.valueOf(sizeArray[1]) != 0) {
						// 加入height压缩参数
						fileImgSizeParams.append(",h_" + sizeArray[1]);
					}

					// 指定当目标缩略图大于原图时是否处理。值是 1 表示不处理；值是 0 表示处理。
					fileImgSizeParams.append(",limit_0");

				} catch (Exception e) {
					throw new ServiceException("阿里云存储对象OSS处理压缩参数时出现异常:" + e.getMessage());
				}

			}

			return bucketInfo.get("realPath") + "/" + entity.getDataInfo().getId() + fileImgSizeParams;
		}
		return null;
	}
}
