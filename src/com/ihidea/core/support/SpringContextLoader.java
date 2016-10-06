package com.ihidea.core.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ihidea.core.util.SystemUtilsEx;

/**
 * @author TYOTANN
 */
public class SpringContextLoader implements ServletContextListener {

	private final static Logger logger = LoggerFactory.getLogger(SpringContextLoader.class);

	private static ApplicationContext applicationContext = null;

	public void contextInitialized(ServletContextEvent sce) {

		// 绑定RMI主机IP
		try {
			logger.info("绑定RMI主机IP:" + SystemUtilsEx.getHostIp());
			System.setProperty("java.rmi.server.hostname", SystemUtilsEx.getHostIp());

			logger.info("系统绑定RMI主机IP成功:" + SystemUtilsEx.getHostIp());

			// 解决连接超时
			// System.setProperty("sun.rmi.transport.tcp.responseTimeout", new
			// Integer(Integer.MAX_VALUE).toString());
		} catch (Exception e) {
			logger.error("系统绑定RMI主机IP失败", e);
		}

		// 装载Spring的Context
		try {
			applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
			logger.info("系统spring配置装载成功~");
		} catch (Exception e) {
			logger.error("系统spring配置装载失败", e);
		}

		// 装载任务
		// try {
		// SpringContextLoader.getBean(TaskEnginee.class).startScheduler();
		// logger.info("系统装载任务成功~");
		// } catch (Exception e) {
		// logger.error("系统装载任务失败", e);
		// }
	}

	public void contextDestroyed(ServletContextEvent sce) {

		// 卸载任务
		// try {
		// SpringContextLoader.getBean(TaskEnginee.class).stopScheduler();
		// logger.info("系统卸载任务成功~");
		// } catch (Exception e) {
		// logger.error("系统卸载任务失败", e);
		// }

		// 最关键的，在tomcat的server.xml中加入
		// <Listener
		// className="org.apache.catalina.core.JreMemoryLeakPreventionListener"
		// />

		// 卸载数据库驱动,防止因MySQL驱动导致memory leak
		try {

			// 卸载所有驱动
			Enumeration<Driver> driverEnum = DriverManager.getDrivers();
			if (driverEnum != null) {
				while (driverEnum.hasMoreElements()) {
					DriverManager.deregisterDriver(driverEnum.nextElement());
				}
			}

			logger.info("系统卸载库驱动成功~");
		} catch (Throwable th) {
			logger.info("系统卸载库驱动出错:" + th.getMessage());
		}

		try {
			Class<?> cls = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
			Method method = (cls == null ? null : cls.getMethod("shutdown"));
			if (method != null) {
				logger.info("系统卸载数据库驱动-开始清除MySQL线程...");
				method.invoke(null);
				logger.info("系统卸载数据库驱动-清除MySQL线程成功~");
			}
		} catch (Throwable th) {
			logger.info("系统卸载数据库驱动-清除MySQL线程出错:" + th.getMessage());
		}

		try {
			// 卸载Spring的Context
			applicationContext = null;
			logger.info("系统spring配置卸载成功~");
		} catch (Exception e) {
			logger.error("系统spring配置卸载失败", e);
		}
	}

	public static void setSpringContext(ApplicationContext applicationContext) {
		SpringContextLoader.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {

		if (applicationContext == null) {
			applicationContext = ContextLoader.getCurrentWebApplicationContext();
		}

		return applicationContext;
	}

	// ---------------------------------------------------

	/**
	 * 通过对象名,得到spring管理的对象
	 * 
	 * @param beanName
	 *            对象名
	 * @return
	 */
	public static Object getBean(String beanName) {

		if (!getApplicationContext().containsBean(beanName)) {

			// 第一位如果是小写就大写,如果大写就小写
			if (!beanName.substring(0, 1).toLowerCase().equals(beanName.substring(0, 1))) {
				beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
			} else if (!beanName.substring(0, 1).toUpperCase().equals(beanName.substring(0, 1))) {
				beanName = beanName.substring(0, 1).toUpperCase() + beanName.substring(1);
			}
		}

		return getApplicationContext().getBean(beanName);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName, Class<T> clazz) {
		return (T) SpringContextLoader.getBean(beanName);
	}

	public static <T> T getBean(Class<T> clazz) {
		String[] beans = getApplicationContext().getBeanNamesForType(clazz);
		if (beans != null && beans.length > 0) {
			return getBean(beans[0], clazz);
		}
		return null;
	}

	/**
	 * 通过注解得到类型
	 * 
	 * @param clazz
	 * @return
	 */
	public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> clazz) {
		return getApplicationContext().getBeansWithAnnotation(clazz);

	}
}
