package com.jh.paymentchannel.service;

import com.jh.paymentchannel.business.impl.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@Component
public class AliCloudBank4Auth {

    private static final Logger log = LoggerFactory.getLogger(AliCloudBank4Auth.class);

    @RequestMapping(method= RequestMethod.POST,value="/v1.0/paymentchannel/bank4RealCheck/alicloud")
    @ResponseBody
    public String bank4RealCheck(
            @RequestParam(value="bankCard") String bankCard,
            @RequestParam(value="idCard") String idCard,
            @RequestParam(value="phone") String phone,
            @RequestParam(value="userName") String userName
                                 ) {
        bankCard=bankCard.trim();
        idCard=idCard.trim();
        phone=phone.trim();
        String host = "https://bkcard4.market.alicloudapi.com";
        String path = "/bankcard4/check";
        String method = "GET";
        String appcode = "e03ad1e0da2d402eac76c2713350a91a";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("bankcard", bankCard);
        querys.put("idcard", idCard);
        querys.put("mobile", phone);
        querys.put("name", userName);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            log.info("阿里云数据四要素查询结果"+response.toString());
            //获取response的body
            String result=EntityUtils.toString(response.getEntity());
            log.info(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
