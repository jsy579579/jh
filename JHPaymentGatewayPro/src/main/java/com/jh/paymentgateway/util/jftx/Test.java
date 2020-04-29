package com.jh.paymentgateway.util.jftx;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Test {

    /*public static void main(String[] args) {
    	
    	String requestURL = "https://payfor.jfpays.com/rest/v1/api/";
    	String url = requestURL + "102001";
        //加密
        String partnerNo = "ta7E6dop70WE";

        //需与报文头的traceId一致
        String traceId = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());;
        
        Map<String,Object > map = new HashMap<String,Object>();
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("traceId", traceId);
        head.put("charset", "UTF-8");
        head.put("partnerNo", partnerNo);
        head.put("txnCode", "102001");
        head.put("reqDate", DateFormatUtils.format(new Date(),"yyyyMMdd"));
        head.put("partnerType", "OUTER");
        head.put("reqTime", DateFormatUtils.format(new Date(),"yyyyMMddHHmmss"));
        head.put("version", "1.0.0");
        
        // 业务参数
		map.put("callBackUrl", "http://106.15.56.208/v1.0/paymentgateway/quick/jftx/callback");//异步回调地址
		map.put("accountCategory", "PERSON");//收款人账户种类
		map.put("accountName", "钟守韩");//收款人姓名
		map.put("purpose", "提现");//资金用途
		map.put("mobile", "13166382981");//手机号
		map.put("bankName", "工商银行");//收款人账户总行名称
		map.put("certificateNo", "370983199302183717");//证件号
		map.put("head", head);//证件号
		map.put("accountNo", "6212261001038982085");//收款人账户号
		map.put("bankNo", "102100099996");//收款人账户总行联行号
		map.put("currency", "156");//币种
		map.put("txnAmt", "1056");//订单金额
		map.put("certificateType", "ID");//证件类型
		String jsonStr = JSON.toJSONString(map);

		System.out.println(jsonStr);
		
//        String jsonStr = "{\"callBackUrl\":\"http://localhost/xxxxxx/xxxxxxxx\",\"accountCategory\":\"PERSON\",\"accountName\":\"xxxx\",\"purpose\":\"\",\"mobile\":\"15022222222\",\"bankName\":\"中国农业银行\",\"certificateNo\":\"111111111111111111\",\"head\":{\"traceId\":\"000000000000000001\",\"charset\":\"UTF-8\",\"partnerNo\":\"939101260563621\",\"txnCode\":\"102001\",\"reqDate\":\"20171222\",\"partnerType\":\"OUTER\",\"reqTime\":\"20171222172207\",\"version\":\"1.0.0\"},\"accountNo\":\"111111111111111111\",\"bankNo\":\"103100000026\",\"currency\":\"156\",\"txnAmt\":\"1\",\"certificateType\":\"ID\"}";

       //上游代付公钥
        PublicKey publicKeyplatform = RSAKey
                .getRSAPublicKeyByAbsoluteFileSuffix("D:\\certs\\jfdf_rsa_pub_key.pem", "pem","RSA");
        //平台秘钥
        PrivateKey privatekeypartner = RSAKey
                .getRSAPrivateKeyByAbsoluteFileSuffix("D:\\certs\\xinli_pkcs8_rsa_private_key_2048.pem", "pem","RSA");
        //加密串
        String randomAESKey = CommunicationConsumeSecurityWarper.getRandomAESKey();

        String cipherB64AESKey = CommunicationConsumeSecurityWarper.getCipherB64AESKeyByplatformPublicKey(randomAESKey,
                publicKeyplatform);

        String cipherB64PlainText = CommunicationConsumeSecurityWarper.getCipherB64PlainTextByPartnerAESKey(jsonStr, randomAESKey);

        String signB64PlainText = CommunicationConsumeSecurityWarper.getSignB64PlainTextByPartnerPrivateKey(jsonStr, privatekeypartner);

        Map<String, String> nvps = new HashMap<String, String>();
        nvps.put("encryptData", cipherB64PlainText);
        nvps.put("encryptKey", cipherB64AESKey);
        nvps.put("signData", signB64PlainText);
        nvps.put("traceId", traceId);
        nvps.put("partnerNo", partnerNo);
        
        byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, nvps);
		String resStr = new String(resByte, StandardCharsets.UTF_8);
		System.out.println("============ 返回报文原文:" + resStr);
		JSONObject resJson = JSON.parseObject(resStr);
		
		
    	 //解密
        PrivateKey privateKeyPlatform = RSAKey
                .getRSAPrivateKeyByAbsoluteFileSuffix("D:\\certs\\xinli_pkcs8_rsa_private_key_2048.pem", "pem","RSA");   //秘钥
        String encryptKey = "J+tCsFhXmhBs2OAW0wIQU3hgRp50MXCJTYrqMD5NjXRCtKuZsFneYRWwojJplCyjeW5oC/T14tbitGbSOv6PXB0A0kiwzBzvfXk81taS/gYrLKF6aZ5zgB4VmwMOoXvCsaEIVESbq6YsZaOAnNo1rDwhry5V3Iw4IvHczEi4dU62Is9DDlfYY9Or3UM+ZYIxMVMonmw+hC5FIJqf4ON3+JiBiUTG8Puzam/96efrasW9hfcDJ8UphQK9eD0TutGXOS/HjRKjBn9jAkFlSorYXTgzdxAlNvtb+tNfcRLg9cZtkGf0/ZtB+H3kOir7VO09L8PHm8BgnuAt90ElNPQQMA==";                 //加密只有的AES秘钥
        String encryptData = "IBR9NtYkTzdZ382BtpBaSEyjuzKYgKZYSPvS7bAgnuVUkdQ4HQGPtsvmCgFJ218GrKtQJRlr2GFiFGWUN0Pw7Vh12UUXwQjfkCBE8wOl7VZjnF1qyU8JPCLx5q8AwuTqEPxtGdSCy50wwu+379S9OMqHO74EiP0OGXtW0es2eDHAJ/Jpr9JFc3XfEMl8xXFVXFBHv7wbsc3dz7efnYluWLM9bhb0xYJk9Uk38THDrEz+UuIU8jRPBcDqLZQO+USf+68jjqZ7VAw2w84oWlf8cOc99wYeIyLGpDn3ou1RNovEWV0DpYAoZDnyWUjwIu2UZ2NrdhWkMa77oOOp2C1VoMz1wOFRAmQ/IO850M1DFm7C1Z77OUsojG5O+kTEMQH1UatgNYBjX7NGpTkVqHfvEZtwpYjdObpQn0wPp2sdc6gpa7GksAYY3JThIDZIANIoKIX1StSzNXU6h16bzDi4Nw==";                //加密数据
        String signData = "AAh61/vgCXVS/nv3DDL6NzlnfLP+AmC4l8A7boKHCafSstj2Am134whdqupmp3K20eP6mz6cbKnfhufZ/nnrd9WB4J+IYqbsEjbVrKBe7SifXjSIop6KErnDtd7lA3ulzMAia9f4Yg04Mv81q8n8o2GsgKXumF7uS2fIdxoyJ5EE79KtQYfRSIK3cPqxoDDvki2fP9H863ddCn7ZrtoMbfrF/3m/QAUGi66VjKbk7JF3gW7soFrOUjsfRGbwrOgEDwuO92kSYT+JFFn48Jq3i/IHiz7pabj7dz/FOBkUNk7E9e42HNEnS+Q/w3djggEXI+fADKd+ZAZnyvV4bMtTMg==";                   //签名数据
        PublicKey publicKeyPartner = RSAKey
                .getRSAPublicKeyByAbsoluteFileSuffix("D:\\certs\\jfdf_rsa_pub_key.pem", "pem","RSA");      //上游公钥
        //解密128bit 16位AES密钥
        String partnerAESKey = CommunicationProvideSecurityWarper.getPartnerAESKeyByPlatformPrivateKey(encryptKey,
                privateKeyPlatform);
        //使用partnerAESKey解密数据
        String plainText = CommunicationProvideSecurityWarper.getPlainTextByAESKey(encryptData, partnerAESKey);
        JSONObject plainTextJson = JSON.parseObject(plainText);
	    JSONObject plainheadJson=plainTextJson.getJSONObject("head");
        System.out.println(plainheadJson.get("respCode")+":"+plainheadJson.get("traceId"));
        //验签
        boolean booleanText=CommunicationProvideSecurityWarper.checkSignB64PlainTextByPartnerPublicKey(plainText, signData,
                publicKeyPartner);
    }*/
}
