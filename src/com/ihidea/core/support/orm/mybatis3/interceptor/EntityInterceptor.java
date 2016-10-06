package com.ihidea.core.support.orm.mybatis3.interceptor;

import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import com.ihidea.core.base.CoreEntity;

@Intercepts({
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class }) })
public class EntityInterceptor implements Interceptor {
	public Object intercept(Invocation invocation) throws Throwable {
		Object entity = invocation.getArgs()[1];

		if (entity instanceof CoreEntity) {
			BeanUtils.describe(entity);
		}

		return invocation.proceed();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
	}
}