package com.ihidea.component.mobile.push;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import cn.jpush.api.common.DeviceType;

@Lazy
@Component
public class JPushIOS implements IPush {

	@Autowired
	private JPushService jPushService;

	public void pushToApp(String appid, MobilePushEntity pushEntity) {
		jPushService.sendNotification2App(appid, pushEntity, DeviceType.IOS);
	}

	public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity) {
		jPushService.sendNotification2Tag(appid, tagName, pushEntity, DeviceType.IOS);
	}

	public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {

		if (StringUtils.isNotBlank(userId)) {
			jPushService.sendNotification2Alias(appid, userId, pushEntity, DeviceType.IOS);
		}
	}

	public void pushToUserList(String appid, List<String> userIdList, MobilePushEntity pushEntity) {
		if (userIdList != null && userIdList.size() > 0) {
			for (String userId : userIdList) {
				if (StringUtils.isNotBlank(userId)) {
					pushToUser(appid, userId, pushEntity);
				}
			}
		}
	}

	/**
	 * 根据设备列表推送
	 */
	public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity) {

		if (deviceTokenList != null && deviceTokenList.size() > 0) {

			for (String deviceToken : deviceTokenList) {
				if (StringUtils.isNotBlank(deviceToken)) {
					jPushService.sendNotification2Reg(appid, deviceToken, pushEntity, DeviceType.IOS);
				}
			}
		}
	}

}
