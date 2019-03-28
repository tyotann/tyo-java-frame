package com.ihidea.core.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Component;

/**
 * 如果全部使用mybatis作为dao，这个最后除了存储过程,都可以删除
 * @author TYOTANN
 */
@Component
public class JdbcSupportService {

	protected Log logger = LogFactory.getLog(JdbcSupportService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

//	private SimpleJdbcTemplate simpleJdbcTemplate;
//
//	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
//		if (this.simpleJdbcTemplate == null) {
//			this.simpleJdbcTemplate = new SimpleJdbcTemplate(jdbcTemplate.getDataSource());
//		}
//		return this.simpleJdbcTemplate;
//	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public int batchUpdate(String sql, final List<Object[]> list) {
		int[] counts = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return list.size();
			}

			public void setValues(PreparedStatement ps, int index) throws SQLException {
				Object[] objects = (Object[]) list.get(index);
				for (int i = 1; i <= objects.length; ++i)
					ps.setObject(i, objects[(i - 1)]);
			}
		});
		int count = 0;
		for (int number : counts) {
			count += number;
		}

		return count;
	}

	/**
	 * spring的增强
	 * @param <T>
	 * @param sql
	 * @param t
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> queryForList(String sql, Class t) {
		return (List<T>) getJdbcTemplate().query(sql, BeanPropertyRowMapper.newInstance(t));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> queryForList(String sql, Object[] param, Class t) {
		return (List<T>) getJdbcTemplate().query(sql, param, BeanPropertyRowMapper.newInstance(t));
	}

	public List<Map<String, Object>> queryForList(String sql, Object... objects) {

		return jdbcTemplate.query(sql, objects, new ResultSetExtractor<List<Map<String, Object>>>() {

			public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				ResultSetMetaData data = rs.getMetaData();
				int ColumnCount = data.getColumnCount();
				while (rs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int i = 1; i <= ColumnCount; ++i) {
						String clunName = data.getColumnName(i);
						map.put(clunName.toLowerCase(), rs.getObject(clunName));
					}
					list.add(map);
				}

				return list;
			}
		});
	}

	/**
	 * 调用对应的存储过程
	 * @param procedureName
	 * @param paramType
	 * @param param
	 * @return
	 */
	public Map<String, Object> callProcedure(String procedureName, List<SqlParameter> paramType, Map<String, Object> param) {
		CallStoreProcedure xsp = new CallStoreProcedure(getJdbcTemplate(), procedureName);
		for (int i = 0; i < paramType.size(); ++i) {
			SqlParameter parameter = (SqlParameter) paramType.get(i);
			if (parameter instanceof SqlOutParameter)
				xsp.setOutParameter(parameter.getName(), parameter.getSqlType());
			else {
				xsp.setParameter(parameter.getName(), parameter.getSqlType());
			}
		}
		xsp.SetInParam(param);
		return xsp.execute();
	}

	private class CallStoreProcedure extends StoredProcedure {

		private Map<String, Object> inParam;

		private RowMapper<Map<String, Object>> rm = new RowMapper<Map<String, Object>>() {
			public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
				return null;
			}
		};

		private RowMapperResultSetExtractor<Map<String, Object>> callback = new RowMapperResultSetExtractor<Map<String, Object>>(rm) {

			public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

				if (!rs.isClosed()) {
					int count = rs.getMetaData().getColumnCount();
					String[] header = new String[count];

					// 属性名都改为小写
					for (int i = 0; i < count; ++i) {
						header[i] = rs.getMetaData().getColumnName(i + 1).toLowerCase();
					}

					while (rs.next()) {
						Map<String, Object> row = new HashMap<String, Object>(count + 7);
						for (int i = 0; i < count; ++i) {
							row.put(header[i], rs.getObject(i + 1));
						}
						result.add(row);
					}
				}

				return result;
			}
		};

		public void setOutParameter(String column, int type) {
			declareParameter(new SqlOutParameter(column, type, callback));
		}

		public void setParameter(String column, int type) {
			declareParameter(new SqlParameter(column, type));
		}

		public void SetInParam(Map<String, Object> inParam) {
			this.inParam = inParam;
		}

		public Map<String, Object> execute() {

			Map<String, Object> result = null;

			try {
				compile();
				result = execute(this.inParam);
			} catch (Exception e) {

				// 存储过程中没有开游标，会导致此处游标已关闭错误产生
				if (e.getMessage().indexOf("Cursor is closed") > 0) {
					result = null;
				} else {
					logger.info(e.getMessage(), e);
				}
			}

			return result;
		}

		@SuppressWarnings("unused")
		public CallStoreProcedure(DataSource dataSource, String sql) {
			super(dataSource, sql);
			compile();
		}

		public CallStoreProcedure(JdbcTemplate jdbcTemplate, String sql) {
			setJdbcTemplate(jdbcTemplate);
			setSql(sql);
		}

		@SuppressWarnings("unused")
		public CallStoreProcedure(JdbcTemplate jdbcTemplate, String sql, List<SqlParameter> declareParameterList) {
			setJdbcTemplate(jdbcTemplate);
			setSql(sql);
			for (int i = 0; i < declareParameterList.size(); ++i) {
				SqlParameter parameter = (SqlParameter) declareParameterList.get(i);
				declareParameter(parameter);
			}
			compile();
		}
	}

}
