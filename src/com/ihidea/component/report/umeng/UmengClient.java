package com.ihidea.component.report.umeng;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.DateUtilsEx;
import com.ihidea.core.util.HttpClientUtils;
import com.ihidea.core.util.JSONUtilsEx;

public class UmengClient {

	private static Log logger = LogFactory.getLog(UmengClient.class);

	public static String PLATFORM_IPHONE = "iphone";

	public static String PLATFORM_ANDROID = "android";

	public static UmengEntity regUser(String userId, String email) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		params.put("email", email);

		return UmengUtils.queryEntity("http://oauth.umeng.com/api/markets/reg_user", params, false);
	}

	/**
	 * 得到用户token
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static String getUserToken(String userId) throws Exception {

		// TODO

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);

		UmengEntity result = UmengUtils.queryEntity("http://oauth.umeng.com//api/markets/user_token", params, false);

		if (StringUtils.isBlank(result.getCode()) && StringUtils.isBlank(result.getErrcode())) {
			return (String) ((Map) result.getData()).get("auth_token");
		} else {
			throw new ServiceException(result.getErrmsg());
		}
	}

	/**
	 * 分配 appkey, 并开启统计功能
	 * @param userId
	 * @param appid
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static String allocAppKey(String userId, String appid, String appName, String platform) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		params.put("market_app_id", appid + "_" + platform);
		params.put("app_name", appName);
		params.put("app_platform", platform);

		UmengEntity result = UmengUtils.queryEntity("http://oauth.umeng.com/api/markets/alloc_appkey", params, false);

		if (StringUtils.isBlank(result.getCode()) && StringUtils.isBlank(result.getErrcode())) {
			return (String) ((Map) result.getData()).get("app_key");
		} else {
			throw new ServiceException(result.getErrmsg());
		}
	}

	/**
	 * 删除APP账号
	 * @param userId
	 * @param appid
	 * @param platform
	 * @throws Exception
	 */
	public static void deleteAppKey(String userId, String appid, String platform) throws Exception {

		// TODO
		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		params.put("market_app_id", appid + "_" + platform);

		UmengEntity result = UmengUtils.queryEntity("http://oauth.umeng.com/api/markets/delete_app", params, false);

		if (StringUtils.isNotBlank(result.getCode()) || StringUtils.isNotBlank(result.getErrcode())) {
			logger.error("删除友盟APP失败:" + appid + "平台:" + platform);
		}
	}

	// TODO
	@SuppressWarnings("rawtypes")
	public static String getAppKey(String umengUserId, String appid, String platform) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", umengUserId);
		params.put("market_app_id", appid + "_" + platform);

		UmengEntity result = UmengUtils.queryEntity("http://oauth.umeng.com/api/markets/app_key", params, true);
		if (StringUtils.isBlank(result.getCode()) && StringUtils.isBlank(result.getErrcode())) {
			return (String) ((Map) result.getData()).get("app_key");
		} else {
			throw new ServiceException(result.getErrmsg());
		}
	}

	/**
	 * 获取 iframe 地址
	 * @param userId
	 * @param appid
	 * @return
	 * @throws Exception
	 */
	public static String dashboard(String userId, String token, String appid, String platform) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		params.put("market_app_id", appid + "_" + platform);
		return UmengUtils.queryString("http://oauth.umeng.com/api/markets/dashboard", params, true);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> report(String appKey, String token) throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();

		try {

			String resultJSON = HttpClientUtils.get("http://api.umeng.com/channels?appkey=" + appKey + "&auth_token=" + token);

			List<Map<String, Object>> resultList = JSONUtilsEx.deserialize(resultJSON, List.class);

			if (resultList != null && resultList.size() > 0) {

				for (Map<String, Object> channelMap : resultList) {

					// 今日新增用户
					if (result.containsKey("todayUserCount")) {
						result.put("todayUserCount", (Integer) result.get("todayUserCount") + (Integer) channelMap.get("install"));
					} else {
						result.put("todayUserCount", channelMap.get("install"));
					}

					// 今日启动次数(不是实时数据)

					// 活跃用户
					if (result.containsKey("todayStartUser")) {
						result.put("todayStartUser", (Integer) result.get("todayStartUser") + (Integer) channelMap.get("active_user"));
					} else {
						result.put("todayStartUser", channelMap.get("active_user"));
					}

					// 累计用户
					if (result.containsKey("totalUser")) {
						result.put("totalUser", (Integer) result.get("totalUser") + (Integer) channelMap.get("total_install"));
					} else {
						result.put("totalUser", channelMap.get("total_install"));
					}
				}
			}

			// 计算累计启动次数
			// 今日启动次数
			String now = DateUtilsEx.formatToString(new Date(), "yyyy-MM-dd");
			resultJSON = HttpClientUtils.get("http://api.umeng.com/launches?appkey=" + appKey + "&start_date=" + now + "&end_date=" + now
					+ "&auth_token=" + token);

			Map<String, Object> resultMap = JSONUtilsEx.deserialize(resultJSON, Map.class);

			if (resultMap.containsKey("data")) {
				List<Integer> dateDataList = ((Map<String, List<Integer>>) resultMap.get("data")).get("all");
				result.put("todayStartCount", dateDataList.get(0));
			}
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * 获取 iframe 地址
	 * @param userId
	 * @param appid
	 * @return
	 * @throws Exception
	 */
	public static String redirect(String userId) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		return UmengUtils.queryUrl("http://oauth.umeng.com/api/markets/redirect_apps", params);
	}

	/**
	 * 跳转到用户指定报表
	 * @param userId
	 * @param appid
	 * @param platform
	 * @return
	 * @throws Exception
	 */
	public static String redirect(String userId, String appid, String platform) throws Exception {

		// TODO
		Map<String, String> params = new HashMap<String, String>();
		params.put("market_user_id", userId);
		params.put("market_app_id", appid + "_" + platform);

		return UmengUtils.queryUrl("http://oauth.umeng.com/api/markets/redirect_reports", params);
	}

	public static void main(String[] args) throws Exception {

		// Map<String, String> test = new HashMap<String, String>();
		// test.put("market_app_id", "bae4b3c6-f620-4b24-80b5-ab0fdabc0596_iphone");
		// test.put("app_platform", "iphone");
		// test.put("app_name", "20140611181359_电商4期_ios");
		// test.put("market_user_id", "012138b7-34b9-4959-b74a-d89f453763a0");
		//
		// UmengUtils.queryUrl("http://oauth.umeng.com/api/markets/alloc_appkey", test);

		// Map<String, String> params = new HashMap<String, String>();
		// params.put("market_user_id", "d91119b7-b5d2-4808-8ee2-016046ee42cd");
		// params.put("email", "45392841@qq.com");

		// System.out.println(regUser("d91119b7-b5d2-4808-8ee2-016046ee42cxxx1", "d91119b7-b5d2-4808-8ee2-016046ee42cxxx@iappk.com"));

		// {email=d91119b7-b5d2-4808-8ee2-016046ee42ca@iappk.com, auth_token=EyTrlMUWapz7MH6r7kYM}

		// "正式测试APP_android",
		// UmengClient.PLATFORM_ANDROID));
		//
		// System.out.println(allocAppKey("d91119b7-b5d2-4808-8ee2-016046ee42ca", "d91119b7-b5d2-4801-8ae2-016046ee42ca_2", "正式测试APP_ios",
		// UmengClient.PLATFORM_IPHONE));
		// 537d87746c738ff739000001

//		System.out.println(report("53a818c66c738f0dd300020a", "Td5r4IMing8ZTAQHHCuK"));

		// System.out.println( allocAppKey("d91119b7-b5d2-4808-8ee2-016046ee42cf", "d91119b7-b5d2-4801-8ae2-016046ee42c2", "测试APP_ios"));

		// {app_key=537d46cf6c738f66d1000014}
		// System.out.println(entity);

		// System.out.println(getAppKey("0d3fd872-a190-4f92-8bad-c5d230509094", "8f5cab7a-dbf8-4069-a246-342d7544afa9", "1"));
		// System.out.println(getAppKey("d91119b7-b5d2-4808-8ee2-016046ee42ca", "d91119b7-b5d2-4801-8ae2-016046ee42ca_2"));

		// System.out.println(dashboard("d91119b7-b5d2-4808-8ee2-016046ee42cf", "11"));

		// System.out.println(allocAppKey("0d3fd872-a190-4f92-8bad-c5d230509094", "8f5cab7a-dbf8-4069-a246-342d7544afa9", "猪宝贝", "1"));

		System.out.println(redirect("7a346121-4ed1-41ad-9c43-201c2fda4ccb"));
		 System.out.println(redirect("7a346121-4ed1-41ad-9c43-201c2fda4ccb","5066bd69-4a2e-4a66-9046-33133eb9d6e3",UmengClient.PLATFORM_ANDROID));

		// HttpClientUtils.get("http://api.umeng.com/launches?appkey="
		// + getAppKey("995df5ed-e7cc-4ed5-a7cd-fff01627edfb", "5a47e0a9-165e-4688-bbed-9f4b795df2c8") + "&auth_token="
		// + "UwoVHYkKow34YrzceCy1");
	}
}
