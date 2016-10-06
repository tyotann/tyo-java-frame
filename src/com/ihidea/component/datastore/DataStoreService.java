package com.ihidea.component.datastore;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ihidea.component.datastore.dao.CptDataStoreMapper;
import com.ihidea.component.model.CptDataStore;
import com.ihidea.component.model.CptDataStoreExample;
import com.ihidea.component.model.CptDataStoreExample.Criteria;
import com.ihidea.core.base.CoreService;
import com.ihidea.core.support.cache.CacheSupport;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.support.orm.mybatis3.util.IbatisServiceUtils;
import com.ihidea.core.util.FileUtilsEx;

@Service
public class DataStoreService extends CoreService {

	@Autowired
	private CptDataStoreMapper dao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 添加
	 * @param record
	 * @return
	 */
	public int insertDataStore(CptDataStore record) {

		if ("2".equals(record.getType())) {
			if (!isExists(record.getPath())) {
				throw new ServiceException("文件路径不存在");
			}

			record.setPath(FileUtilsEx.filterPath(record.getPath()));

			// 备份
			if (!isExists(record.getBakPath())) {
				throw new ServiceException("备份文件路径不存在");
			}

			record.setBakPath(FileUtilsEx.filterPath(record.getBakPath()));
		}

		return dao.insert(record);
	}

	/**
	 * 文件路径是否存在
	 * @param path
	 * @return
	 */
	private boolean isExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public int deleteDataStore(String id) {

		// TODO
		// 删除前判断是否还有文件,如果还有,则无法删除

		return dao.deleteByPrimaryKey(id);
	}

	/**
	 * 更新
	 * @param record
	 * @return
	 */
	public int updateDataStore(CptDataStore record) {

		if ("2".equals(record.getType())) {
			record.setPath(FileUtilsEx.filterPath(record.getPath()));
			record.setBakPath(FileUtilsEx.filterPath(record.getBakPath()));
		}

		return dao.updateByPrimaryKey(record);
	}

	/**
	 * 查询
	 * @param record
	 * @return
	 * @throws Exception
	 */
	public List<CptDataStore> selectDataStores(CptDataStore record) throws Exception {
		CptDataStoreExample example = new CptDataStoreExample();

		Criteria criteria = example.createCriteria();

		IbatisServiceUtils.createCriteriaByEntity(criteria, record);

		example.setOrderByClause("id");

		return dao.selectByExample(example);
	}

	/**
	 * 查询
	 * @param id
	 * @return
	 */
	public CptDataStore selectDataStore(String id) {
		return dao.selectByPrimaryKey(id);
	}

	/**
	 * 通过存储名称得到存储信息
	 * @param name
	 * @return
	 */
	public CptDataStore getInfoByName(String name) {

		CptDataStore result = CacheSupport.get("DataStoreService.getInfoByName", name, CptDataStore.class);

		if (result == null) {
			result = jdbcTemplate.queryForObject("select id, name, type, path, type, bak_path from cpt_datastore where name = ?",
					new Object[] { name }, new BeanPropertyRowMapper<CptDataStore>(CptDataStore.class));
			CacheSupport.put("DataStoreService.getInfoByName", name, result);
		}

		return result;
	}
}
