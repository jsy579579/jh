package com.jh.paymentgateway.util.xs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

/**
 * @Author:Chand
 * @Date:2014年8月21日
 * @Time:下午12:42:13
 * @Description:请求基础类
 */
public class SdkRequest {
	
	private static final Logger LOG = LoggerFactory.getLogger(SdkRequest.class);

    public static String requestAction (String ACTION, Map<String, String> data, RSAPrivateKey privateKey) throws Exception {
        /**
         * 业务数据加密
         */
        LOG.info("－－－－－－参数明文-data－－－－－－－");
        LOG.info(JSON.toJSONString(data));
        String encryptkey = "1234567890123456";
        LOG.info("－－－－－－随机密码明文-encryptkey－－－－－－－");
        LOG.info(encryptkey);
        String dataStr = AESTool.encrypt(JSON.toJSONString(data), encryptkey);
        /**
         * 数据签名
         */
        StringBuilder signBuffer = new StringBuilder();
        signBuffer.append(Config.ORGNO);
        signBuffer.append(Config.MERNO);
        signBuffer.append(ACTION);
        signBuffer.append(dataStr);
        String sign = SignUtils.sign(signBuffer.toString(), Config.KEY);
        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("orgNo", Config.ORGNO);
        parameters.put("merNo", Config.MERNO);
        parameters.put("action", ACTION);
        parameters.put("data", dataStr);
        String rsaEncryptkey = Base64.encode(RSATool.encrypt(encryptkey.getBytes("UTF-8"), privateKey));
        parameters.put("encryptkey", rsaEncryptkey);
        parameters.put("sign", sign);

        String requestContent = SdkRequest.createRequest(parameters);
        /**
         * 发送请求
         */
        LOG.info("－－－－－－请求内容－－－－－－－");
        LOG.info(requestContent);
        String resultBuffer = SdkRequest.doPost(Config.BASE_URL, requestContent);
        /**
         * 返回数据解析
         */
        LOG.info("－－－－－－返回数据－－－－－－－");
        LOG.info(resultBuffer.toString());
        JSONObject jsonObject    = JSON.parseObject(resultBuffer.toString());
        String     rtnData       = jsonObject.getString("data");
        String     rtnEncryptkey = jsonObject.getString("encryptkey");
        if (StringUtils.isEmpty(rtnData) || StringUtils.isEmpty(rtnEncryptkey)) {
            return null;
        }
//        System.out.println("－－－－－－返回数据明文－－－－－－－");
//        System.out.println("rtnData:" + rtnData);
//        System.out.println("encryptkey:" + rtnEncryptkey);
        byte[] aesKey = RSATool.decrypt(Base64.decode(rtnEncryptkey), privateKey);
//        System.out.println("encryptkey-aes:" + new String(aesKey));
        LOG.info(AESTool.decrypt(rtnData, new String(aesKey)));
        return AESTool.decrypt(rtnData, new String(aesKey));
    }

    public static String createRequest (Map<String, String> parm) {
        StringBuffer rtn = new StringBuffer();
        for (String s : parm.keySet()) {
            try {
                rtn.append(s).append("=").append(URLEncoder.encode(parm.get(s), "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return rtn.toString();
    }

    public static String doPost (String url, String content) {
        /**
         * 发送请求
         */
        OutputStream       outputStream       = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream        inputStream        = null;
        InputStreamReader  inputStreamReader  = null;
        BufferedReader     reader             = null;
        StringBuffer       resultBuffer       = new StringBuffer();
        String             tempLine           = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection     connection        = realUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(content.length()));
            httpURLConnection.setReadTimeout(15*1000);
            httpURLConnection.setConnectTimeout(5*1000);
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(content);
            outputStreamWriter.flush();

            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            return resultBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
