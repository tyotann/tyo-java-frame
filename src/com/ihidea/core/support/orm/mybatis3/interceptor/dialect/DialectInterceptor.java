package com.ihidea.core.support.orm.mybatis3.interceptor.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.util.PropertyUtils;

@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class }) })
public class DialectInterceptor implements Interceptor {

	protected Log logger = LogFactory.getLog(this.getClass());

	private static Dialect dialect = null;

	/** MappedStatement索引值 */
	private static final int MAPPED_STATEMENT_INDEX = 0;

	/** 参数索引值 */
	private static final int PARAMETER_INDEX = 1;

	/** RowBounds 索引值 */
	private static final int ROWBOUNDS_INDEX = 2;

	@SuppressWarnings("unchecked")
	public Object intercept(Invocation invocation) throws Throwable {

		Object[] invocationArgs = invocation.getArgs();

		MappedStatement ms = (MappedStatement) invocationArgs[MAPPED_STATEMENT_INDEX];

		// 存储过程不分页
		if (ms.getStatementType() != StatementType.CALLABLE) {

			PageLimit pl = PageLimitHolderFilter.getContext();

			// 原始SQL
			String sql = ms.getBoundSql(invocationArgs[PARAMETER_INDEX]).getSql().trim();

			// 当job调用时,pl为null
			if (dialect.supportsLimit() && pl != null && pl.limited() && !pl.isLimited()) {

				logger.debug("开始分页操作进行SQL：" + sql);

				// 总页数为0的话,说明没有查过sql count数
				if (pl.getTotalCount() == 0 && !pl.isOnlyGetRows()) {

					// 重写count的sql
					rewriteCount(sql, ms, invocationArgs);

					Integer totalCount = ((List<Integer>) invocation.proceed()).get(0);

					// 设置分页的总页数
					pl.setTotalCount(totalCount);
				}

				// 重写分页的sql
				rewriteLimit(sql, ms, invocationArgs);

				// 分页完成
				pl.setLimited(true);
			} else if ((pl != null) && (pl.isOnlyGetTotalCnt())) {

				this.logger.debug("仅进行查询记录数的SQL：" + sql);

				rewriteCount(sql, ms, invocationArgs);

				Integer totalCount = (Integer) ((List<Integer>) invocation.proceed()).get(0);

				pl.setTotalCount(totalCount);

				PageLimitHolderFilter.getContext().setOnlyGetTotalCnt(false);
			}
		}

		return invocation.proceed();
	}

	/**
	 * 此处为具体修改的路径
	 * 
	 * @param sql
	 * @return
	 */
	private void rewriteCount(String sql, MappedStatement ms, Object[] args) {

		BoundSql boundSql = ms.getBoundSql(args[PARAMETER_INDEX]);

		args[ROWBOUNDS_INDEX] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);

		args[MAPPED_STATEMENT_INDEX] = copyFromMappedStatement(ms,
				new BoundSqlSqlSource(copyFromBoundSql(ms, boundSql, dialect.getCountString(sql))), true);
	}

	/**
	 * 此处为具体修改的路径
	 * 
	 * @param sql
	 * @return
	 */
	private void rewriteLimit(String sql, MappedStatement ms, Object[] args) {

		PageLimit pl = PageLimitHolderFilter.getContext();

		int offset = pl.getStartRowNo() - 1;
		int limit = pl.getPageLength();

		BoundSql boundSql = ms.getBoundSql(args[PARAMETER_INDEX]);

		if (dialect.supportsLimitOffset()) {
			sql = dialect.getLimitString(sql, offset, limit);
			offset = RowBounds.NO_ROW_OFFSET;
		} else {
			sql = dialect.getLimitString(sql, 0, limit);
		}

		// 本来的分页偏移设置失效(因为已经用原声SQL分页)
		args[ROWBOUNDS_INDEX] = new RowBounds(offset, RowBounds.NO_ROW_LIMIT);

		args[MAPPED_STATEMENT_INDEX] = copyFromMappedStatement(ms, new BoundSqlSqlSource(copyFromBoundSql(ms, boundSql, sql)), false);

		// 分页后设置已经分过页
		pl.setLimited(true);
	}

	private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql) {
		BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
		for (ParameterMapping mapping : boundSql.getParameterMappings()) {
			String prop = mapping.getProperty();
			if (boundSql.hasAdditionalParameter(prop)) {
				newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		return newBoundSql;
	}

	private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource, boolean countMappedStatement) {

		Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());

		// TODO getKeyProperties什么意思?
		if (ms.getKeyProperties() != null) {
			for (String property : ms.getKeyProperties()) {
				builder.keyProperty(property);
			}
		}

		// builder.keyProperty(ms.getKeyProperty());

		// setStatementTimeout()
		builder.timeout(ms.getTimeout());

		// setStatementResultMap()
		builder.parameterMap(ms.getParameterMap());

		// 如果是算count的话,返回结果为Integer型
		if (countMappedStatement) {
			List<ResultMap> tmpList = new ArrayList<ResultMap>();
			tmpList.add(new ResultMap.Builder(ms.getConfiguration(), ms.getId() + "-count", Integer.class, new ArrayList<ResultMapping>())
					.build());
			builder.resultMaps(tmpList);
		} else {
			builder.resultMaps(ms.getResultMaps());
		}

		builder.resultSetType(ms.getResultSetType());

		// setStatementCache()
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());
		return builder.build();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {

		getDialect();

		// String dialectClass = properties.getProperty("dialectClass");
		//
		// try {
		// dialect = (Dialect) Class.forName(dialectClass).newInstance();
		// } catch (Exception e) {
		// throw new RuntimeException("分页类创建出错:" + dialectClass, e);
		// }

	}

	public static class BoundSqlSqlSource implements SqlSource {
		/** BoundSql对象 */
		private BoundSql boundSql;

		/**
		 * 构造方法
		 * 
		 * @param boundSqlParam
		 *            BoundSql对象
		 */
		public BoundSqlSqlSource(BoundSql boundSqlParam) {
			this.boundSql = boundSqlParam;
		}

		/**
		 * getter for boundSql
		 * 
		 * @param parameterObject
		 *            Object
		 * @return BoundSql对象
		 */
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	/**
	 * 得到数据库方言
	 * 
	 * @return
	 */
	public static Dialect getDialect() {

		if (dialect == null) {

			try {

				String database = PropertyUtils.getProperty("database");

				if (StringUtils.isBlank(database) || "oracle".equals(database)) {
					dialect = OracleDialect.class.newInstance();
				} else if ("sqlserver2012".equals(database)) {
					dialect = SqlServer2012Dialect.class.newInstance();
				} else if ("mysql".equals(database)) {
					dialect = MysqlDialect.class.newInstance();
				}
			} catch (Exception e) {
				throw new RuntimeException("分页类创建出错:" + dialect, e);
			}

		}

		return dialect;

	}
}
