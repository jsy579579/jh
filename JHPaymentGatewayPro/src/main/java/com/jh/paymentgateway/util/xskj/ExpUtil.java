/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * www.hnapay.com
 */

package com.jh.paymentgateway.util.xskj;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * com.hnapay.expconsumedemo.util
 * Created by weiyajun on 2017-03-02  9:31
 */
public class ExpUtil {

    private static int readTimeOut = 30000;
    private static int connectTimeOut = 30000;

    private static HttpTransport httpTransport = new HttpTransport();

    private static HttpPost method = null;

    //此处为商户的私钥  需要从新生portal安全中心处下载  下载后解压，把 商户号_PrivateKey_10.pem 文件中内容拷贝到此处，拷贝时不拷贝 “-----BEGIN RSA PRIVATE KEY-----” 与 “-----END RSA PRIVATE KEY-----”
    public static String MER_PRI_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALjgo1k2wIzsOaZH8HayAlFnym8q+5XzeHj5/1AHZrgjkNIG3Qkg2muFk/158NqA74RxMbaZJNPzD8rzoYqSEXsbVV4f281+VC/GSFjkoWEj6JY8PeeeuErowM6EBHMKrYhI6s5hvu4JOTvGIKDkWFCXF4Z28LCMXifdSsQOqrvbAgMBAAECgYBiJC3SoDQbGxOHZ80I1XpRiI138hNYCMU9Q0j7TNKEkazBMY+I1v3b8i8bce4fsC512jkPu8BHG1KY5Dgu/Vd1XtRz0rFwRDRRamVqQ+FmS7gJ6LhqSVMK/2v4Bb6vRY/NJgkbi8w/iAy5VM7HyhCJQClfSEzSaryuK/L4I5T+4QJBAP3Wmp/kHGs0BSd2clbamOtJhjWPpVW3CRZuJxyCG9GDUHJI0FsLzrpTurC2mYeLyVKYpYMrWeb/NzlYXat1aOkCQQC6c7EYaQx4GAQhaU2MNEoARJnI//lsgcb2Mgug2wgl0a0aiNgfobAPp/4ExMD+smVxa/Ezusf73lockptwE8QjAkBnGtBbudBBSzP8v4PZ+5i5mXfGMb66fkpWrg986OxbwbbfdOwMz1L9JB1kwgbCo+j2f8Ja8TTnOys3Q8loZ5xhAkAT0XRDefb/VN4rhvvdKTmsaZ5HBR5H8Etro8okoONu0aiqp10Bj7gdEegoLZmt+Nxbf/O1Yecb2HiPjO3ErfUtAkBemLxrYSCkP4ojHWqHjfVFyfdbZWFi4ahjpMRC2AyalQsEoqEVgJtU9HDmuEbyy2n3Pb1+mnb95JcOFv5CEj7D";
//    public static String MER_PRI_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIUnDbPLF4/ktu+yOD6e0THVt339X1AOKM67D8Vz3FE++y6Tul8zk0cHOShcekTCSd7+Ckxb87qWhQOBZv+yOdD6RrB6bRZgUsB/uPylP3no32hkaPdrwm8y9tFWt5FZARrUUhwTVM+LhTOqSk0ncZDAVUhQBUg+QfrFTbBRJ1UVAgMBAAECgYB03LzS8smAOf0wzJs+oob6FReRlUIE8hhwsI77/l78U6mCzJdDHPIC4d0RhnCtTlQlSZNiR0XXmmmtlPIAMr04WIix73DhH/xaZ4HnyT1BF7oemtY/x9qgiDv26PflAeAjFL3q5wE7CW1Hr4ycFVXc5925OfG2/rW6/BHSwqhCFQJBAOM60kbgssLZnN+EXO//333DigPEtvAa6SKD/Qj4m5VumEWPap5vBc32MwBKMG5RwGmiEobs17HzYbUcHmltgn8CQQCWAupfmQtd/+VUdk00ZV2uAsmXyVY7r+OooRW3SnDSjX+qTS2r8X0fhYkTDbOrQrTNqh8Q8GEOx9Yfk3x1pTZrAkAT/7FSG6SgEZcE9RlY5iQ9ZWBYRI/1QekS6Jr0lyzaUgmE1cKEfzD7l5eVhvIMQ5Wztlf7GA1D1C/dSMREcybxAkEAk0H9bSbBn97p8sXG+B5hncYiuxvryN/a7dhjhvBOXIKGxljI8rc2itD9RHT44K+06QeNQ4WU4XuOWKThUmOCTQJAApHzIF9NA59JscuYuBcA95Zgt+QZbFwQCRB5v2j14BCaqt+1eTcu0MTGPOyJpPilwZrnelr5JMu0cPHzXACjJg==";
    //此处为新生的公钥  需要从新生portal安全中心处下载  下载后解压，把 HnapayExpPublicKey.pem.pem 文件中内容拷贝到此处，拷贝时不拷贝 “-----BEGIN PUBLIC KEY-----” 与 “-----END PUBLIC KEY-----”
    public static String HNAPAY_PUBLIC_KEY_PEM = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4Ybi8UscW3Cq4yFoLqZAmTv+3dtzBvc6mOKg/Ec75OJm+BfOpR8wM9eKa/rhBXnudSgXsoDEaTO7wmRtSHL+aLpdHQfVTwPjzkJjKx7rMHwTqgCu5ASDabz4vY6QCSJ9KoYET5lsRU/qB7/XQxNnSDA7Q8I7jEGXpEfLmTrOZrQIDAQAB";

