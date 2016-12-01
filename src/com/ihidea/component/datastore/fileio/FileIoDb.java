package com.ihidea.component.datastore.fileio;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.stereotype.Component;

import com.ihidea.component.datastore.DataStoreService;
import com.ihidea.component.datastore.dao.TCptDataInfoMapper;
import com.ihidea.component.datastore.dao.model.TCptDataInfo;
import com.ihidea.core.support.JdbcSupportService;
import com.ihidea.core.support.exception.ServiceException;

/**
 * 存储数据库IO
 * @author TYOTANN
 */
@Component
class FileIoDb implements IFileIo {

	private Log logger = LogFactory.getLog(FileIoDb.class);

	@Autowired
	private DataStoreService dataStoreService;

	@Autowired
	private TCptDataInfoMapper dataInfoDao;

	@Autowired
	private JdbcSupportService jdbcSupportService;

//	@Autowired
//	private LobHandler lobHandler;

	/**
	 * 保存到存储路径
	 */
	@Override
	public void save(FileIoEntity entity) {

		saveFile(entity.getDataInfo().getId(), entity.getContent(), dataStoreService.getInfoByName(entity.getDataInfo().getStoreName())
				.getPath());

	}

	/**
	 * 保存到备份存储路径
	 */
	@Override
	public void saveBak(FileIoEntity entity) {

		saveFile(entity.getDataInfo().getId(), entity.getContent(), dataStoreService.getInfoByName(entity.getDataInfo().getStoreName())
				.getBakPath());

	}

	/**
	 * 持久化
	 */
	private void saveFile(final String id, final byte[] content, String storePath) {

//		jdbcSupportService.getJdbcTemplate().execute("insert into " + storePath + " (id,value) values(?,?)",
//				new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
//
//					@Override
//					protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
//
//						ps.setString(1, id);
//
//						InputStream stream = new ByteArrayInputStream(content);
//						lobCreator.setBlobAsBinaryStream(ps, 2, stream, content.length);
//					}
//				});
	}

	@Override
	public boolean remove(FileIoEntity entity) {

		String storePath = dataStoreService.getInfoByName(entity.getDataInfo().getStoreName()).getPath();

		try {
			jdbcSupportService.getJdbcTemplate().update("delete from " + storePath + " where id = ?",
					new Object[] { entity.getDataInfo().getId() });

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public byte[] get(String id) {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		String storePath = dataStoreService.getInfoByName(dataInfo.getStoreName()).getPath();

		final List<byte[]> byteBody = new ArrayList<byte[]>();

		jdbcSupportService.getJdbcTemplate().query("select value from " + storePath + " where id= ?", new Object[] { id },
				new AbstractLobStreamingResultSetExtractor() {

					@Override
					protected void handleNoRowFound() throws LobRetrievalFailureException {
					}

					@Override
					public void streamData(final ResultSet rs) throws SQLException, IOException {
//						byteBody.add(lobHandler.getBlobAsBytes(rs, 1));
					}
				});

		return byteBody.get(0);
	}

	@Override
	public void updateContent(final String id, final byte[] content) {

		TCptDataInfo dataInfo = dataInfoDao.selectByPrimaryKey(id);

		String storePath = dataStoreService.getInfoByName(dataInfo.getStoreName()).getPath();

		try {
//			jdbcSupportService.getJdbcTemplate().execute("update " + storePath + " set value = ? where id = ?",
//					new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
//
//						@Override
//						protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
//
//							InputStream stream = new ByteArrayInputStream(content);
//							lobCreator.setBlobAsBinaryStream(ps, 1, stream, content.length);
//
//							ps.setString(2, id);
//						}
//					});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public void execute(FileIoEntity fileIoEntity, IFileInputStream fileInputStreamImpl) {
		throw new ServiceException("未实现");
	}

	@Override
	public String getRealPath(FileIoEntity entity) {
		return null;
	}
}
