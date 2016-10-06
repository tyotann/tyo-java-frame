package com.ihidea.core.support.orm.mybatis3.interceptor;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.SqlSessionFactory;

@Intercepts( { @Signature(type = Executor.class, method = "query", args = { SqlSessionFactory.class }) })
public class IbatisExInterceptor implements Interceptor {

	public Object intercept(Invocation invocation) throws Throwable {
		return invocation.proceed();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties arg0) {
	}

}
