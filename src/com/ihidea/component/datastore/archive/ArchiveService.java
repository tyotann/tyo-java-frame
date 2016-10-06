package com.ihidea.component.datastore.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.ihidea.component.datastore.FileSupportService;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.base.CoreService;
import com.ihidea.core.invoke.controller.ResultEntity;
import com.ihidea.core.invoke.controller.call.CallController;
import com.ihidea.core.invoke.controller.call.CallServiceFactory;
import com.ihidea.core.support.JdbcSupportService;
import com.ihidea.core.support.exception.ServiceException;

@Service
public class ArchiveService extends CoreService {

//	@Autowired
//	private LobHandler lobHandler;

	@Autowired
	private JdbcSupportService jdbcSupportService;

	@Autowired
	private FileSupportService fileSupportService;

	/**
	 * <pre>
	 * 档案目录树
	 * 
	 * &lt;pre&gt;
	 * &#064;param categoryid
	 * &#064;return
	 */
	public List<Map<String, Object>> loadArcContCate(String categoryid) {
		return jdbcSupportService.getJdbcTemplate().queryForList("select * from v_arccontent where categoryid=?",
				new Object[] { categoryid });
	}

	/**
	 * <pre>
	 *   该类是插入图片明细表的服务类。
	 * </pre>
	 * @param id ID
	 * @param ino 顺序号
	 * @param filename 文件名称
	 * @param path 文件路径
	 */
	public void savePicture(int id, String ino, String filename, String path) {
		String sql = "insert into ARCSJMX (id,ino,name,pathname) values(?,?,?,?)";
		jdbcSupportService.getJdbcTemplate().update(sql, new Object[] { id, ino, filename, path });
	}

	/**
	 * <pre>
	 * 保存收件图片到表ARCSJMX
	 * </pre>
	 * @param id ID
	 * @param ino 顺序号
	 * @param filename 文件名称
	 * @param path 文件路径
	 * @param is 输入流
	 * @param filesize 文件大小
	 */
	public void savePictureToDB(final int id, final long ino, final String filename, final String path, final InputStream is,
			final long filesize) {
//		jdbcSupportService.getJdbcTemplate().execute("insert into ARCSJMX (id,ino,name,pathname,content) VALUES(?,?,?,?,?)",
//				new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
//					protected void setValues(PreparedStatement pstmt, LobCreator lobCreator) throws SQLException, DataAccessException {
//						pstmt.setInt(1, id);
//						pstmt.setLong(2, ino);
//						pstmt.setString(3, filename);
//						pstmt.setString(4, path);
//						lobCreator.setBlobAsBinaryStream(pstmt, 5, is, (int) filesize);
//					}
//				});
	}

	/**
	 * <pre>
	 *   获取收件明细表序列号。
	 * </pre>
	 */
	public Long getArcsjmxino() {
		return jdbcSupportService.getJdbcTemplate().queryForLong("select SEQ_ARCSJMX.nextVal from dual", new Object[0]);
	}

	/**
	 * delete
	 * 
	 * <pre>
	 * 获取图片信息
	 * </pre>
	 * @param id ARCSJMX表ID
	 * @return
	 */
	public List<Map<String, Object>> getPicture(int id) {
		return jdbcSupportService.queryForList("select a.id,a.ino,a.name,a.ino url from arcsjmx a where a.id=? order by a.ino", id);
	}
	
