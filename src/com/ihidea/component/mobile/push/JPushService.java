package com.ihidea.component.mobile.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.DeviceType;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

import com.ihidea.core.CoreConstants;
import com.ihidea.core.support.exception.ServiceException;
import com.ihidea.core.util.StringUtilsEx;

/**
 * 引用到Jpush包，所以延迟加载
 * 
 * @author TYOTANN
 *
 */
@Lazy(true)
@Component
public class JPushService {

	private final static Logger logger = LoggerFactory.getLogger(JPushService.class);

	@Autowired
	private MobilePushService mobilePushService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void sendNotification2App(String appid, MobilePushEntity pushEntity, DeviceType platform) {

		PushPayload.Builder payloadBuilder = PushPayload.newBuilder();
		payloadBuilder.setAudience(Audience.all());

		log(appid, String.valueOf(platform), "all", null, null, null, pushEntity.getType(), pushEntity.getValue(),
				pushEntity.getMsgTitle(), pushEntity.getMsgContent());

		send(appid, pushEntity, payloadBuilder, platform);
	}

	public void sendNotification2Tag(String appid, String tag, MobilePushEntity pushEntity, DeviceType platform) {

		PushPayload.Builder payloadBuilder = PushPayload.newBuilder();
		payloadBuilder.setAudience(Audience.tag(tag));

		log(appid, String.valueOf(platform), null, tag, null, null, pushEntity.getType(), pushEntity.getValue(), pushEntity.getMsgTitle(),
				pushEntity.getMsgContent());

		send(appid, pushEntity, payloadBuilder, platform);
	}

	public void sendNotification2Alias(String appid, String alias, MobilePushEntity pushEntity, DeviceType platform) {

		PushPayload.Builder payloadBuilder = PushPayload.newBuilder();
		payloadBuilder.setAudience(Audience.alias(alias.replace("-", "")));

		log(appid, String.valueOf(platform), null, null, alias.replace("-", ""), null, pushEntity.getType(), pushEntity.getValue(),
				pushEntity.getMsgTitle(), pushEntity.getMsgContent());

		send(appid, pushEntity, payloadBuilder, platform);
	}

	public void sendNotification2Reg(String appid, String regId, MobilePushEntity pushEntity, DeviceType platform) {

		PushPayload.Builder payloadBuilder = PushPayload.newBuilder();
		payloadBuilder.setAudience(Audience.registrationId(regId));

		log(appid, String.valueOf(platform), null, null, null, regId, pushEntity.getType(), pushEntity.getValue(),
				pushEntity.getMsgTitle(), pushEntity.getMsgContent());

		send(appid, pushEntity, payloadBuilder, platform);
	}

