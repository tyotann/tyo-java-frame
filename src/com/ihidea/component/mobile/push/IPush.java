package com.ihidea.component.mobile.push;

import java.util.List;

public interface IPush {

	public void pushToUser(String appid, String userId, MobilePushEntity pushEntity);

	public void pushToUserList(String appid, List<String> userIdList, MobilePushEntity pushEntity);

	public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity);

	public void pushToApp(String appid, MobilePushEntity pushEntity);

	public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity);
}
