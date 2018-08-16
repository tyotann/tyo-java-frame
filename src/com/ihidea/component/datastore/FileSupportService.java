package com.ihidea.component.datastore;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ihidea.component.datastore.dao.TCptDataInfoMapper;
import com.ihidea.component.datastore.dao.model.TCptDataInfo;
import com.ihidea.component.datastore.fileio.FileIoEntity;
import com.ihidea.component.datastore.fileio.FileIoFactory;
import com.ihidea.component.datastore.fileio.IFileInputStream;
import com.ihidea.component.datastore.fileio.IFileIo;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.base.CoreService;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.orm.mybatis3.util.IbatisServiceUtils;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.FileUtilsEx;
import com.ihidea.core.util.ImageUtilsEx;
import com.ihidea.core.util.StringUtilsEx;

@Service
public class FileSupportService extends CoreService {

	@Autowired
	private TCptDataInfoMapper dataInfoDao;

	@Autowired
	private DataStoreService dataStoreService;

	/**
	 * @param fileName 文件名
	 * @param fileContent 文件二进制内容
	 * @param storeName cpt_datastore.name
	 * @param fileImgSize 图片压缩尺寸，100|200，100|0，0|300 或 格式 100,200，100,0，0,300 或 格式 100x200，100x0，0x300
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public String add(String fileId, String fileName, byte[] fileContent, String storeName, String fileImgSize, String filePath, boolean useOrigFileName) {

		TCptDataInfo dataInfo = new TCptDataInfo();

		// 文件信息
		{
			// 文件编号加入后缀名
			if (StringUtils.isBlank(fileId) && StringUtils.isBlank(filePath)) {
				if (!"true".equals(CoreConstants.getProperty("filestore.enable.suffix"))) {
					dataInfo.setId(StringUtilsEx.getUUID());
				} else {
					dataInfo.setId(StringUtilsEx.getUUID() + "." + FileUtilsEx.getSuffix(fileName));
				}
			} else {
				if(StringUtils.isNotBlank(filePath)) {
					if(!filePath.endsWith("/")) {
						filePath = filePath + "/" ;
					}

					if(useOrigFileName) {
						dataInfo.setId(filePath + fileName);
					} else {
						if (!"true".equals(CoreConstants.getProperty("filestore.enable.suffix"))) {
							dataInfo.setId(filePath + StringUtilsEx.getUUID());
						} else {
							dataInfo.setId(filePath + StringUtilsEx.getUUID() + "." + FileUtilsEx.getSuffix(fileName));
						}
					}
				}
			}

			// 有可能是非session
			dataInfo.setCreateUser(SessionContext.getSessionInfo() != null ? SessionContext.getSessionInfo().getUserId() : null);

			dataInfo.setCreateDate(DateUtilsEx.getSysDate());

			dataInfo.setFileName(fileName);

			dataInfo.setFileSize(BigDecimal.valueOf(fileContent.length));

			dataInfo.setStatus(BigDecimal.valueOf(0));

			dataInfo.setStoreName(storeName);
		}

		// 如果设置图片尺寸,并且是图片类型的文件则进行压缩
		if (StringUtils.isNotBlank(fileImgSize) && FileUtilsEx.isImage(fileName)) {

			fileImgSize = fileImgSize.replace(",", "|").replace("x", "|");

			String[] sizeArray = fileImgSize.split("\\|");

			try {
				fileContent = ImageUtilsEx.resizeImage(fileContent, Integer.valueOf(sizeArray[0]), Integer.valueOf(sizeArray[1]),
						FileUtilsEx.getSuffix(fileName));

				// 重新尺寸
				dataInfo.setFileSize(BigDecimal.valueOf(fileContent.length));
			} catch (Exception e) {
				logger.error("图片压缩出错:" + e.getMessage(), e);
			}
		}

		// 文件实际保存
		{
			IFileIo fileIo = FileIoFactory.getInstance(storeName);

			FileIoEntity entity = new FileIoEntity();
			entity.setDataInfo(dataInfo);
			entity.setContent(fileContent);

			fileIo.save(entity);
		}

		// 图片的话，设置具体尺寸
		setDescription(dataInfo, fileContent);

		// 文件信息保存
		dataInfoDao.insert(dataInfo);

		return dataInfo.getId();
	}

	public String add(String fileName, byte[] fileContent, String storeName, String fileImgSize, String filePath, boolean useOrigFileName) {
		return add(null, fileName, fileContent, storeName, fileImgSize, filePath, useOrigFileName);
	}

	public String add(String fileName, byte[] fileContent, String storeName) {
		return add(null, fileName, fileContent, storeName, null, null, false);
	}

	/**
	 * 设置文件描述
	 * @param dataInfo
	 * @param fileContent
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	private void setDescription(TCptDataInfo dataInfo, byte[] fileContent) {

		if (!FileUtilsEx.isImage(dataInfo.getFileName()))
			return;
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileContent));
			image.getHeight();
			dataInfo.setDescription(String.valueOf(image.getWidth()) + "|" + String.valueOf(image.getHeight()));
		} catch (IOException e) {
			this.logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 增加附件,不提交的话查询不到附件
	 * @param fileName 文件名
	 * @param fileContent 文件二进制内容
	 * @return
	 */
	public String add(String fileName, byte[] fileContent) {
		return add(fileName, fileContent, "default");
	}

