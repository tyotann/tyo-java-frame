package com.ihidea.core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 系统配置文件装载
 * 
 * @author TYOTANN
 */
public class PropertyUtils {

	private final static Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

	private static final String CONFIG_FILE = "application.properties";

	private static Map<String, Properties> pMap = new HashMap<String, Properties>();

	private synchronized static Properties get(String propertyFileName) {

		propertyFileName = StringUtils.isNotBlank(propertyFileName) ? propertyFileName : CONFIG_FILE;

		Properties p = pMap.get(propertyFileName);

		if (p == null) {

			try {

				p = PropertiesLoaderUtils.loadProperties(new ClassPathResource(propertyFileName));

				if (p != null) {
					pMap.put(propertyFileName, p);
					logger.info("配置文件[{}],装载成功~", new Object[] { propertyFileName });
				} else {
					logger.info("配置文件[{}],装载失败!", new Object[] { propertyFileName });
				}
			} catch (IOException e) {
				logger.error("配置文件 [" + propertyFileName + "] ,装载异常:" + e.getMessage(), e);
			}
		}

		return p;
	}

	public static String getProperty(String propertyFileName, String key, String defaultValue) {
		return StringUtils.defaultIfEmpty(get(propertyFileName).getProperty(key), defaultValue);
	}

	public static String getProperty(String key, String defaultValue) {
		return getProperty(null, key, defaultValue);
	}

	public static String getProperty(String key) {
		return getProperty(null, key, null);
	}

	public static void reload() {
		reload(CONFIG_FILE);
	}

	public static void reload(String filename) {
		if (pMap.get(filename) != null) {
			pMap.get(filename).clear();
		}
		get(filename);
	}
}