	private void send(String appid, MobilePushEntity pushEntity, PushPayload.Builder payloadBuilder, DeviceType deviceType) {

		try {
			List<String[]> JpushInfoList = mobilePushService.getJpushKeyInfo(appid, pushEntity.getJpushApiMasterSecret(),
					pushEntity.getJpushAppKey());

			// 如果配置mobile.notify.ios.production=false,则是开发模式
			boolean iosMode = true;
			if (StringUtils.isNotBlank(CoreConstants.getProperty("mobile.notify.ios.production"))
					&& "false".equals(CoreConstants.getProperty("mobile.notify.ios.production").trim())) {
				iosMode = false;
			}

			// JPushClient jPushClient = new
			// JPushClient("6f9de6a1f8cdd8271edabde7",
			// "13e390c79495cbd12e280b8c", true, 86400);

			// 设置平台
			payloadBuilder.setPlatform(deviceType.equals(DeviceType.IOS) ? Platform.ios() : Platform.android());

			Map<String, Object> extrasMap = new HashMap<String, Object>();

			// 新的模式，只需要传入type与value
			if (StringUtils.isNotBlank(pushEntity.getType())) {
				extrasMap.put("type", pushEntity.getType());
			}

			if (StringUtils.isNotBlank(pushEntity.getValue())) {
				extrasMap.put("value", pushEntity.getValue());
			}

			// for (String key : pushEntity.getAttr().keySet()) {
			// extrasMap.put(key, pushEntity.getAttr().get(key));
			//
			// // 兼容老版本推送
			// if ("url".equals(key)) {
			// extrasMap.put("uri", pushEntity.getAttr().get(key));
			// }
			// }

			// 如果是IOS，设置平台特性
			if (deviceType.equals(DeviceType.IOS)) {

				// IOS内容的长度限制
				String notificationContent = pushEntity.getMsgContent();
				if (StringUtils.isNotBlank(notificationContent) && notificationContent.getBytes().length > 70) {
					notificationContent = StringUtilsEx.substringb(notificationContent, 70) + "...";
				}

				IosNotification.Builder iosNotificationBuilder = IosNotification.newBuilder().setAlert(notificationContent).setBadge(1)
						.setSound("default");

				for (String key : extrasMap.keySet()) {
					iosNotificationBuilder.addExtra(key, String.valueOf(extrasMap.get(key)));
				}

				payloadBuilder.setNotification(Notification.newBuilder().addPlatformNotification(iosNotificationBuilder.build()).build());
			} else {

				AndroidNotification.Builder androidNotificationBuilder = AndroidNotification.newBuilder()
						.setAlert(pushEntity.getMsgContent()).setTitle(pushEntity.getMsgTitle());

				for (String key : extrasMap.keySet()) {
					androidNotificationBuilder.addExtra(key, String.valueOf(extrasMap.get(key)));
				}

				payloadBuilder.setNotification(Notification.newBuilder().addPlatformNotification(androidNotificationBuilder.build())
						.build());
			}

			PushPayload pushPayload = payloadBuilder.build();

			for (String[] jpushInfo : JpushInfoList) {
				try {
					JPushClient jPushClient = new JPushClient(jpushInfo[0], jpushInfo[1], iosMode,
							(pushEntity.getJpushTimeToLive() == null ? 86400 : pushEntity.getJpushTimeToLive()));
					jPushClient.sendPush(pushPayload);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.error("JPUSH推送消息时发生异常:[" + e.getMessage() + "]", e);
			throw new ServiceException("JPUSH推送消息时发生异常:[" + e.getMessage() + "],请与管理员联系!");
		}
	}

	private void log(String appid, String platform, String typePlatform, String typeTag, String typeAlias, String typeReg, String type,
			String value, String title, String content) {

		// try {
		logger.info("记录推送日志,appid:{},platform:{},typePlatform:{},typeTag:{},typeAlias:{},typeReg:{},type:{},value:{},title;{},content:{}",
				new Object[] { appid, platform, typePlatform, typeTag, typeAlias, typeReg, type, value, title, content });
		// jdbcTemplate
		// .update("insert into cpt_notify_log (appid, id, platform, type_platform, type_tag, type_alias, type_registration, msg_type, msg_value, msg_title, msg_content, create_time)"
		// + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)", new Object[]
		// { appid, StringUtilsEx.getUUID(),
		// platform, typePlatform, typeTag, typeAlias, typeReg, type, value,
		// title, content });
		// } catch (Exception e) {
		// logger.error("记录推送日志时异常:" + e.getMessage(), e);
		// }

	}

	public static void main(String[] args) throws Exception {

		MobilePushEntity pushEntity = new MobilePushEntity();
		pushEntity.setMsgTitle("title");
		pushEntity.setMsgContent("群推送");
		pushEntity.setType("3001");
		pushEntity.setValue("6a049b1a-18a8-494f-b41e-30051e66959c");

		new JPushService().sendNotification2App("", pushEntity, DeviceType.Android);
		// new JPushService().sendNotification2Alias("",
		// "973fd6ac-252c-452a-b0e6-0cf57da90864",pushEntity,
		// DeviceEnum.Android);
	}

}
