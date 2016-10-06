package com.ihidea.component.mobile.push;

import java.util.List;

public class BlankPush implements IPush {

	@Override
	public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushToUserList(String appid, List<String> userIdList, MobilePushEntity pushEntity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushToDevice(String appid, List<String> deviceTokenList, MobilePushEntity pushEntity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushToApp(String appid, MobilePushEntity pushEntity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushToTag(String appid, String tagName, MobilePushEntity pushEntity) {
		// TODO Auto-generated method stub

	}

}
