package com.ihidea.core.support.orm.mybatis3.interceptor.dialect;

import org.springframework.stereotype.Component;

@Component
public class OracleDialect extends Dialect {

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
		final int temp = 11;
		final int tempInt = 100;
		sql = sql.trim();
		boolean isForUpdate = false;
		if (sql.toLowerCase().endsWith(" for update")) {
			sql = sql.substring(0, sql.length() - temp);
			isForUpdate = true;
		}

		StringBuffer pagingSelect = new StringBuffer(sql.length() + tempInt);
		// if (offset > 0) {
		pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		// }

		// else {
		// pagingSelect.append("select * from ( ");
		// }
		pagingSelect.append(sql);
		// if (offset > 0) {
		String endString = String.valueOf(offset) + "+" + String.valueOf(limit);
		pagingSelect.append(" ) row_ ) where rownum_ <= " + endString + " and rownum_ > " + String.valueOf(offset));
		// }

		// else {
		// pagingSelect.append(" ) where rownum <= " + String.valueOf(limit));
		// }

		if (isForUpdate) {
			pagingSelect.append(" for update");
		}

		return pagingSelect.toString();
	}

	public String getCountString(String sql) {
		return "select count(1) as count from (" + sql + ")";
	}

	public String getSysdateString() {
		return "select sysdate from dual";
	}

}
