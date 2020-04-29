package com.jh.paymentchannel.util.ybhk;

import com.alibaba.fastjson.JSON;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.Map;


public class HttpClientHelper {

    public static Map sendMultiPart(Map<String, String> basicParamMap, Map<String, File> fileParamMap, String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        if (basicParamMap != null && basicParamMap.size() > 0) {
            for (Map.Entry<String, String> entry : basicParamMap.entrySet()) {
                multipartEntityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue(), "text/plain", Charset.forName("UTF-8")));
            }
        }

        if (fileParamMap != null && fileParamMap.size() > 0) {
            for (Map.Entry<String, File> entry : fileParamMap.entrySet()) {
                multipartEntityBuilder.addPart(entry.getKey(), new FileBody(entry.getValue(), ContentType.create("image/jpeg"), entry.getValue().getName()));
//                multipartEntityBuilder.addBinaryBody(entry.getKey(), new FileInputStream(entry.getValue()), ContentType.create("multipart/form-data"), entry.getKey() + ".jpg");
            }
        }

        HttpEntity httpEntity = multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE).build();
        httpPost.setEntity(httpEntity);
        System.out.println(httpPost.getRequestLine());
        System.out.println(httpPost.getAllHeaders().toString());

        CloseableHttpResponse response = httpclient.execute(httpPost);
        String returnString = EntityUtils.toString(response.getEntity());
        System.out.println("1：" + response.getStatusLine());
        System.out.println("2:" + response.getEntity());
        System.out.println("3：" + returnString);
        httpclient.close();

//       JSONObject json_str = JSONObject.fromObject(returnString);
//       System.out.println("json_str:"+json_str);

        Map TreeMap = JSON.parseObject(returnString, Map.class);



        return TreeMap;
    }

}
