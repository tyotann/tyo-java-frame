package com.ihidea.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.alibaba.druid.support.http.util.IPAddress;
import com.alibaba.druid.support.http.util.IPRange;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.pageLimit.PageLimit;
import com.ihidea.core.support.pageLimit.PageLimitHolderFilter;
import com.ihidea.core.support.session.SessionContext;
import com.ihidea.core.support.session.SessionInfo;

/**
 * 涉及业务的共通
 * 
 * @author TYOTANN
 */
public class CommonUtilsEx {

	/**
	 * json转化成存储过程调用XML参数
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static String toXml(String jsonStr) throws Exception {

		Class clz = jsonStr.trim().indexOf("[") == 0 ? ArrayList.class : HashMap.class ;

		String xml = XMLUtilsEx
				.serialize(JSONUtilsEx.deserialize(jsonStr, clz));

		// 加入分页
		PageLimit pl = PageLimitHolderFilter.getContext();

		if (pl != null) {
			xml = xml + "<limitEnable>" + String.valueOf(pl.limited()) + "</limitEnable>";

			if (pl.limited()) {
				xml = xml + "<startResult>" + String.valueOf(pl.getStartRowNo()) + "</startResult>";
				xml = xml + "<endResult>" + String.valueOf(pl.getEndRowNo()) + "</endResult>";
			}
		}

		xml = "<ywsj>" + xml + getSessionInfoXml() + "</ywsj>";
		return StringUtilsEx.filterSQL(xml);
	}

	/**
	 * map转化成存储过程调用XML参数
	 * 
	 * @param param
	 * @return
	 */
	public static String toXml(Map<String, Object> param) throws Exception {

		// 加入分页
		PageLimit pl = PageLimitHolderFilter.getContext();

		if (pl != null) {
			param.put("limitEnable", String.valueOf(pl.limited()));

			if (pl.limited()) {
				param.put("startResult", String.valueOf(pl.getStartRowNo()));
				param.put("endResult", String.valueOf(pl.getEndRowNo()));
			}
		}

		return StringUtilsEx.filterSQL("<ywsj>" + XMLUtilsEx.serialize(param) + getSessionInfoXml() + "</ywsj>");
	}

	private static String getSessionInfoXml() {

		SessionInfo session = SessionContext.getSessionInfo();

		return session != null ? ("<userid>" + session.getUserId() + "</userid><appid>" + CoreConstants.appId + "</appid>")
				: "<appid>" + CoreConstants.appId + "</appid>";
	}

	// 因为future自身10秒超时,所以这里设置20sec自动失效
	private static Cache<String, Set<String>> allowIpAddressMap = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).weakValues()
			.build();

	public static boolean isIPAddressInRange(String remoteAddress, String ipRanges) {

		if (StringUtils.isNotBlank(remoteAddress)) {

			Set<String> allowIpSet = allowIpAddressMap.getIfPresent(ipRanges);

			// 如果这个IP地址之前已经验证过,则直接通过
			if (allowIpSet != null && allowIpSet.contains(remoteAddress)) {
				return true;
			}

			// 如果没有初始化range列表，则先初始化range列表
			if (StringUtils.isNotBlank(ipRanges)) {
				for (String ipRange : Arrays.asList(ipRanges.split(","))) {

					IPRange ip = new IPRange(ipRange);

					if (ip != null && ip.getIPAddress() != null && ip.isIPAddressInRange(new IPAddress(remoteAddress))) {

						if (allowIpSet == null) {
							allowIpSet = new HashSet<String>();
							allowIpSet.add(remoteAddress);
							allowIpAddressMap.put(ipRanges, allowIpSet);
						}

						return true;
					}
				}
			}
		}

		return false;
	}

}
