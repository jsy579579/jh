package com.jh.paymentgateway.controller.hqm.util;

import com.jh.paymentgateway.controller.hqt.util.HashMapConver;
import com.jh.paymentgateway.controller.hqt.util.HttpUtils;
import com.jh.paymentgateway.controller.hqt.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class AbstractChannel {

    private static final Logger log = LoggerFactory.getLogger(AbstractChannel.class.getSimpleName());
    private final String merchno="shbyt2019120609"; // 填写你们自己的商户号

    private final String merchkey="533e83ddb7c1d8ee25cb756c73db510a"; // 填写你们自己的秘钥

    private final String version="0100";//交易版本

    public String transcode;//交易码

    public String postUrl;//请求地址

    public AbstractChannel(String transcode,String postUrl) {

        this.transcode = transcode;
        this.postUrl=postUrl;
    }

    public Map<String,String> allRequestMethod(Map<String,String> map){

        if(map.get("transtype")!=null){
            map.put("transcode","902");  //交易码
        }else {
            map.put("transcode","050");  //交易码
        }
        map.put("version",version);  //版本号

        map.put("ordersn",new Date().getTime()+""); //唯一值，交易唯一
        System.out.println("ordersn"+new Date().getTime()+"");


        map.put("merchno", merchno); //商户号

        Map orderbymap =  HashMapConver.getOrderByMap();

        orderbymap.putAll(map);

        byte[] response =  HashMapConver.getSign(orderbymap,merchkey);

        try {
            if(map.get("transtype")!=null){
                //postUrl="http://pay.huanqiuhuiju.com/authsys/api/channel/pay/execute.do";
                postUrl="http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
            }else{
                postUrl="http://pay.huanqiuhuiju.com/authsys/api/channel/pay/execute.do";
            }

            String result = HttpUtils.post(postUrl, response); //发送post请求

            System.out.println("返回参数："+result);

            Map<String ,String> resultMap = JsonUtil.jsonToMap(result);

            return resultMap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }
    public Map<String,Object> allRequestMethodquery(Map<String,String> map){

        if(map.get("transtype")!=null){
            map.put("transcode","902");  //交易码
        }else {
            map.put("transcode","050");  //交易码
        }
        map.put("version",version);  //版本号

        map.put("ordersn",new Date().getTime()+""); //唯一值，交易唯一
        System.out.println("ordersn"+new Date().getTime()+"");


        map.put("merchno", merchno); //商户号

        Map orderbymap =  HashMapConver.getOrderByMap();

        orderbymap.putAll(map);

        byte[] response =  HashMapConver.getSign(orderbymap,merchkey);

        try {
            if(map.get("transtype")!=null){
                //postUrl="http://pay.huanqiuhuiju.com/authsys/api/channel/pay/execute.do";
                postUrl="http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
            }else{
                postUrl="http://pay.huanqiuhuiju.com/authsys/api/channel/pay/execute.do";
            }

            String result = HttpUtils.post(postUrl, response); //发送post请求

            System.out.println("返回参数："+result);

            Map<String ,Object> resultMap = JsonUtil.jsonToMap(result);

            return resultMap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }
}
