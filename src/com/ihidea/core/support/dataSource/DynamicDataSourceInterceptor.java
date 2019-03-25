package com.ihidea.core.support.dataSource;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author TYOTANN
 *
 */
@Aspect
@Component
public class DynamicDataSourceInterceptor {

	private final static Logger logger = LoggerFactory.getLogger(DynamicDataSourceInterceptor.class);

	@Autowired
	private TransactionTemplate transactionTemplate;

	// 无需考虑事务的情况,因为如果存在事务,那么方法一开始就已经获取了master的connection, 此时再设置route都是不生效的
	// 并且这样也符合逻辑:凡是有事务的方法都只能使用master库, 即使后续再设置route也不能改变，直到事务结束
	@Pointcut("@annotation(com.ihidea.core.support.dataSource.DynamicDataSource)")
	public void function() {
	}

	@Around("function()")
	public Object around(final ProceedingJoinPoint pjp) throws Throwable {

		// 得到注解信息
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		DynamicDataSource dataSource = signature.getMethod().getAnnotation(DynamicDataSource.class);

		String preRoute = DynamicDataSourceManager.getRoute();
		String route = dataSource.key();
		try {

			if (StringUtils.isNotBlank(route)) {
				DynamicDataSourceManager.setRoute(route);
				if(logger.isDebugEnabled()) {
					logger.debug("设置数据源为:" + route);
				}
			}
			return pjp.proceed();
		} finally {

			// 恢复原有路由
			DynamicDataSourceManager.setRoute(preRoute);
		}
	}
}