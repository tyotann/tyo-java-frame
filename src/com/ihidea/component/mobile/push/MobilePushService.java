package com.ihidea.component.mobile.push;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ihidea.core.support.exception.ServiceException;

@Lazy
@Service
public class MobilePushService {

	private Log logger = LogFactory.getLog(MobilePushService.class);

	@Autowired
	private JPushAndroid jPushAndroid;

	@Autowired
	private JPushIOS jPushIOS;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {
		getIPushImpl(appid, "android").pushToUser(appid, userId, pushEntity);
		getIPushImpl(appid, "ios").pushToUser(appid, userId, pushEntity);
	}

	public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity) {
		getIPushImpl(appid, "android").pushToTag(appid, tagName, pushEntity);
		getIPushImpl(appid, "ios").pushToTag(appid, tagName, pushEntity);
	}

	public void pushToUserList(String appid, List<String> userIdList, MobilePushEntity pushEntity) {
		getIPushImpl(appid, "android").pushToUserList(appid, userIdList, pushEntity);
		getIPushImpl(appid, "ios").pushToUserList(appid, userIdList, pushEntity);
	}

	/**
	 * 推送消息到指定设备
	 * @param deviceTokenList
	 * @param pushEntity
	 */
	public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity) {
		Map<String, List<String>> splitTokenMap = splitToken(deviceTokenList);
		getIPushImpl(appid, "android").pushToDevice(appid, splitTokenMap.get("android"), pushEntity);
		getIPushImpl(appid, "ios").pushToDevice(appid, splitTokenMap.get("ios"), pushEntity);
	}

	/**
	 * 发送至每个应用
	 * @param appid
	 */
	public void pushToApp(String appid, MobilePushEntity pushEntity) {
		getIPushImpl(appid, "android").pushToApp(appid, pushEntity);
		getIPushImpl(appid, "ios").pushToApp(appid, pushEntity);
	}

	public void pushToPlatform(String appid, String platform, MobilePushEntity pushEntity) {

		if (StringUtils.isBlank(platform)) {
			pushToApp(appid, pushEntity);
		} else {
			if ("android".equals(platform)) {
				getIPushImpl(appid, "android").pushToApp(appid, pushEntity);
			} else if ("ios".equals(platform)) {
				getIPushImpl(appid, "ios").pushToApp(appid, pushEntity);
			} else {
				throw new ServiceException("请输入正确的platform类别:android or ios");
			}
		}
	}

	/**
	 * 是否启用推送
	 * @param deviceToken
	 * @param status 0:不启用,1:启用,默认为启用
	 * @param accountId
	 * @param appid
	 */
	// public void enablePush(String deviceToken, boolean access, String appid, String accountId) {
	//
	// int updateCnt = jdbcTemplate.update("update cpt_notify set open_type = ?, account_id = ?, appid = ? where device_token = ?",
	// new Object[] { (access ? 1 : 0), (StringUtils.isBlank(accountId) ? StringUtils.EMPTY : accountId), appid, deviceToken });
	//
	// if (updateCnt == 0) {
	// jdbcTemplate.update("insert into cpt_notify (platform, device_token, open_type, un_read_cnt, status, account_id, appid) values"
	// + "(?, ?, ?, 0, 1, ?, ?)", new Object[] { MobilePushService.getPlatformWithDeviceToken(deviceToken), deviceToken,
	// (access ? 1 : 0), (StringUtils.isBlank(accountId) ? StringUtils.EMPTY : accountId), appid });
	// }
	// }

	/**
	 * 用户已读取消息
	 * @param deviceToken
	 * @param msgIdList
	 */
	// public void readMsg(String deviceToken, List<String> msgIdList) {
	//
	// if (msgIdList == null || msgIdList.size() == 0) {
	// jdbcTemplate.update("update cpt_notify set un_read_cnt = 0 where device_token = ?", deviceToken);
	// }
	// }

	/*********************************** 内部使用接口 ******************************/

	private Map<String, List<String>> splitToken(List<String> deviceTokenList) {

		Map<String, List<String>> result = new HashMap<String, List<String>>();

		List<String> iosDeviceToken = new ArrayList<String>();
		result.put("ios", iosDeviceToken);

		List<String> androidDeviceToken = new ArrayList<String>();
		result.put("android", androidDeviceToken);

		if (deviceTokenList != null) {
			for (String deviceToken : deviceTokenList) {

				if (getPlatformWithDeviceToken(deviceToken) == 0) {
					iosDeviceToken.add(deviceToken);
				} else {
					androidDeviceToken.add(deviceToken);
				}
			}
		}

		return result;
	}

	// 0:IOS,1:android
	protected static Integer getPlatformWithDeviceToken(String deviceToken) {
		return (deviceToken.length() == 64) ? 0 : 1;
	}

	/**
	 * 得到未读消息数
	 * @param deviceToken
	 * @return
	 */
	// protected Integer getUnReadCnt(String deviceToken) {
	// return jdbcTemplate.queryForObject("select nvl(sum(a.un_read_cnt), 0) from cpt_notify a where a.device_token = ?",
	// new Object[] { deviceToken }, Integer.class);
	// }

	// protected Integer addUnReadCnt(String deviceToken) {
	//
	// int updateCnt = jdbcTemplate.update("update cpt_notify set un_read_cnt = un_read_cnt+1 where device_token = ?",
	// new Object[] { deviceToken });
	//
	// if (updateCnt == 0) {
	// jdbcTemplate.update("insert into cpt_notify (platform, device_token, open_type, un_read_cnt, status) values"
	// + "(?, ?, 1, 1, 1)", new Object[] { MobilePushService.getPlatformWithDeviceToken(deviceToken), deviceToken });
	//
	// updateCnt = 1;
	// }
	//
	// return updateCnt;
	// }

	// protected List<String> getDeviceTokenByAppid(String appid) {
	// return jdbcTemplate.queryForList("select device_token from cpt_notify a where a.appid = ? and status = 1", new Object[] { appid },
	// String.class);
	// }

	/**
	 * 返回API MasterSecret,AppKey
	 * @param appid
	 * @return
	 */
	protected List<String[]> getJpushKeyInfo(String appid, String apiMasterSecret, String appKey) {

		List<Map<String, Object>> resultList = null;

		List<String[]> resultA = new ArrayList<String[]>();

		try {

			// 如果设定了自定义key，则使用自定义，否则进行数据库查询
			if (StringUtils.isNotBlank(apiMasterSecret) && StringUtils.isNotBlank(appKey)) {
				resultA.add(new String[] { apiMasterSecret, appKey });
			} else {
				resultList = jdbcTemplate.queryForList("select * from cpt_notify_jpush where appid = ? and status = 1",
						new Object[] { appid });

				if (resultList != null && resultList.size() > 0) {
					for (Map<String, Object> result : resultList) {
						resultA.add(new String[] { (String) result.get("api_master_secret"), (String) result.get("app_key") });
					}
				}
			}

		} catch (Exception e) {
		}

		if (resultA.size() == 0) {
			throw new ServiceException("请配置好推送的APPKEY");
		}

		return resultA;
	}

	// protected String getDeviceIdByUserId(String userId) {
	// return jdbcTemplate.queryForObject("select device_token from cpt_notify a where a.account_id = ? and status = 1",
	// new Object[] { userId }, String.class);
	// }

	private IPush getIPushImpl(String appid, String platform) {

		BigDecimal cnt = jdbcTemplate.queryForObject(
				"select count(1) from cpt_notify_jpush where appid = ? and platform like ? and status=1", new Object[] { appid,
						"%" + platform + "%" }, BigDecimal.class);

		if (cnt.intValue() > 0) {

			if ("android".equals(platform)) {
				return jPushAndroid;
			} else if ("ios".equals(platform)) {
				return jPushIOS;
			}
		} else {
			logger.error("平台:" + platform + ";未找到具体的推送实现!");
		}

		return new BlankPush();
	}
}
