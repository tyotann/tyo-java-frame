package com.ihidea.core.support.dataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSourceManager extends AbstractRoutingDataSource {

	private static ThreadLocal<String> local = new ThreadLocal<String>();

	private static String defaultDataSource = "dataSource";

	public static String getDefaultDataSource() {
		return defaultDataSource;
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return getRoute();
	}

	// ---------------------------------------------------------------------------------------------------

	/**
	 * 设置数据源路径
	 */
	public static void setRoute(String route) {
		if (StringUtils.isNotBlank(route)) {
			local.set(route);
		}
	}

	public static String getRoute() {
		if (StringUtils.isBlank(local.get())) {
			local.set(getDefaultDataSource());
		}
		return local.get();
	}
}