	public List<Map<String, Object>> getPicture(String serviceName,Map<String, Object> params) {
		// 取得服务解析器
		CallController serviceHandle = CallServiceFactory.getInstance(serviceName.toString());
		// 服务调用
		try {
			ResultEntity re = serviceHandle.call(serviceName, params);
			return (List<Map<String, Object>>)re.getData();
		} catch (Exception e) {
			try {
				throw new Exception(e.getMessage());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return null;
		
	}

	/**
	 * <pre>
	 * 从数据库获取收件图片内容
	 * </pre>
	 * @param filename 文件名
	 * @param errfilename 错误图片路径
	 * @param os 输出流
	 */
	@SuppressWarnings("unchecked")
	public void getPictureFromDB(String filename, final String errfilename, final OutputStream os) {
		String sql = "SELECT content FROM arcsjmx WHERE pathname=? ";
		jdbcSupportService.getJdbcTemplate().query(sql, new Object[] { filename }, new AbstractLobStreamingResultSetExtractor() {
			@Override
			protected void handleNoRowFound() throws LobRetrievalFailureException {
				InputStream is;
				try {
					is = new FileInputStream(errfilename);
					FileCopyUtils.copy(is, os);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void streamData(ResultSet rs) throws SQLException, IOException {
//				InputStream is = lobHandler.getBlobAsBinaryStream(rs, 1);
//				if (is != null) {
//					FileCopyUtils.copy(is, os);
//				}
			}
		});
	}

	/**
	 * <pre>
	 * 更新ARCSJMX表中的图片信息
	 * </pre>
	 * @param path 文件路径
	 * @param is 输入流
	 * @param filesize 文件大小
	 */
	public void updatePictureToDB(final String path, final InputStream is, final long filesize) {
//		jdbcSupportService.getJdbcTemplate().execute("update ARCSJMX set content=? where pathname=? ",
//				new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
//					protected void setValues(PreparedStatement pstmt, LobCreator lobCreator) throws SQLException, DataAccessException {
//						lobCreator.setBlobAsBinaryStream(pstmt, 1, is, (int) filesize);
//						pstmt.setString(2, path);
//					}
//				});
	}

	/**
	 * <pre>
	 * 删除收件明细数据
	 * </pre>
	 * @param ino 顺序号
	 * @param fileName 文件路径
	 */
	public void deletePicture(String ino, String fileName, String storeName) {

		try {
			// 删除档案文件
			fileSupportService.remove(ino);

			// 删除表信息
			jdbcSupportService.getJdbcTemplate().update("delete ARCSJMX where ino=?", new Object[] { ino });
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * <pre>
	 *   删除收件信息，包括收件表数据及文件
	 * </pre>
	 * @param id 收件表ID
	 */
	public void deleteArcsj(int id) {
		try {
			List<Map<String, Object>> lst = jdbcSupportService.getJdbcTemplate().queryForList("select * from ARCSJMX where id=?",
					new Object[] { id });
			for (int i = 0; i <= lst.size() - 1; i++) {
				Map<String, Object> mFile = (Map<String, Object>) lst.get(i);
				File file = new File(CoreConstants.uploadDir + mFile.get("pathname").toString());
				if (file.exists()) {
					file.delete();
				}
			}
			jdbcSupportService.getJdbcTemplate().update("delete ARCSJMX where id=?", new Object[] { id });
			jdbcSupportService.getJdbcTemplate().update("delete ARCSJ where id=?", new Object[] { id });
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * <pre>
	 * 获取ARCSJ表ID信息
	 * </pre>
	 * @param typeid
	 * @param tabname
	 * @param ywlx
	 * @param cs
	 * @param ywid
	 * @return
	 */
	public List<Map<String, Object>> selectArcsjs(String typeid, String tabname, String ywlx, String cs, String ywid) {
		try {
			if (typeid.equals("")) {
				List<Map<String, Object>> lst = null;
				if (cs.equals("")) {
					lst = jdbcSupportService.queryForList("select typeid from arcsjlx where tabname=? and ywlx=?", new Object[] { tabname,
							ywlx });
				} else {
					lst = jdbcSupportService.queryForList("select typeid from arcsjlx where tabname=? and ywlx=? and cs=?", new Object[] {
							tabname, ywlx, cs });
				}
				typeid = lst.get(0).get("typeid").toString();
			}
			List<Map<String, Object>> result = jdbcSupportService.queryForList("select id from arcsj where typeid=? and ywid=?",
					new Object[] { typeid, ywid });

			return result;
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * <pre>
	 * 获取ARCSJ表ID信息
	 * </pre>
	 * @param tabname
	 * @param ywid
	 * @return
	 */
	public List<Map<String, Object>> selectArcsjs(String tabname, String ywid) {
		try {
			List<Map<String, Object>> lst = jdbcSupportService.queryForList("select id from arcsj where tabname=? and ywid=?",
					new Object[] { tabname, ywid });
			return lst;
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * <pre>
	 * 获取档案附件表ARCFJ信息
	 * </pre>
	 */
	public Map<String, Object> getArcfj(int id) {
		List<Map<String, Object>> lst = jdbcSupportService.getJdbcTemplate().queryForList("select * from arcfj where id=" + id);
		if (lst.size() > 0) {
			return lst.get(0);
		} else {
			return null;
		}
	}

	/**
	 * <pre>
	 * 查询档案案卷表ARCVOLUME信息
	 * </pre>
	 */
	public List<Map<String, Object>> getArcExport(String gdjbwd, String categoryid) {
		List<Map<String, Object>> lst = jdbcSupportService.getJdbcTemplate().queryForList(
				"select * from arcvolume where gdjbwd in (select gdjbwd from arcorgrelationshipgd where orgcode='" + gdjbwd
						+ "') and comp_category=" + categoryid
						+ " and gdsj>=(select nvl(max(datetime),to_date('20100101','yyyymmdd')) from arcexportlog where orgcode='" + gdjbwd
						+ "' and categoryid='" + categoryid + "') and gdsj<=sysdate");
		// this.commonDao.batchUpdate("insert into
		// arcexportlog(orgcode,categoryid,datetime) values (?,?,?) ", values);
		return lst;
	}

	/**
	 * <pre>
	 * 查询档案案卷表ARCVOLUME及档案附件表ARCFJ信息
	 * </pre>
	 */
	public List<Map<String, Object>> getArcExport2(String gdjbwd, String categoryid) {
		List<Map<String, Object>> lst2 = jdbcSupportService.getJdbcTemplate().queryForList(
				"select * from arcvolume a,arcfj b where a.gdjbwd in (select gdjbwd from arcorgrelationshipgd where orgcode='" + gdjbwd
						+ "') and a.comp_category=" + categoryid
						+ " and a.gdsj>=(select nvl(max(datetime),to_date('20100101','yyyymmdd')) from arcexportlog where orgcode='"
						+ gdjbwd + "' and categoryid='" + categoryid + "') and a.gdsj<=sysdate and a.archno = b.archno");
		// this.commonDao.batchUpdate("insert into
		// arcexportlog(orgcode,categoryid,datetime) values (?,?,?) ", values);
		return lst2;
	}

	/**
	 * <pre>
	 * 插入档案导出日志表ARCEXPORTLOG数据
	 * </pre>
	 */
	public void setArcExportLog(String orgcode, String categoryid, String userid) {
		jdbcSupportService.getJdbcTemplate().execute(
				"insert into arcexportlog values('" + orgcode + "','" + categoryid + "','" + userid + "',sysdate)");
	}

}