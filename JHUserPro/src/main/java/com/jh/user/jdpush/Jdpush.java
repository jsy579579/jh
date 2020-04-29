package com.jh.user.jdpush;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CommonAbstractCriteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.jpush.api.JPushClient;
import cn.jpush.api.common.ClientConfig;
import cn.jpush.api.common.TimeUnit;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.push.model.notification.WinphoneNotification;
import cn.jpush.api.report.ReceivedsResult;
import cn.jpush.api.report.ReceivedsResult.Received;
import cn.jpush.api.report.UsersResult;

/**
 * 消息推送
 */
public class Jdpush {
    protected static final Logger log = LoggerFactory.getLogger(Jdpush.class);

    private static final String APPKEY = "61d62200f0dfa0f015a19db1";
    private static final String MASTERSECRET = "93cba8b91c242190f93dfb2d";
    private static final String jfb_APPKEY = "36e107691619851260da30ca";
    private static final String jfb_MASTERSECRET = "082595b18e439ba91e6b02fd";
    private static final String DAY = "86400";
    public static JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, Integer.valueOf(DAY));
    public static JPushClient jfbjpushClient = new JPushClient(jfb_MASTERSECRET, jfb_APPKEY, Integer.valueOf(DAY));

    /**
     * 推送通知接口
     *
     * @param alias   别名
     * @param tags    tag数组
     * @param title   推送标题
     * @param btype   推送类型
     * @param content 推送内容
     */
    public static void sendPushNotice(String alias, String[] tags, String title, String btype, String content) {
        PushPayload payload = null;
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("btype", btype);
            // 根据别名推送  
            if (alias != null && tags == null) {
                payload = buldPushObject_all_all_alias(alias, title, content, map);
            } else if (alias == null && tags != null) { // 根据tag[]推送  
                payload = buldPushObject_all_all_tag(tags, title, content, map);
            } else if (alias != null && tags != null) { // 别名和tags[] 推送通知  
                payload = buldPushObject_all_all_aliasAndTag(alias, tags, title, content, map);
            } else if (alias == null && tags == null) {
                payload = buldPushObject_all_all(title, content, map);
            }
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
    }

    public static void sendPushNotice(String alias, String title, String btype, String content) {
        PushPayload payload = null;
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("btype", btype);
            // 根据别名推送  
            payload = buldPushObject_all_all_alias(alias, title, content, map);
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
    }

    public static void sendPushNotice(String APPKEY, String MASTERSECRET, String alias, String title, Map map, String content) {
        PushPayload payload = null;
        JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, Integer.valueOf(DAY));
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            // 根据别名推送  
            payload = buldPushObject_all_all_alias(alias, title, content, map);
        } else {
            log.info("No notification - " + content);
        }
        try {
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. 推送异常");
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ");
        }
    }

    /**
     * 无忧5A
     */
    public static void wy_sendPushNoticeAll(String APPKEY, String MASTERSECRET, String alert, Map map, String content) {
        PushPayload payload = null;
        JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, Integer.valueOf(DAY));
