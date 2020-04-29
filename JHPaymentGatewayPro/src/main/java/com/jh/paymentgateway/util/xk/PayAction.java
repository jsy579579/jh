package com.jh.paymentgateway.util.xk;


import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PayAction {


    public static Map<String,Object> pay(Map<String,String> parms , String url){
        final Logger LOG = LoggerFactory.getLogger(PayAction.class);
        //签名并将请求数据做base64编码
        String tranData = SignMessageUtil.signMessage(parms, PayConfig.KEY, PayConfig.SIGNTYPE);
        //组装请求参数
        Map<String,String> parm = new HashMap<String, String>();
        parm.put("merNo", PayConfig.MERNO);
        parm.put("tranData", tranData);
        String res = new HttpClientUtil().doPost(url, parm, "utf-8");
        // System.out.println(res);

        Map<String, Object> resMap = JsonUtil.jsonResToMap(res);
        // System.out.println(resMap);
        //LOG.info("小卡返回参数为："+ com.alibaba.fastjson.JSONObject.toJSONString(resMap));

        //unicode to Cn
        String responseMsg = UnicodeToString.unicodeToString(resMap.get("responseMsg").toString());
        // System.out.println("responseMsg---------------"+responseMsg);
        Object responseCode = resMap.get("responseCode");

//        Map<String,String> respMap = (Map<String, String>) resMap.get("respMap");
//        String sign = (String) resMap.get(PayConfig.SIGNATURE);
//        //验签
//        if(SignMessageUtil.verifyMessage(respMap, PayConfig.KEY, PayConfig.SIGNTYPE,sign)) {
//            System.out.println("验签成功");
//        }else {
//            Map<String, String> map = new HashMap<String, String>();
//            System.out.println("验签失败");
//        }
        return resMap;

    }


}
