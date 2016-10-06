package com.ihidea.core.support.orm.mybatis3.interceptor.dialect;

public abstract class Dialect {

	/**
	 * 是否支持limit
	 * @return 支持返回true，否则返回false
	 */
	public boolean supportsLimit() {
		return false;
	}

	/**
	 * 是否支持偏移游标
	 * @return 支持返回true，否则返回false
	 */
	public boolean supportsLimitOffset() {
		return supportsLimit();
	}

	/**
	 * 将sql变成分页sql语句,直接使用offset,limit的值作为占位符
	 * @param sql 执行的sql语句
	 * @param offset 偏移量
	 * @param limit 范围大小
	 * @return 添加分页后的sql语句
	 */
	public abstract String getLimitString(String sql, int offset, int limit);

	/**
	 * 将sql变成count sql语句
	 * @param sql
	 * @return count后的sql语句
	 */
	public abstract String getCountString(String sql);

	/**
	 * 得到系统当前时间的SQL
	 * @return
	 */
	public abstract String getSysdateString();

}