//        JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, null, ClientConfig.getInstance());
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            // 根据别名推送  
            payload = buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(alert, content, map);

        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. 推送异常");
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ");
        }
    }

    public static void sendPushNoticeAll(String APPKEY, String MASTERSECRET, String title, Map map, String content) {
        PushPayload payload = null;
        JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, Integer.valueOf(DAY));
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            // 根据别名推送  
            payload = buldPushObject_all_all(title, content, map);
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
//            log.info("HTTP Status: " + e.getStatus());  
//            log.info("Error Code: " + e.getErrorCode());  
//            log.info("Error Message: " + e.getErrorMessage());  
//            log.info("Msg ID: " + e.getMsgId());  
        }
    }

    public static void sendPushNoticeAndroid(String APPKEY, String MASTERSECRET, String title, Map map, String content) {
        PushPayload payload = null;
        JPushClient jpushClient = new JPushClient(MASTERSECRET, APPKEY, Integer.valueOf(DAY));
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            // 根据别名推送  
            payload = buldPushObject_android_all(title, content, map);
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
    }

    public static void jfb_sendPushNotice(String alias, String title, Map map, String content) {
        PushPayload payload = null;
        // 生成推送的内容，这里我们先测试全部推送  
        // 通知提示信息  
        if (content != null) {
            // 根据别名推送  
            payload = buldPushObject_all_all_alias(alias, title, content, map);
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jfbjpushClient.sendPush(payload);
            System.out.println(result.msg_id + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
    }

    /**
     * 推送自定义消息接口.根据别名修改标签（tag）
     *
     * @param alias   别名
     * @param content 推送内容
     */
    public static void sendPushMessage(String alias, String content) {
        PushPayload payload = null;
        // For push, all you need do is to build PushPayload object.  
        // PushPayload payload = buildPushObject_all_all_alert();  
        // 判断用户别名和tag不为空的情况下才推送修改标签（tag）  
        if (content != null && alias != null) {
            payload = PushPayload.newBuilder()
                    .setAudience(Audience.alias(alias))
                    .setPlatform(Platform.all())
                    .setMessage(Message.content(content)).build();
        } else {
            log.info("No notification - " + content);
        }
        try {
            System.out.println(payload.toString());
            PushResult result = jpushClient.sendPush(payload);
            System.out.println(result + "................................");
            log.info("Got result - " + result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
    }

    /**
     * 查询记录推送成功条数
     *
     * @param mid
     */
    public static void countPush(String mid) {
        PushPayload payload = null;
        try {
            ReceivedsResult result = jpushClient.getReportReceiveds(mid);
            Received received = result.received_list.get(0);
            System.out.println("android_received:" + received.android_received
                    + "\nios:" + received.ios_apns_sent);
            log.debug("Got result - " + result);
        } catch (APIConnectionException e) {
            // Connection error, should retry later  
            log.error("Connection error, should retry later", e);
        } catch (APIRequestException e) {
            // Should review the error, and fix the request  
            log.error("Should review the error, and fix the request", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
        }
    }

    /**
     * 统计用户数据。需要vip用户才能访问
     */
    public static void getReportUser() {
        PushPayload payload = null;
        try {
            UsersResult result = jpushClient.getReportUsers(TimeUnit.DAY, "2015-04-28", 8);
            // Received received =result  
            // System.out.println("android_received:"+received.android_received+"\nios:"+received.ios_apns_sent);  
            log.debug("Got result - " + result);
        } catch (APIConnectionException e) {
            // Connection error, should retry later  
            log.error("Connection error, should retry later", e);
        } catch (APIRequestException e) {
            // Should review the error, and fix the request  
            log.error("Should review the error, and fix the request", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
        }
    }

    /**
     * 根据别名通知推送
     *
     * @param alias 别名
     * @param alert 推送内容
     * @return
     */
    public static PushPayload buldPushObject_all_all_alias(String alias, String title, String content, Map<String, String> map) {
        return PushPayload
                .newBuilder()
                .setPlatform(Platform.all())
                .setOptions(Options.newBuilder().setApnsProduction(true).build())
                .setAudience(Audience.alias(alias))
                .setNotification(
                        Notification
                                .newBuilder()
                                .addPlatformNotification(
                                        IosNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .addPlatformNotification(
                                        AndroidNotification.newBuilder()
                                                .setAlert(content)
                                                .setTitle(title).addExtras(map)
                                                .build())
                                .addPlatformNotification(
                                        WinphoneNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .build()).build();
    }

    /**
     * 根据tag通知推送
     *
     * @param alias 别名
     * @param alert 推送内容
     * @return
     */
    public static PushPayload buldPushObject_all_all_tag(String[] tags, String title, String content, Map<String, String> map) {
        return PushPayload
                .newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.tag(tags))
                .setNotification(
                        Notification
                                .newBuilder()
                                .addPlatformNotification(
                                        IosNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .addPlatformNotification(
                                        AndroidNotification.newBuilder()
                                                .setAlert(content)
                                                .setTitle(title).addExtras(map)
                                                .build())
                                .addPlatformNotification(
                                        WinphoneNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .build()).build();
    }

    /**
     * 根据tag通知推送
     *
     * @param alias 别名
     * @param alert 推送内容
     * @return
     */
    public static PushPayload buldPushObject_all_all_aliasAndTag(String alias, String[] tags, String title, String content, Map<String, String> map) {
        return PushPayload
                .newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setAudience(Audience.tag(tags))
                .setNotification(
                        Notification
                                .newBuilder()
                                .addPlatformNotification(
                                        IosNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .addPlatformNotification(
                                        AndroidNotification.newBuilder()
                                                .setAlert(content)
                                                .setTitle(title).addExtras(map)
                                                .build())
                                .addPlatformNotification(
                                        WinphoneNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .build()).build();
    }

    /**
     * 根据通知推送
     *
     * @param alias 别名
     * @param alert 推送内容
     * @return
     */
    public static PushPayload buldPushObject_all_all(String title, String content, Map<String, String> map) {
        return PushPayload
                .newBuilder()
                .setPlatform(Platform.all())
                .setOptions(Options.newBuilder().setApnsProduction(true).build())
                .setAudience(Audience.all())
                .setNotification(
                        Notification
                                .newBuilder()
                                .addPlatformNotification(
                                        IosNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .addPlatformNotification(
                                        AndroidNotification.newBuilder()
                                                .setAlert(content)
                                                .setTitle(title).addExtras(map)
                                                .build())
                                .addPlatformNotification(
                                        WinphoneNotification.newBuilder()
                                                .setAlert(content)
                                                .addExtras(map).build())
                                .build()).build();
    }

    /**
     * @param alert 通知信息
     *              消息内容是 MSG_CONTENT
     */
    public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(String alert, String content, Map<String, String> map) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.all())
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(content)
                                .setBadge(5)
                                .setSound("happy")
                                .setContentAvailable(true)
                                .addExtra("from", "JPush")
                                .build())
                        .build())
                .setMessage(Message.content(content))
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .build())
                .build();
    }

    /**
     * 根据通知推送
     *
     * @param alias 别名
     * @param alert 推送内容
     * @return
     */
    public static PushPayload buldPushObject_android_all(String title, String content, Map<String, String> map) {
        return PushPayload
                .newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.all())
                .setNotification(
                        Notification
                                .newBuilder()
                                .addPlatformNotification(
                                        AndroidNotification.newBuilder()
                                                .setAlert(content)
                                                .setTitle(title).addExtras(map)
                                                .build())
                                .build()).build();
    }

}  