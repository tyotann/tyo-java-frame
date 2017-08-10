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

	// 这里拦截有点问题，只支持非事务的，或者是与事务平级的
	// 因为如果事务，在事务开始的时候已经获得了dataSource(一般这个时候是在service入口处)，后面再修改dataSource的route值，已经没有用了
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
			}

			// 如果当前线程存在事务,且事务的数据源与本次数据源不一致，则需要新开事务,因为每次事务都会获取数据源
			if (StringUtils.isNotBlank(route) && TransactionSynchronizationManager.isActualTransactionActive() && !preRoute.equals(route)) {

				// 则新开启事务
				transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

				return transactionTemplate.execute(new TransactionCallback<Object>() {

					@Override
					public Object doInTransaction(TransactionStatus status) {
						try {
							return pjp.proceed();
						} catch (Throwable e) {
							status.setRollbackOnly();
							logger.error(e.getMessage(), e);
						}
						return null;
					}
				});
			} else {
				return pjp.proceed();
			}
		} finally {

			// 恢复原有路由
			DynamicDataSourceManager.setRoute(preRoute);
		}
	}
}