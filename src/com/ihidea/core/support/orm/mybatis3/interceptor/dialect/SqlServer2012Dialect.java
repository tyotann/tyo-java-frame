package com.ihidea.core.support.orm.mybatis3.interceptor.dialect;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SqlServer2012Dialect extends Dialect {

	/**
	 * 是否支持limit
	 * @return 支持返回true，否则返回false
	 */
	public boolean supportsLimit() {
		return true;
	}

	/**
	 * 是否支持偏移游标
	 * @return 支持返回true，否则返回false
	 */
	public boolean supportsLimitOffset() {
		return true;
	}

	/**
	 * 将sql变成分页sql语句,提供将offset及limit使用占位符(placeholder)替换.
	 * @param sql 执行的sql语句
	 * @param offset 偏移量
	 * @param limit 范围大小
	 * @param offsetPlaceholder 偏移处理器
	 * @param limitPlaceholder limit处理器
	 * @return 添加分页后的sql语句
	 */
	public String getLimitString(String sql, int offset, int limit) {

		final int tempInt = 100;

		sql = sql.trim();

		StringBuffer pagingSelect = new StringBuffer(sql.length() + tempInt);

		pagingSelect.append(sql);

		// order by前后可能不是空格
		if (StringUtils.indexOf(sql.toLowerCase(), "order by") > -1) {
			pagingSelect.append(" offset " + offset + " rows fetch next " + limit + " rows only");
		} else {
			pagingSelect.append(" order by 1 offset " + offset + " rows fetch next " + limit + " rows only");
		}

		return pagingSelect.toString();
	}

	// select xx from aa order by 1-->select count(1) as count from (select top 100 percent xx from aa order by 1) as frame_count
	public String getCountString(String sql) {
		return "select count(1) as count from (select top 100 percent " + StringUtils.substring(sql, 6) + ") as frame_count";
	}

	public String getSysdateString() {
		return "select getdate()";
	}

}