    //加密字段
    public static Map<String, String[]> encryptField = new HashMap<String, String[]>();
    //签名字段
    public static Map<String, String[]> signField = new HashMap<String, String[]>();
    //验签字段
    public static Map<String, String[]> verifyField = new HashMap<String, String[]>();

    public static Map<String, String[]> verifyNotifyField = new HashMap<String, String[]>();
    //提交到新生的字段
    public static Map<String, String[]> submitField = new HashMap<String, String[]>();

    static {
        System.out.println("connectTimeOut=" + connectTimeOut);
        method = new HttpPost();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(readTimeOut)
                .setConnectTimeout(connectTimeOut)
                .setConnectionRequestTimeout(connectTimeOut)
                .build();
        method.setHeader(HTTP.CONTENT_ENCODING, "UTF-8");
        method.setHeader(HTTP.USER_AGENT, "Rich Powered/1.0");
        method.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        method.setConfig(requestConfig);

        //以下为加密的字段
        encryptField.put("EXP01", new String[]{"payType", "cardType", "bankCode", "cardNo", "merUserId", "merUserIp"});
        encryptField.put("EXP02", new String[]{"hnapayOrderId", "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType", "identityCode", "merUserIp"});
        encryptField.put("EXP03", new String[]{"hnapayOrderId", "smsCode", "merUserIp"});
        encryptField.put("EXP04", new String[]{"bizProtocolNo", "payProtocolNo", "merUserIp"});
        encryptField.put("EXP05", new String[]{"tranAmt", "payType", "cardType", "bankCode", "cardNo", "bizProtocolNo", "payProtocolNo", "frontUrl", "notifyUrl", "orderExpireTime", "merUserIp", "riskExpand", "goodsInfo"});
        encryptField.put("EXP06", new String[]{"hnapayOrderId", "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType", "identityCode", "merUserId", "merUserIp"});
        encryptField.put("EXP07", new String[]{"hnapayOrderId", "smsCode", "merUserIp"});
        encryptField.put("EXP08", new String[]{});
        encryptField.put("EXP09", new String[]{"orgMerOrderId", "orgSubmitTime", "orderAmt", "refundOrderAmt", "notifyUrl"});
        encryptField.put("EXP10", new String[]{"cardType", "bankCode","cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo","identityType", "identityCode", "merUserId","merUserIp"});
        encryptField.put("EXP11", new String[]{"hnapayOrderId","smsCode", "merUserIp"});
        encryptField.put("EXP12", new String[]{"tranAmt", "payType", "cardType", "bankCode", "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType", "identityCode", "bizProtocolNo", "payProtocolNo", "frontUrl", "notifyUrl", "orderExpireTime", "merUserId","merUserIp", "riskExpand", "goodsInfo"});
        encryptField.put("EXP13", new String[]{"hnapayOrderId", "smsCode", "merUserIp"});
        encryptField.put("EXP14", new String[]{"tranAmt", "payType", "cardNo", "holderName", "cardAvailableDate", "cvv2", "mobileNo", "identityType", "identityCode", "tradeFeeUnit", "tradeFeeAmt", "extraFeeUnit", "extraFeeAmt", "settleCardNo", "settleMobileNo", "notifyUrl", "orderExpireTime", "merUserId", "merUserIp", "riskExpand", "goodsInfo","subMerchantId"});
        encryptField.put("EXP15", new String[]{"hnapayOrderId", "smsCode", "merUserIp"});
        encryptField.put("EXP16", new String[]{});
        encryptField.put("EXP17", new String[]{"orgMerOrderId", "orgSubmitTime", "orderAmt", "refundOrderAmt", "notifyUrl"});
        encryptField.put("EXP20", new String[]{"orgMerOrderId", "orgSubmitTime", "payeeAccount", "mobile"});

        //以下为签名字段
        String[] geSignField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext"};
        signField.put("EXP01", geSignField);
        signField.put("EXP02", geSignField);
        signField.put("EXP03", geSignField);
        signField.put("EXP04", geSignField);
        signField.put("EXP05", geSignField);
        signField.put("EXP06", geSignField);
        signField.put("EXP07", geSignField);
        signField.put("EXP08", new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime"});
        signField.put("EXP09", geSignField);
        signField.put("EXP10", geSignField);
        signField.put("EXP11", geSignField);
        signField.put("EXP12", geSignField);
        signField.put("EXP13", geSignField);
        signField.put("EXP14", geSignField);
        signField.put("EXP15", geSignField);
        signField.put("EXP17", geSignField);
        signField.put("EXP20", new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime","msgCiphertext","signType"});


        //以下为收到新生响应及后台通知时的验签字段
        verifyField.put("EXP01", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId", "payFactors"});
        verifyField.put("EXP02", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId"});
        verifyField.put("EXP03", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode",
                "bizProtocolNo", "payProtocolNo", "bankCode", "cardType", "shortCardNo"});
        verifyField.put("EXP04", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode"});
        verifyField.put("EXP05", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId", "payFactors"});
        verifyField.put("EXP06", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId"});
        verifyField.put("EXP07", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId",
                "bizProtocolNo", "payProtocolNo", "tranAmt", "checkDate", "bankCode", "cardType", "shortCardNo"});
        verifyField.put("EXP08", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId",
                "tranAmt", "refundAmt", "orderStatus"});
        verifyField.put("EXP09", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId",
                "orgMerOrderId", "tranAmt", "refundAmt", "orderStatus"});
        verifyField.put("EXP10", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId"});
        verifyField.put("EXP11", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode",
                "bizProtocolNo", "payProtocolNo", "bankCode", "cardType", "shortCardNo"});
        verifyField.put("EXP12", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId"});
        verifyField.put("EXP13", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId",
                "bizProtocolNo", "payProtocolNo", "tranAmt", "checkDate", "bankCode", "cardType", "shortCardNo"});

        verifyField.put("EXP14", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId","submitTime"});
        verifyField.put("EXP15", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId","tranAmt", "checkDate", "bankCode", "cardType", "shortCardNo"});
        verifyField.put("EXP16", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId",
                "tranAmt", "refundAmt", "orderStatus"});
        verifyField.put("EXP17", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode", "hnapayOrderId","orgMerOrderId","tranAmt","refundAmt","orderStatus"});
        verifyField.put("EXP20", new String[]{"version", "tranCode", "merOrderId", "merId", "charset", "signType", "resultCode", "errorCode"});

        //异步通知
        verifyNotifyField.put("EXP15",new String[]{"version", "tranCode", "merOrderId", "merId"
                , "charset", "signType", "resultCode"
                , "hnapayOrderId", "tranAmt"
                ,  "bankCode", "cardType", "shortCardNo","settleShortCardNo","settleAmt"});
        //需要提交到新生的字段
        String[] expGeSubmitField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext", "signType", "signValue", "merAttach", "charset"};
        String[] exp08SubmitField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "signType", "signValue", "merAttach", "charset"};
        String[] exp16SubmitField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "signType", "signValue", "merAttach", "charset"};
        String[] exp17SubmitField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "msgCiphertext","signType","remark","merAttach","charset", "signValue"};
        String[] exp20SubmitField = new String[]{"version", "tranCode", "merId", "merOrderId", "submitTime", "signType","signValue","merAttach","charset","msgCiphertext"};

        submitField.put("EXP01", expGeSubmitField);
        submitField.put("EXP02", expGeSubmitField);
        submitField.put("EXP03", expGeSubmitField);
        submitField.put("EXP04", expGeSubmitField);
        submitField.put("EXP05", expGeSubmitField);
        submitField.put("EXP06", expGeSubmitField);
        submitField.put("EXP07", expGeSubmitField);
        submitField.put("EXP08", exp08SubmitField);
        submitField.put("EXP09", expGeSubmitField);
        submitField.put("EXP10", expGeSubmitField);
        submitField.put("EXP11", expGeSubmitField);
        submitField.put("EXP12", expGeSubmitField);
        submitField.put("EXP13", expGeSubmitField);
        submitField.put("EXP14", expGeSubmitField);
        submitField.put("EXP15", expGeSubmitField);
        submitField.put("EXP16", exp16SubmitField);
        submitField.put("EXP17", exp17SubmitField);
        submitField.put("EXP20", exp20SubmitField);

    }

    /**
     * 签名，提交到新生时需要做签名
     *
     * @param tranCode 交易码 例如 EXP01
     * @param params   签名字段
     * @return 签名值
     */
    public static String sign(String tranCode, Map<String, String> params) throws IllegalArgumentException {
        if (null == tranCode || "".equals(tranCode))
            throw new IllegalArgumentException("参数无效！");
        if (null == params)
            throw new IllegalArgumentException("参数无效！");
        String base64 = "";
        try {
            PrivateKey prikey = getPrivateKeyByPem(MER_PRI_KEY);
            String merData = genSingData(tranCode, params);
            byte[] b = RSAAlgorithms.getSignByte(prikey, merData);
            base64 = Base64Util.encode(b);
            base64 = base64.replace("\n", "").replace("\r", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }

    /**
     * 加密 ，对敏感信息的加密 ，签名前需要对敏感信息加密
     *
     * @param tranCode 交易码 例如 EXP01
     * @param params   加密字段
     * @return 密文
     */
    public static String encrpt(String tranCode, Map<String, String> params) throws IllegalArgumentException {
        if (null == tranCode || "".equals(tranCode))
            throw new IllegalArgumentException("参数无效！");
        if (null == params)
            throw new IllegalArgumentException("参数无效！");
        String plainData = genEncryptJson(tranCode, params);
        try {
            //使用新生公钥加密  RSA算法
            String hexPublicKey = HexStringByte.byteToHex(Base64Util.decode(HNAPAY_PUBLIC_KEY_PEM));
            byte[] cipherByte = RSAAlgorithms.encryptByPublicKey(plainData.getBytes("UTF-8"), hexPublicKey);
            String base64 = Base64Util.encode(cipherByte);
            base64 = base64.replace("\n", "").replace("\r", "");
            return base64;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 验证新生响应及后台通知的签名
     *
     * @param tranCode 交易码
     * @param params   参数
     * @return true验签通过   false 验签未通过或失败
     * @throws Exception
     */
    public static boolean verify(String tranCode, Map<String, Object> params) throws Exception {
        if (null == tranCode || "".equals(tranCode))
            throw new IllegalArgumentException("参数无效！");
        if (null == params)
            throw new IllegalArgumentException("参数无效！");
        String signVal = params.get(ExpConstant.SIGNVALUE).toString();
        if (null == signVal || "".equals(signVal)) {
            throw new IllegalArgumentException("签名值不能为空！");
        }
        if(null !=  params.get(ExpConstant.PAYFACTORS)){
            //验签时支付要素要进行特殊处理
            ArrayList<String> list = (ArrayList<String>) params.get(ExpConstant.PAYFACTORS);
            StringBuffer factor=new StringBuffer();
            for(String str:list){
                factor.append(str).append(",");
            }
            if(factor.length() > 0) {
            	params.put(ExpConstant.PAYFACTORS, factor.substring(0,factor.length()-1));
            } else {
            	params.put(ExpConstant.PAYFACTORS, "");
            }
        }

        String verifyData = genVerifyData(tranCode, params);
        return RSAAlgorithms.verify(getPublicKeyByPem(HNAPAY_PUBLIC_KEY_PEM), verifyData, Base64Util.decode(signVal));
    }


    public static boolean verifyNotify(String tranCode, Map<String, Object> params) throws Exception {
        if (null == tranCode || "".equals(tranCode))
            throw new IllegalArgumentException("参数无效！");
        if (null == params)
            throw new IllegalArgumentException("参数无效！");
        String signVal = params.get(ExpConstant.SIGNVALUE).toString();
        if (null == signVal || "".equals(signVal)) {
            throw new IllegalArgumentException("签名值不能为空！");
        }
        if(null !=  params.get(ExpConstant.PAYFACTORS)){
            //验签时支付要素要进行特殊处理
            ArrayList<String> list = (ArrayList<String>) params.get(ExpConstant.PAYFACTORS);
            StringBuffer factor=new StringBuffer();
            for(String str:list){
                factor.append(str).append(",");
            }
            if(factor.length() > 0) {
                params.put(ExpConstant.PAYFACTORS, factor.substring(0,factor.length()-1));
            } else {
                params.put(ExpConstant.PAYFACTORS, "");
            }
        }

        String verifyData = genVerifyNotifyData(tranCode, params);
        return RSAAlgorithms.verify(getPublicKeyByPem(HNAPAY_PUBLIC_KEY_PEM), verifyData, Base64Util.decode(signVal));
    }

    /**
     * 生成加密的JSON串
     *
     * @param tranCode
     * @param params
     * @return
     */
    public static String genEncryptJson(String tranCode, Map<String, String> params) {
        String[] field = encryptField.get(tranCode.toUpperCase());
        if (null != field && field.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (int i = 0; i < field.length; i++) {
                sb.append("\"");
                sb.append(field[i]);
                sb.append("\":\"");
                sb.append(params.get(field[i]));
                sb.append("\"");
                if (i < field.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
            return sb.toString();
        } else {
            return "";
        }

    }

    /**
     * 生成签名明文串
     *
     * @param tranCode 交易码
     * @param params   参数
     * @return 返回签名明文串
     */
    private static String genSingData(String tranCode, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        for (int i = 0; i < signField.get(tranCode.toUpperCase()).length; i++) {
            sb.append(signField.get(tranCode.toUpperCase())[i]);
            sb.append("=[");
            sb.append(params.get(signField.get(tranCode.toUpperCase())[i]));
            sb.append("]");
        }
        return sb.toString();
    }


    /**
     * 生成验签的明文
     *
     * @param tranCode
     * @param params
     * @return
     */
    private static String genVerifyNotifyData(String tranCode, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        for (int i = 0; i < verifyNotifyField.get(tranCode.toUpperCase()).length; i++) {
            sb.append(verifyNotifyField.get(tranCode.toUpperCase())[i]);
            sb.append("=[");
            sb.append(params.get(verifyNotifyField.get(tranCode.toUpperCase())[i]));
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * 生成验签的明文
     *
     * @param tranCode
     * @param params
     * @return
     */
    private static String genVerifyData(String tranCode, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        for (int i = 0; i < verifyField.get(tranCode.toUpperCase()).length; i++) {
            sb.append(verifyField.get(tranCode.toUpperCase())[i]);
            sb.append("=[");
            sb.append(params.get(verifyField.get(tranCode.toUpperCase())[i]));
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * POST提交
     *
     * @param url
     * @param obj
     * @return
     * @throws Exception
     */
    public static String submit(String tranCode, String url, Map<String, String> obj) throws Exception {
        if (null == tranCode || "".equals(tranCode))
            throw new IllegalArgumentException("参数无效！");
        if (null == url || "".equals(url))
            throw new IllegalArgumentException("参数无效！");
        if (null == obj)
            throw new IllegalArgumentException("参数无效！");
        method.setURI(new URI(url + "?v=" + UUID.randomUUID()));
        httpTransport.setMethod(method);
        String response;
        Map<String, String> paraMap = new HashMap<String, String>();

        String[] field = submitField.get(tranCode.toUpperCase());
        for (int i = 0; i < field.length; i++) {
            paraMap.put(field[i], obj.get(field[i]));
        }
        if (url.startsWith("https")) {
            response = httpTransport.submit_https(paraMap);
        } else {
            response = httpTransport.submit(paraMap);
        }
        return response;
    }


    /**
     * pem转私钥
     *
     * @param pem
     * @return 返回私钥
     * @throws Exception
     */
    public static PrivateKey getPrivateKeyByPem(String pem)
            throws Exception {
        byte[] bPriKey = Base64Util.decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bPriKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ExpConstant.ALGORITHM);
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        return key;
    }

    /**
     * pem转公钥
     *
     * @param pem
     * @return 返回公钥
     * @throws Exception
     */
    public static PublicKey getPublicKeyByPem(String pem)
            throws Exception {
        return RSAAlgorithms.getPublicKey(Base64Util.decode(pem));
    }
}