	/**
	 * 提交文件,正式存储入库
	 * @param id 文件编号
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void submit(String id, String desc) {

		if (StringUtils.isBlank(desc)) {
			throw new ServiceException("提交入库的文件,必须要有描述!");
		}

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);
		dataInfo.setStatus(BigDecimal.valueOf(1));
		dataInfo.setDescription(desc);

		IbatisServiceUtils.updateByPk(dataInfo, dataInfoDao);

		// 备份文件保存
		{
			IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());
			FileIoEntity entity = get(id);

			if (!StringUtils.isEmpty(dataStoreService.getInfoByName(entity.getDataInfo().getStoreName()).getBakPath()))
				fileIo.saveBak(entity);
		}
	}

	/**
	 * 得到文件信息
	 * @param id 文件id
	 * @return
	 */
	public FileIoEntity get(String id) {

		FileIoEntity entity = new FileIoEntity();

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		if (dataInfo != null) {
			IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());
			entity.setContent(fileIo.get(id));
			entity.setDataInfo(dataInfo);
		}

		return entity;
	}

	public FileIoEntity getInfo(String id) {

		FileIoEntity entity = new FileIoEntity();

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		if (dataInfo != null) {
			entity.setDataInfo(dataInfo);
		}

		return entity;
	}

	/**
	 * 不得到文件内容,得到文件的信息
	 * @param id
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public FileIoEntity execute(String id, IFileInputStream fileInputStreamImpl) throws Exception {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		// TODO 兼容没有后缀名的文件
		if (dataInfo == null && StringUtils.isNotBlank(id) && id.indexOf(".") > -1) {
			dataInfo = dataInfoDao.selectByPrimaryKey(id.substring(0, id.indexOf(".")));
		}

		FileIoEntity entity = new FileIoEntity(dataInfo);

		if (dataInfo != null && StringUtils.isNotBlank(dataInfo.getStoreName()) && StringUtils.isNotBlank(dataInfo.getId())) {

			IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());
			fileIo.execute(entity, fileInputStreamImpl);
		} else {
			fileInputStreamImpl.execute(null, null);
		}

		return entity;
	}

	/**
	 * 得到文件描述信息
	 * @param ids 文件id
	 * @return
	 */
	public List<FileIoEntity> getInfoByIds(String ids) {

		List<FileIoEntity> resultList = new ArrayList<FileIoEntity>();

		if (StringUtils.isNotBlank(ids)) {
			String[] idArray = ids.split(",");

			for (int i = 0; i < idArray.length; i++) {

				if (StringUtils.isNotBlank(idArray[i])) {

					TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(idArray[i]);

					if (dataInfo != null) {
						FileIoEntity entity = new FileIoEntity();
						entity.setDataInfo(dataInfo);
						resultList.add(entity);
					}
				}
			}
		}

		return resultList;
	}

	/**
	 * 通过文件名找到文件
	 * @param fileName 文件名
	 * @param storeName 存储名
	 * @return
	 */
	public List<FileIoEntity> getByName(String fileName, String storeName) {

		TCptDataInfo dataInfo = new TCptDataInfo();
		dataInfo.setFileName(fileName);
		dataInfo.setStoreName(storeName);

		List<TCptDataInfo> dataInfoList = IbatisServiceUtils.find(dataInfo, dataInfoDao);

		IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());

		List<FileIoEntity> result = new ArrayList<FileIoEntity>();

		for (TCptDataInfo dataInfoT : dataInfoList) {

			FileIoEntity entity = new FileIoEntity();

			entity.setContent(fileIo.get(dataInfoT.getId()));
			entity.setDataInfo(dataInfoT);

			result.add(entity);
		}

		return result;
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void remove(String id) {

		if (StringUtils.isNotBlank(id)) {
			TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

			IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());

			FileIoEntity entity = new FileIoEntity();
			entity.setDataInfo(dataInfo);

			if (fileIo.remove(entity)) {
				dataInfoDao.deleteByPrimaryKey(id);
			} else {
				throw new ServiceException("文件:" + id + "删除失败");
			}
		}
	}

	/**
	 * 根据name删除历史数据
	 * @param fileName
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void removeByName(String fileName, String storeName) {

		List<FileIoEntity> res = getByName(fileName, storeName);

		for (int i = 0; i < res.size(); i++) {
			TCptDataInfo dataInfo = res.get(i).getDataInfo();

			remove(dataInfo.getId());
		}
	}

	/**
	 * 更新文件内容
	 * @param id
	 * @param content
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void updateContent(String id, byte[] content) {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		dataInfo.setFileSize(new BigDecimal(content.length));

		// 修改文件长度
		IbatisServiceUtils.updateByPk(dataInfo, dataInfoDao);

		IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());

		// 修改文件内容
		fileIo.updateContent(id, content);
	}

	/**
	 * 清理临时数据
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void clearTemp(String storeName) {

		IFileIo fileIo = FileIoFactory.getInstance(storeName);

		// 找出所有状态为0的数据
		TCptDataInfo searchEntity = new TCptDataInfo();
		searchEntity.setStoreName(storeName);
		searchEntity.setStatus(BigDecimal.ZERO);

		for (TCptDataInfo tempData : IbatisServiceUtils.find(searchEntity, dataInfoDao)) {

			// 移除数据内容
			FileIoEntity fileIoEntity = new FileIoEntity();
			fileIoEntity.setDataInfo(tempData);

			// 如果正常删除,则修改状态
			if (fileIo.remove(fileIoEntity)) {

				tempData.setStatus(new BigDecimal("-1"));
				dataInfoDao.updateByPrimaryKey(tempData);
			}

		}
	}
	
	public String getRealPathById(String id, String fileImgSize){
		
		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);
		
		FileIoEntity entity = new FileIoEntity();
		entity.setDataInfo(dataInfo);
		entity.setFileImgSize(fileImgSize);
		
		IFileIo fileIo = FileIoFactory.getInstance(dataInfo.getStoreName());
		
		return fileIo.getRealPath(entity);
	}
}
