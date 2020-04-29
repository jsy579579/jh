package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jftx.CommunicationConsumeSecurityWarper;
import com.jh.paymentgateway.util.jftx.CommunicationProvideSecurityWarper;
import com.jh.paymentgateway.util.jftx.HttpClient4Util;
import com.jh.paymentgateway.util.jftx.RSAKey;
import com.jh.paymentgateway.util.ymd.RsaUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class JFTXpageRequset extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	@Value("${jftx.requestURL}")
	private String requestURL;
	
	@Value("${jftx.partnerNo}")
	private String partnerNo;
	
	@Value("${jftx.public_key}")
	private String publicKey="D:\\certs\\jfdf_rsa_pub_key.pem";
	
	@Value("${jftx.private_key}")
	private String privateKey="D:\\certs\\xinli_rsa_private_key_pkcs8.pem";
	private static final Logger LOG = LoggerFactory.getLogger(JFTXpageRequset.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jftx/register")
	public @ResponseBody Object getRegister(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		String requestURL = "https://payfor.jfpays.com/rest/v1/api/";
    	String url = requestURL + "102001";
        //加密
        String partnerNo = "ta7E6dop70WE";

        //需与报文头的traceId一致
        String traceId =  partnerNo+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());;
        
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
		
		
        /*jsonStr = "{\"callBackUrl\":\"http://106.15.56.208/v1.0/paymentgateway/quick/jftx/callback\",\"accountCategory\":\"PERSON\",\"accountName\":\"钟守韩"
        		+ "\",\"purpose\":\"提现\",\"mobile\":\"13166382981\",\"bankName\":\"工商银行\",\"certificateNo\":\"370983199302183717\",\"head\":{\"traceId\":"
        		+ "\""+traceId+"\",\"charset\":\"UTF-8\",\"partnerNo\":\"ta7E6dop70WE\",\"txnCode\":\"102001\",\"reqDate\":\""+DateFormatUtils.format(new Date(),"yyyyMMdd")+"\",\"partnerType\":\"OUTER\","
        		+ "\"reqTime\":\""+DateFormatUtils.format(new Date(),"yyyyMMddHHmmss")+"\",\"version\":\"1.0.0\"},\"accountNo\":\"6212261001038982085\",\"bankNo\":\"102100099996\",\"currency\":\"156\","
        		+ "\"txnAmt\":\"1056\",\"certificateType\":\"ID\"}";*/
        LOG.info("请求明文："+jsonStr);
        
        LOG.info(publicKey+"请求明文："+privateKey);
        //上游代付公钥
        PublicKey publicKeyplatform = RSAKey
                .getRSAPublicKeyByAbsoluteFileSuffix(publicKey, "pem","RSA");
        //平台秘钥
        PrivateKey privatekeypartner = RSAKey
                .getRSAPrivateKeyByAbsoluteFileSuffix(privateKey, "pem","RSA");
        //加密串
        String randomAESKey =  "6D0DD441A36B7AEE";
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
        LOG.info("请求密文："+nvps);
        byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, nvps);
		String resStr = new String(resByte, StandardCharsets.UTF_8);
		LOG.info("============ 返回报文原文:" + resStr);
		JSONObject resJson = JSON.parseObject(resStr);
		return resJson;

	}

	/**
	 *回调
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jftx/callback")
	public void JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		LOG.info("/v1.0/paymentgateway/quick/jftx/callback异步回调进来了======");

		String rCode="000000";
		Map<String, String[]> parameterMaps = request.getParameterMap();
		Map<String, String> parameterMap =new HashMap<String, String>();
		Set<String> keySet = parameterMaps.keySet();
		for (String key : keySet) {
			String[] strings = parameterMaps.get(key);
			LOG.info(key+"="+strings[0]);
			parameterMap.put(key, strings[0]);
		}
		try {
			 //解密
	        PrivateKey privateKeyPlatform = RSAKey
	                .getRSAPrivateKeyByAbsoluteFileSuffix(privateKey, "pem","RSA"); //秘钥
	        String encryptKey =parameterMap.get("encryptKey");                 //加密只有的AES秘钥
	        String encryptData = parameterMap.get("encryptData");               //加密数据
	        String signData = parameterMap.get("signData");                   //签名数据
	        PublicKey publicKeyPartner = RSAKey
	                .getRSAPublicKeyByAbsoluteFileSuffix(publicKey, "pem","RSA");      //上游公钥
	        //解密128bit 16位AES密钥
	        String partnerAESKey = CommunicationProvideSecurityWarper.getPartnerAESKeyByPlatformPrivateKey(encryptKey,
	                privateKeyPlatform);
	        //使用partnerAESKey解密数据
	        String plainText = CommunicationProvideSecurityWarper.getPlainTextByAESKey(encryptData, partnerAESKey);
	        LOG.info("plainText="+plainText);
	        //验签
	        boolean sinStarts= CommunicationProvideSecurityWarper.checkSignB64PlainTextByPartnerPublicKey(plainText, signData,
	                publicKeyPartner);
	        if(sinStarts){
	        	JSONObject plainTextJson = JSON.parseObject(plainText);
	    	    JSONObject plainheadJson=plainTextJson.getJSONObject("head");
	    	    String orderCode=plainheadJson.getString("traceId");
	    	    String platformId=plainheadJson.getString("platformId");
	    	    String respMsg=plainheadJson.getString("respMsg");
	    	    PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(orderCode);
	            if(plainheadJson.get("respCode").equals("000000")){
	            	this.updateSuccessPaymentOrder(bean.getIpAddress(), orderCode,platformId);
	            }else if(!plainheadJson.get("respCode").equals("200002")){
	            	this.addOrderCauseOfFailure(orderCode, respMsg, bean.getIpAddress());
	            }else{
	            	rCode="999999";
	            }
	        }
		} catch (Exception e) {
			rCode="999999";
		}
		
		PrintWriter pw = response.getWriter();
		pw.print(rCode);
		pw.close();
	}
	//余额查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jftx/CheckBalance")
	public @ResponseBody Object CheckBalance(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		
		String requestURL = "https://payfor.jfpays.com/rest/v1/api/";
    	String url = requestURL + "103001";
        //加密
        String partnerNo = "ta7E6dop70WE";

        //需与报文头的traceId一致
        String traceId =  partnerNo+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());;
        
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
		
		
        /*jsonStr = "{\"callBackUrl\":\"http://106.15.56.208/v1.0/paymentgateway/quick/jftx/callback\",\"accountCategory\":\"PERSON\",\"accountName\":\"钟守韩"
        		+ "\",\"purpose\":\"提现\",\"mobile\":\"13166382981\",\"bankName\":\"工商银行\",\"certificateNo\":\"370983199302183717\",\"head\":{\"traceId\":"
        		+ "\""+traceId+"\",\"charset\":\"UTF-8\",\"partnerNo\":\"ta7E6dop70WE\",\"txnCode\":\"102001\",\"reqDate\":\""+DateFormatUtils.format(new Date(),"yyyyMMdd")+"\",\"partnerType\":\"OUTER\","
        		+ "\"reqTime\":\""+DateFormatUtils.format(new Date(),"yyyyMMddHHmmss")+"\",\"version\":\"1.0.0\"},\"accountNo\":\"6212261001038982085\",\"bankNo\":\"102100099996\",\"currency\":\"156\","
        		+ "\"txnAmt\":\"1056\",\"certificateType\":\"ID\"}";*/
        LOG.info("请求明文："+jsonStr);
        
        LOG.info(publicKey+"请求明文："+privateKey);
        //上游代付公钥
        PublicKey publicKeyplatform = RSAKey
                .getRSAPublicKeyByAbsoluteFileSuffix(publicKey, "pem","RSA");
        //平台秘钥
        PrivateKey privatekeypartner = RSAKey
                .getRSAPrivateKeyByAbsoluteFileSuffix(privateKey, "pem","RSA");
        //加密串
        String randomAESKey =  "6D0DD441A36B7AEE";
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
        LOG.info("请求密文："+nvps);
        byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, nvps);
		String resStr = new String(resByte, StandardCharsets.UTF_8);
		LOG.info("============ 返回报文原文:" + resStr);
		JSONObject resJson = JSON.parseObject(resStr);
		return resJson;
	}
}
