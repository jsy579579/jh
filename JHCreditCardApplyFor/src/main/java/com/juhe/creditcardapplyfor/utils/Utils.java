package com.juhe.creditcardapplyfor.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author huhao
 * @title: Util
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/20 002015:48
 */

@Component
public class Utils {

    @Value("${publicKey}")
    private String pubKey;
    private static String publicKey;

    @Value("${oemID}")
    private String ID;
    private static String oemID;

    @Value("${merchant}")
    private String merc;
    private static String merchant;

    @PostConstruct
    public void init(){
        publicKey = this.pubKey;
        oemID = this.ID;
        merchant = this.merc;
    }


    /**
     * 生成申卡订单编号
     * @return
     */
    public static String getClientId(){
        return "JH" + getTimeStamp() + getRandom() + getRandom();
    }

    /**
     * 生成积分订单编号
     * @return
     */
    public static String getConversionClientId(){
        return "JHJ" + getTimeStamp() + getRandom() + getRandom();
    }

    /**
     * 生成网贷订单编号
     * @return
     */
    public static String getLoanClientId(){
        return "JHL" + getTimeStamp() + getRandom() + getRandom();
    }



    /**
     * 得到当前系统的时间戳
     * @return
     */
    private static String getTimeStamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 得到当前系统的时间(yyyy-MM-dd)
     */
    public static String getDate(){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 得到一个随机数
     * @return
     */
    private static int getRandom(){
        Random random = new Random();
        return random.nextInt(90) + 10;
    }

    public static Map<String,String> getHead(){
        //生成时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());
        //公钥加密
        String sign = RsaUtils.encrypt(publicKey, timestamp);
        //建立请求头部数据
        Map<String,String> baseMap = new HashMap<>();
        baseMap.put("X-Auth-OEM",oemID);
        baseMap.put("X-Open-Sign",sign);
        baseMap.put("X-Open-Merchant",merchant);
        baseMap.put("X-Open-Timestamp",timestamp);
        return baseMap;
    }

    public static String randomNumber(){
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(9)+1;
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
    }


}
