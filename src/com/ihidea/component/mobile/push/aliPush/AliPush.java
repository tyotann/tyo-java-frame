package com.ihidea.component.mobile.push.aliPush;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.push.model.v20160801.PushRequest;
import com.aliyuncs.push.model.v20160801.PushResponse;
import com.ihidea.component.mobile.push.IPush;
import com.ihidea.component.mobile.push.MobilePushEntity;
import com.ihidea.core.util.JSONUtilsEx;
import com.ihidea.core.util.PropertyUtils;
import com.ihidea.core.util.StringUtilsEx;

/**
 * 阿里云移动推送
 * 
 * @author wenhao
 * @version [版本号, 2017年6月26日]
 */
public class AliPush implements IPush {
    
    private static final Logger logger = LoggerFactory.getLogger(AliPush.class);
    
    private static final Long APPKEY = Long.valueOf(PropertyUtils.getProperty("push.aliPush.appkey"));
    
    private static final String ACCESS_KEY_ID = PropertyUtils.getProperty("push.aliPush.accessKeyId");
    
    private static final String ACCESS_KEY_SECRET = PropertyUtils.getProperty("push.aliPush.accessKeySecret");
    
    private static final String REGION = PropertyUtils.getProperty("push.aliPush.region");
    
    private static final String PUSH_TYPE = PropertyUtils.getProperty("push.aliPush.pushType");
    
    private static final String DEVICE_TYPE = PropertyUtils.getProperty("push.aliPush.deviceType");
    
    private static DefaultAcsClient client;
    
