package com.ihidea.component.mobile.push.jPush;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ihidea.component.mobile.push.IPush;
import com.ihidea.component.mobile.push.MobilePushEntity;

import cn.jpush.api.common.DeviceType;

@Lazy
@Component
public class JPushAndroid implements IPush {

	@Autowired
	private JPushService jPushService;

	public void pushToApp(String appid, MobilePushEntity pushEntity) {
		jPushService.sendNotification2App(appid, pushEntity, DeviceType.Android);
	}

	public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity) {
		jPushService.sendNotification2Tag(appid, tagName, pushEntity, DeviceType.Android);
	}

	public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {

		if (StringUtils.isNotBlank(userId)) {
			jPushService.sendNotification2Alias(appid, userId, pushEntity, DeviceType.Android);
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

	public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity) {

		if (deviceTokenList != null && deviceTokenList.size() > 0) {

			for (String deviceToken : deviceTokenList) {
				if (StringUtils.isNotBlank(deviceToken)) {
					jPushService.sendNotification2Reg(appid, deviceToken, pushEntity, DeviceType.Android);
				}
			}
		}
	}

}
