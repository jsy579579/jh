package com.jh.mircomall.utils;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

public class PushUtil {
	/**
	 * 测试别名推送
	 * @Author sy
	 * @param appKey
	 * @param masterSecret
	 */
	@SuppressWarnings("deprecation")
	public static void testSendPush(String appKey ,String masterSecret) {  
		JPushClient jpushClient=new JPushClient(masterSecret, appKey, 86400); 
		PushPayload pushPayload = buildPushObject_android_and_iosByAlias("aa","bb","cc");
		System.out.println(pushPayload.toString());
		try {
			PushResult sendPush = jpushClient.sendPush(pushPayload);
			System.out.println(sendPush);
		} catch (APIConnectionException e) {
			e.printStackTrace();
		} catch (APIRequestException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 别名推送
	 * @Author sy
	 * @param alias 别名
	 * @param title 标题
	 * @param content 内容
	 * @return
	 */
	public static PushPayload buildPushObject_android_and_iosByAlias(String alias,String title,String content) {
		return PushPayload.newBuilder()
		.setPlatform(Platform.android_ios())
		.setAudience(Audience.alias(alias))
		.setNotification(Notification.newBuilder()
		.setAlert(content)
		.addPlatformNotification(AndroidNotification.newBuilder()
		.setTitle(title).build())
		.addPlatformNotification(IosNotification.newBuilder()
		.incrBadge(1)
		.addExtra(title, content).build())
		.build())
		.build();
		}
}