    /**
     * 构造函数
     */
    public AliPush() {
        IClientProfile profile = DefaultProfile.getProfile(REGION, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        client = new DefaultAcsClient(profile);
    }
    
    @Override
    public void pushToUser(String appid, String userId, MobilePushEntity pushEntity) {
        send("ACCOUNT", userId, pushEntity);
    }
    
    @Override
    public void pushToUserList(String appid, List<String> userIds, MobilePushEntity pushEntity) {
        send("DEVICE", listToString(userIds), pushEntity);
    }
    
    @Override
    public void pushToDevice(String appid, List<String> deviceList, MobilePushEntity pushEntity) {
        send("DEVICE", listToString(deviceList), pushEntity);
    }
    
    @Override
    public void pushToApp(String appid, MobilePushEntity pushEntity) {
        send("ACCOUNT", "ALL", pushEntity);
    }
    
    @Override
    public void pushToTag(String appid, String tags, MobilePushEntity pushEntity) {
        send("TAG", tags, pushEntity);
    }
    
    private void send(String target, String targetValue, MobilePushEntity pushEntity) {
        PushRequest pushRequest = new PushRequest();
        // 安全性比较高的内容建议使用HTTPS
        pushRequest.setProtocol(ProtocolType.HTTPS);
        // 内容较大的请求，使用POST请求
        pushRequest.setMethod(MethodType.POST);
        // 推送目标
        pushRequest.setAppKey(APPKEY);
        // 推送目标: device:推送给设备; account:推送给指定帐号,tag:推送给自定义标签; all: 推送给全部
        pushRequest.setTarget(target);
        // 根据Target来设定，如Target=device, 则对应的值为 设备id1,设备id2.，多个值使用逗号分隔.(帐号与设备有一次最多100个的限制)
        pushRequest.setTargetValue(targetValue);
        // 消息类型 MESSAGE NOTICE
        pushRequest.setPushType(PUSH_TYPE);
        // 设备类型 ANDROID iOS ALL.
        pushRequest.setDeviceType(DEVICE_TYPE);
        
        // 推送配置
        pushRequest.setTitle(pushEntity.getMsgTitle()); // 消息的标题
        pushRequest.setBody(pushEntity.getMsgContent()); // 消息的内容
        
        // 拓展属性
        Map<String, String> extrasMap = new HashMap<String, String>();
        
        if (StringUtils.isNotBlank(pushEntity.getType())) {
            extrasMap.put("type", pushEntity.getType());
        }
        
        if (StringUtils.isNotBlank(pushEntity.getValue())) {
            extrasMap.put("value", pushEntity.getValue());
        }
        
        // 若设备类型不为 ANDROID ，则需要推送iOS，
        if (!DEVICE_TYPE.equals("ANDROID")) {
            // iOS应用图标右上角角标
            //pushRequest.setiOSBadge(5);
            // 开启静默通知
            //pushRequest.setiOSSilentNotification(false);
            // iOS通知声音
            //pushRequest.setiOSMusic("default");
            // iOS10通知副标题的内容
            //pushRequest.setiOSSubtitle("iOS10 subtitle");
            // 指定iOS10通知Category
            //pushRequest.setiOSNotificationCategory("iOS10 Notification Category");
            // 是否允许扩展iOS通知内容
            //pushRequest.setiOSMutableContent(true);
            // iOS的通知是通过APNs中心来发送的，需要填写对应的环境信息。"DEV" : 表示开发环境 "PRODUCT" : 表示生产环境
            //pushRequest.setiOSApnsEnv("DEV");
            // 消息推送时设备不在线（既与移动推送的服务端的长连接通道不通），则这条推送会做为通知，通过苹果的APNs通道送达一次。注意：离线消息转通知仅适用于生产环境
            //pushRequest.setiOSRemind(true);
            // iOS消息转通知时使用的iOS通知内容，仅当iOSApnsEnv=PRODUCT && iOSRemind为true时有效
            //pushRequest.setiOSRemindBody("iOSRemindBody");
            // 通知的扩展属性(注意 : 该参数要以jsonmap的格式传入,否则会解析出错)
            //pushRequest.setiOSExtParameters("{\"_ENV_\":\"DEV\",\"k2\":\"v2\"}");
        }
        
        // 推送配置: Android
        if (!DEVICE_TYPE.equals("iOS")) {
            // 通知的提醒方式 "VIBRATE" : 震动 "SOUND" : 声音 "BOTH" : 声音和震动 NONE : 静音
            // pushRequest.setAndroidNotifyType("NONE");
            // 通知栏自定义样式0-100
            // pushRequest.setAndroidNotificationBarType(1);
            // 通知栏自定义样式0-100
            // pushRequest.setAndroidNotificationBarPriority(1);
            // 点击通知后动作 "APPLICATION" : 打开应用 "ACTIVITY" : 打开AndroidActivity "URL":打开URL "NONE" : 无跳转
            pushRequest.setAndroidOpenType("ACTIVITY");
            // Android收到推送后打开对应的url,仅当AndroidOpenType="URL"有效
            // pushRequest.setAndroidOpenUrl("http://www.aliyun.com");
            // 设定通知打开的activity，仅当AndroidOpenType="Activity"有效
            pushRequest.setAndroidActivity("com.alibaba.push2.demo.XiaoMiPushActivity");
            // Android通知音乐
            // pushRequest.setAndroidMusic("default");
            // 设置该参数后启动小米托管弹窗功能,此处指定通知点击后跳转的Activity（托管弹窗的前提条件：1. 集成小米辅助通道；2.StoreOffline参数设为true）
            pushRequest.setAndroidXiaoMiActivity("com.ali.demo.MiActivity");
            pushRequest.setAndroidXiaoMiNotifyTitle(pushEntity.getMsgTitle());
            pushRequest.setAndroidXiaoMiNotifyBody(pushEntity.getMsgContent());
            // 设定通知的扩展属性。(注意 : 该参数要以 json map的格式传入,否则会解析出错)
            pushRequest.setAndroidExtParameters(JSONUtilsEx.serialize(extrasMap));
        }
        
        // 推送控制
        // 30秒之间的时间点, 也可以设置成你指定固定时间
        // Date pushDate = new Date(System.currentTimeMillis());
        // String pushTime = ParameterHelper.getISO8601Time(pushDate);
        // 延后推送。可选，如果不设置表示立即推送
        // pushRequest.setPushTime(pushTime);
        // 12小时后消息失效,不会再发送
        // String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + 12 * 3600 * 1000));
        // pushRequest.setExpireTime(expireTime);
        // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到
        pushRequest.setStoreOffline(true);
        
        try {
            
            String msgId = StringUtilsEx.getUUID();
            
            log("aliPush", msgId, DEVICE_TYPE, target, targetValue, pushEntity.getType(), pushEntity.getType(),
                pushEntity.getMsgTitle(), pushEntity.getMsgContent(), null, null);
            PushResponse pushResponse = client.getAcsResponse(pushRequest);
            log("aliPush", msgId, null, null, null, null, null, null, null, pushResponse.getRequestId(), pushResponse.getMessageId());
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }
    
    private void log(String channel, String msgId, String platform, String target, String targetValue, String type, String value,
        String title, String content, String requestId, String messageId) {
        logger.info(
            "记录推送日志,channel:{},msgId:{},platform:{},target:{},targetValue:{},type:{},value:{},title;{},content:{},requestId:{},messageId:{}",
            new Object[]{channel, msgId, platform, target, targetValue, type, value, title, content, requestId, messageId});
    }
}