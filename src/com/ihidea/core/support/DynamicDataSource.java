package com.ihidea.core.support;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

	private static ThreadLocal<String> local = new ThreadLocal<String>();

	@Override
	protected Object determineCurrentLookupKey() {
		return local.get() == null ? "dataSource" : local.get();
	}

	// ---------------------------------------------------------------------------------------------------

	/**
	 * 设置数据源路径
	 */
	public static void setRoute(String route) {
		if (route==null || route.equals("")){
			route = "dataSource";
		}
		local.set(route);
	}
}
