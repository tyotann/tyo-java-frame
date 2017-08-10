package com.ihidea.component.mobile.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ihidea.component.mobile.push.aliPush.AliPush;
import com.ihidea.component.mobile.push.jPush.JPushAndroid;
import com.ihidea.component.mobile.push.jPush.JPushIOS;
import com.ihidea.core.util.PropertyUtils;

@Lazy
@Service
public class MobilePushService {

    private static final String PUSH_CHANNEL = PropertyUtils.getProperty("push.channel");
    
    @Autowired
    private JPushAndroid jPushAndroid;
    
    @Autowired
    private JPushIOS jPushIOS;
    
    public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_android")) {
            jPushAndroid.pushToUser(appid, userId, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_iOS")) {
            jPushIOS.pushToUser(appid, userId, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "aliPush")) {
            new AliPush().pushToUser(appid, userId, pushEntity);
        }
    }
    
    public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity) {
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_android")) {
            jPushAndroid.pushToTag(appid, tagName, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_iOS")) {
            jPushIOS.pushToTag(appid, tagName, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "aliPush")) {
            new AliPush().pushToTag(appid, tagName, pushEntity);
        }
    }
    
    public void pushToUserList(String appid, List<String> userIdList, MobilePushEntity pushEntity) {
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_android")) {
            jPushAndroid.pushToUserList(appid, userIdList, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_iOS")) {
            jPushIOS.pushToUserList(appid, userIdList, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "aliPush")) {
            new AliPush().pushToUserList(appid, userIdList, pushEntity);
        }
    }
    
    /**
     * 推送消息到指定设备
     * 
     * @param deviceTokenList
     * @param pushEntity
     */
    public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity) {
        
        Map<String, List<String>> splitTokenMap = splitToken(deviceTokenList);
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_android")) {
            jPushAndroid.pushToDevice(appid, splitTokenMap.get("android"), pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_iOS")) {
            jPushIOS.pushToDevice(appid, splitTokenMap.get("android"), pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "aliPush")) {
            new AliPush().pushToDevice(appid, splitTokenMap.get("android"), pushEntity);
        }
    }
    
    /**
     * 发送至每个应用
     * 
     * @param appid
     */
    public void pushToApp(String appid, MobilePushEntity pushEntity) {
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_android")) {
            jPushAndroid.pushToApp(appid, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "jpush_iOS")) {
            jPushIOS.pushToApp(appid, pushEntity);
        }
        
        if (StringUtils.contains(PUSH_CHANNEL, "aliPush")) {
            new AliPush().pushToApp(appid, pushEntity);
        }
    }

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
}
