package com.ihidea.core.support.orm.mybatis3;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.jdbc.support.lob.LobHandler;

import com.ihidea.core.support.SpringContextLoader;

/**
 * 存储过程的游标解析器
 * @author TYOTANN
 */
public class ResultSetTypeHandler extends BaseTypeHandler<List<Map<String, Object>>> {

	@Override
	public List<Map<String, Object>> getNullableResult(ResultSet arg0, String arg1) throws SQLException {
		return null;
	}

	@Override
	public List<Map<String, Object>> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		ResultSet rs = null;

		// 游标关闭时会直接报出异常
		try {
			rs = (ResultSet) cs.getObject(columnIndex);
		} catch (SQLException e) {
		}

		if (rs != null && !rs.isClosed()) {

			ResultSetMetaData data = rs.getMetaData();
			int columnCnt = data.getColumnCount();

			while (rs.next()) {
				Map<String, Object> rowMap = new HashMap<String, Object>();
				for (int i = 1; i <= columnCnt; i++) {
					String colName = data.getColumnName(i).toLowerCase();
					Object colValue = rs.getObject(colName);

					// TODO 类型处理
					if ("CLOB".equals(data.getColumnTypeName(i))) {
						LobHandler lobHandler = SpringContextLoader.getBean(LobHandler.class);
						rowMap.put(colName, colValue == null ? StringUtils.EMPTY : lobHandler.getClobAsString(rs, i));
					} else {
						rowMap.put(colName, colValue == null ? StringUtils.EMPTY : colValue.toString());
					}
				}
				result.add(rowMap);
			}
		}

		return result;
	}

	@Override
	public void setNonNullParameter(PreparedStatement arg0, int arg1, List<Map<String, Object>> arg2, JdbcType arg3) throws SQLException {
	}

	@Override
	public List<Map<String, Object>> getNullableResult(ResultSet arg0, int arg1) throws SQLException {
		return null;
	}

}
