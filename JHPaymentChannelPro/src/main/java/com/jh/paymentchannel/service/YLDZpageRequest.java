package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.chinapay.secss.SecssUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Base64;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.YLDZBindCard;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yldz.SignUtil;
import com.jh.paymentchannel.util.yldz.StringUtil;

import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YLDZpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(YLDZpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String merId = "531111804230001";

	// private String Url =
	// "https://payment.chinapay.com/CTITS/service/rest/forward/syn/000000000017/0/0/0/0/0";

	private String Url1 = "https://payment.chinapay.com/CTITS/service/rest/page/nref/000000000017/0/0/0/0/0";

	private String Url2 = "https://payment.chinapay.com/CTITS/service/rest/forward/syn/000000000017/0/0/0/0/0";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 前台模式支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/fastpay")
	public @ResponseBody Object yldzFrontFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String amount = resultObj.getString("amount");
		String desc = resultObj.getString("desc");

		// 将金额转换为以分为单位:
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);

		Random rm = new Random();
		String random = String.valueOf(rm.nextDouble()).substring(2);
		String orderNo = StringUtil.pad(random, "l", 16, "0");

		map.put("MerOrderNo", ordercode);
		// map.put("MerOrderNo", orderNo);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);

		map.put("OrderAmt", Amount);
		// map.put("TranType", "0004");
		map.put("BusiType", "0001");
		map.put("MerBgUrl", ipAddress + "/v1.0/paymentchannel/topup/yldz/fastpay/notify_call");
		map.put("RemoteAddr", request.getRemoteAddr());

		LOG.info("请求报文======" + map);

		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("加密后的密文 Signature======" + sign);
		LOG.info("请求支付的请求报文======" + map);

		maps.put("resp_code", "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/yldz/fastpay/test?Version=20150922" + "&MerId="
				+ merId + "&MerOrderNo=" + ordercode + "&TranDate=" + substring + "&TranTime=" + substring2
				+ "&OrderAmt=" + Amount + "&amount=" + amount + "&BusiType=0001" + "&MerBgUrl=" + ipAddress
				+ "/v1.0/paymentchannel/topup/yldz/fastpay/notify_call" + "&RemoteAddr=" + request.getRemoteAddr()
				+ "&Signature=" + URLEncoder.encode(sign, "UTF-8") + "&orderDesc=" + URLEncoder.encode(desc, "UTF-8"));

		return maps;

	}

	
	
	//签约并支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/bindcard/andpay")
	public @ResponseBody Object yldzBindCardAndPay(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime) throws Exception {
		LOG.info("开始进入签约并支付接口======");
		
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");

		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String nature = fromObject.getString("nature");

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);
		map.put("MerOrderNo", orderCode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);
		map.put("OrderAmt", Amount);

		map.put("BusiType", "0001");
		map.put("TranType", "0004");
		map.put("MerPageUrl", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
		map.put("MerBgUrl", ipAddress + "/v1.0/paymentchannel/topup/yldz/pay/notify_call");
		
		Map<String, Object> tranReserved = new HashMap<String, Object>();
		
		tranReserved.put("SubTransType", "0001");
		
		SecssUtil secssUtil1 = new SecssUtil();
		if (!tranReserved.isEmpty()) {
			secssUtil1.init();
			secssUtil1.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(tranReserved).getBytes()));
			if (!"00".equals(secssUtil1.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil1.getErrMsg());
				return secssUtil1;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil1.getEncValue() + "]");

		}
		
		map.put("TranReserved", secssUtil1.getEncValue());
		Map<String, Object> cardInfoMap = new HashMap<String, Object>();
		cardInfoMap.put("CardNo", bankCard);
		cardInfoMap.put("CertType", "01");
		cardInfoMap.put("CertNo", idCard);
		cardInfoMap.put("AccName", userName);
		cardInfoMap.put("MobileNo", phone);
		
		if(nature.contains("贷记")) {
			String expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
			
			cardInfoMap.put("CVV2", securityCode);
			cardInfoMap.put("CardValidityPeriod", expiredTimeToMMYY);
		}
		
		SecssUtil secssUtil = new SecssUtil();
		if (!cardInfoMap.isEmpty()) {
			secssUtil.init();
			secssUtil.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(cardInfoMap).getBytes()));
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil.getEncValue() + "]");

		}

		map.put("CardTranData", secssUtil.getEncValue());
		map.put("RemoteAddr", request.getRemoteAddr());
		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("请求签约并支付的请求报文======" + map);

		String doPost = doPost(Url2, map);

		LOG.info("doPost======" + doPost);

		String decode = URLDecoder.decode(doPost);

		LOG.info("decode======" + decode);

		Map<String, String> map2 = new HashMap<String, String>();

		String[] split = decode.split("&");

		for (int i = 0; i < split.length; i++) {

			String string2 = split[i];

			String key = string2.substring(0, string2.indexOf("="));
			String value = string2.substring(string2.indexOf("=") + 1);

			map2.put(key, value);
		}

		LOG.info("map2======" + map2);

		String respCode = map2.get("respCode");
		String respMsg = map2.get("respMsg");

		if ("0000".equals(respCode)) {
			LOG.info("请求签约并支付接口成功======");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		} else {
			
			this.addOrderCauseOfFailure(orderCode, respMsg);
			
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		}

	}
	
	
	// 签约短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/bindcardsms")
	public @ResponseBody Object yldzBindCardSMS(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String nature = fromObject.getString("nature");

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);
		map.put("MerOrderNo", orderCode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);

		map.put("BusiType", "0001");
		map.put("TranType", "0608");

		Map<String, Object> cardInfoMap = new HashMap<String, Object>();
		cardInfoMap.put("CardNo", bankCard);
		cardInfoMap.put("CertType", "01");
		cardInfoMap.put("CertNo", idCard);
		cardInfoMap.put("AccName", userName);
		cardInfoMap.put("MobileNo", phone);
		
		if(nature.contains("贷记")) {
			String expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
			
			cardInfoMap.put("CVV2", securityCode);
			cardInfoMap.put("CardValidityPeriod", expiredTimeToMMYY);
		}
		
		SecssUtil secssUtil = new SecssUtil();
		if (!cardInfoMap.isEmpty()) {
			secssUtil.init();
			secssUtil.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(cardInfoMap).getBytes()));
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil.getEncValue() + "]");

		}

		map.put("CardTranData", secssUtil.getEncValue());

		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("请求签约短信的请求报文======" + map);

		String doPost = doPost(Url2, map);

		LOG.info("doPost======" + doPost);

		String decode = URLDecoder.decode(doPost);

		LOG.info("decode======" + decode);

		Map<String, String> map2 = new HashMap<String, String>();

		String[] split = decode.split("&");

		for (int i = 0; i < split.length; i++) {

			String string2 = split[i];

			String key = string2.substring(0, string2.indexOf("="));
			String value = string2.substring(string2.indexOf("=") + 1);

			map2.put(key, value);
		}

		LOG.info("map2======" + map2);

		String respCode = map2.get("respCode");
		String respMsg = map2.get("respMsg");

		if ("0000".equals(respCode)) {
			LOG.info("请求签约短信接口成功======");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		} else {
			
			this.addOrderCauseOfFailure(orderCode, respMsg);
			
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		}

	}

	// 签约接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/bindcard")
	public @ResponseBody Object yldzBindCard(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime, 
			@RequestParam(value = "smsCode") String smsCode)
			throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String desc = resultObj.getString("desc");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String nature = fromObject.getString("nature");

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);
		map.put("MerOrderNo", orderCode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);

		map.put("BusiType", "0001");
		map.put("TranType", "9904");
		map.put("MerPageUrl", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
		map.put("MerBgUrl", ipAddress + "/v1.0/paymentchannel/topup/yldz/bindcard/notify_call");

		Map<String, Object> cardInfoMap = new HashMap<String, Object>();
		cardInfoMap.put("CardNo", bankCard);
		cardInfoMap.put("CertType", "01");
		cardInfoMap.put("CertNo", idCard);
		cardInfoMap.put("AccName", userName);
		cardInfoMap.put("MobileNo", phone);
		cardInfoMap.put("MobileAuthCode", smsCode);
		
		String expiredTimeToMMYY = null;
		if(nature.contains("贷记")) {
			expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
			
			cardInfoMap.put("CVV2", securityCode);
			cardInfoMap.put("CardValidityPeriod", expiredTimeToMMYY);
		}
		
		SecssUtil secssUtil = new SecssUtil();
		if (!cardInfoMap.isEmpty()) {
			secssUtil.init();
			secssUtil.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(cardInfoMap).getBytes()));
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil.getEncValue() + "]");

		}

		map.put("CardTranData", secssUtil.getEncValue());

		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("加密后的密文 Signature======" + sign);
		LOG.info("请求签约的请求报文======" + map);

		String doPost = doPost(Url2, map);

		LOG.info("doPost======" + doPost);

		String decode = URLDecoder.decode(doPost);

		LOG.info("decode======" + decode);

		Map<String, String> map2 = new HashMap<String, String>();

		String[] split = decode.split("&");

		for (int i = 0; i < split.length; i++) {

			String string2 = split[i];

			String key = string2.substring(0, string2.indexOf("="));
			String value = string2.substring(string2.indexOf("=") + 1);

			map2.put(key, value);
		}

		LOG.info("map2======" + map2);

		String respCode = map2.get("respCode");
		String respMsg = map2.get("respMsg");
		String bankInstNo = map2.get("BankInstNo");

		if ("0000".equals(respCode)) {
			LOG.info("请求签约接口成功======");

			String cardTranData = map2.get("CardTranData");
			
			secssUtil.init();
			secssUtil.decryptData(cardTranData);
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加解密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 解 密 成 功 ,结 果为:" + secssUtil.getDecValue());
			
			JSONObject fromObject2 = JSONObject.fromObject(secssUtil.getDecValue());
			
			YLDZBindCard yldzBindCardByBankCard = topupPayChannelBusiness.getYLDZBindCardByBankCard(bankCard);
			
			if(yldzBindCardByBankCard == null) {
				
				YLDZBindCard yldzBindCard = new YLDZBindCard();
				yldzBindCard.setPhone(phone);
				yldzBindCard.setIdCard(idCard);
				yldzBindCard.setBankCard(bankCard);
				yldzBindCard.setStatus("1");
				
				if(nature.contains("贷记")) {
					yldzBindCard.setSecurityCode(securityCode.trim());
					yldzBindCard.setValidity(expiredTimeToMMYY.trim());
				}
				yldzBindCard.setProtocolNo(fromObject2.getString("ProtocolNo"));

				topupPayChannelBusiness.createYLDZBindCard(yldzBindCard);
				
			}else {
				
				yldzBindCardByBankCard.setStatus("1");
				
				if(nature.contains("贷记")) {
					yldzBindCardByBankCard.setSecurityCode(securityCode.trim());
					yldzBindCardByBankCard.setValidity(expiredTimeToMMYY.trim());
				}
				yldzBindCardByBankCard.setProtocolNo(fromObject2.getString("ProtocolNo"));
				
				topupPayChannelBusiness.createYLDZBindCard(yldzBindCardByBankCard);
			}
			
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			/*maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyldzpay/page?amount=" + amount
					+ "&ordercode=" + orderCode + "&desc="
					+ URLEncoder.encode(desc, "UTF-8") + "&ipAddress=" + ipAddress);*/
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/hljc/bindcard/return_call");
			return maps;
		} else {

			this.addOrderCauseOfFailure(orderCode, respMsg);
			
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		}

	}

	// 支付短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/paysms")
	public @ResponseBody Object yldzPaySMS(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String desc = resultObj.getString("desc");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String nature = fromObject.getString("nature");

		// 将金额转换为以分为单位:
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		YLDZBindCard yldzBindCard = topupPayChannelBusiness.getYLDZBindCardByBankCard(bankCard);
		
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);
		map.put("MerOrderNo", orderCode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);
		map.put("OrderAmt", Amount);
		map.put("TranType", "0606");
		map.put("BusiType", "0001");

		Map<String, Object> cardInfoMap = new HashMap<String, Object>();
		cardInfoMap.put("CardNo", bankCard);
		cardInfoMap.put("CertType", "01");
		cardInfoMap.put("CertNo", idCard);
		cardInfoMap.put("AccName", userName);
		cardInfoMap.put("MobileNo", phone);
		
		if(nature.contains("贷记")) {
			cardInfoMap.put("CVV2", yldzBindCard.getSecurityCode());
			cardInfoMap.put("CardValidityPeriod", yldzBindCard.getValidity());
		}
		
		SecssUtil secssUtil = new SecssUtil();
		if (!cardInfoMap.isEmpty()) {
			secssUtil.init();
			secssUtil.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(cardInfoMap).getBytes()));
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil.getEncValue() + "]");

		}

		map.put("CardTranData", secssUtil.getEncValue());

		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("加密后的密文 Signature======" + sign);
		LOG.info("请求支付短信的请求报文======" + map);

		String doPost = doPost(Url2, map);

		LOG.info("doPost======" + doPost);

		String decode = URLDecoder.decode(doPost);

		LOG.info("decode======" + decode);

		Map<String, String> map2 = new HashMap<String, String>();

		String[] split = decode.split("&");

		for (int i = 0; i < split.length; i++) {

			String string2 = split[i];

			String key = string2.substring(0, string2.indexOf("="));
			String value = string2.substring(string2.indexOf("=") + 1);

			map2.put(key, value);
		}

		LOG.info("map2======" + map2);

		String respCode = map2.get("respCode");
		String respMsg = map2.get("respMsg");
		String bankInstNo = map2.get("BankInstNo");

		if ("0000".equals(respCode)) {
			LOG.info("请求支付短信接口成功======");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;
		} else {

			this.addOrderCauseOfFailure(orderCode, respMsg);
			
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;

		}

	}

	// 支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/pay")
	public @ResponseBody Object yldzFastPayasd(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode
			) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String desc = resultObj.getString("desc");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String nature = fromObject.getString("nature");

		// 将金额转换为以分为单位:
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		YLDZBindCard yldzBindCard = topupPayChannelBusiness.getYLDZBindCardByBankCard(bankCard);
		
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20150922");
		map.put("MerId", merId);
		map.put("MerOrderNo", orderCode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranTime", substring2);
		map.put("OrderAmt", Amount);
		map.put("TranType", "0004");
		map.put("BusiType", "0001");

		map.put("MerPageUrl", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
		map.put("MerBgUrl", ipAddress + "/v1.0/paymentchannel/topup/yldz/pay/notify_call");

		Map<String, Object> cardInfoMap = new HashMap<String, Object>();
		cardInfoMap.put("CardNo", bankCard);
		cardInfoMap.put("ProtocolNo", yldzBindCard.getProtocolNo());
		cardInfoMap.put("CertType", "01");
		cardInfoMap.put("CertNo", idCard);
		cardInfoMap.put("AccName", userName);
		cardInfoMap.put("MobileNo", phone);
		cardInfoMap.put("MobileAuthCode", smsCode);
		
		if(nature.contains("贷记")) {
			cardInfoMap.put("CVV2", yldzBindCard.getSecurityCode());
			cardInfoMap.put("CardValidityPeriod", yldzBindCard.getValidity());
		}
		
		SecssUtil secssUtil = new SecssUtil();
		if (!cardInfoMap.isEmpty()) {
			secssUtil.init();
			secssUtil.encryptData(
					Base64.encodeBase64String(new ObjectMapper().writeValueAsString(cardInfoMap).getBytes()));
			if (!"00".equals(secssUtil.getErrCode())) {
				LOG.info("敏 感 信 息 加 密 发 生 错 误 ， 错 误 信 息 为" + secssUtil.getErrMsg());
				return secssUtil;
			}
			LOG.info(" 敏 感 信 息 加 密 成 功 , 敏 感 信 息 机 密 结 果" + secssUtil.getEncValue() + "]");

		}

		map.put("CardTranData", secssUtil.getEncValue());
		map.put("RemoteAddr", request.getRemoteAddr());
		
		String sign = SignUtil.sign(map);
		
		map.put("Signature", sign);

		LOG.info("加密后的密文 Signature======" + sign);
		LOG.info("请求支付的请求报文======" + map);

		String doPost = doPost(Url2, map);

		LOG.info("doPost======" + doPost);

		String decode = URLDecoder.decode(doPost);

		LOG.info("decode======" + decode);

		Map<String, String> map2 = new HashMap<String, String>();

		String[] split = decode.split("&");

		for (int i = 0; i < split.length; i++) {

			String string2 = split[i];

			String key = string2.substring(0, string2.indexOf("="));
			String value = string2.substring(string2.indexOf("=") + 1);

			map2.put(key, value);
		}

		LOG.info("map2======" + map2);

		String respCode = map2.get("respCode");
		String respMsg = map2.get("respMsg");

		if ("0000".equals(respCode)) {
			LOG.info("请求支付接口成功======");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
			return maps;
		}else if("0014".equals(respCode)) {
			LOG.info("等待扣款======");
			
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/yldzpaying");
			return maps;
		}else {

			this.addOrderCauseOfFailure(orderCode, respMsg);
			
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", respMsg);
			return maps;

		}

	}

	// 交易查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/queryorder")
	public @ResponseBody Object yldzQueryOrder(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String createTime = resultObj.getString("createTime");

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("Version", "20140728");
		map.put("MerId", merId);
		map.put("MerOrderNo", ordercode);

		String format = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		String substring = format.substring(0, 8);
		String substring2 = format.substring(8, 14);

		map.put("TranDate", substring);
		map.put("TranType", "0502");
		map.put("BusiType", "0001");

		LOG.info("请求报文======" + map);

		String sign = SignUtil.sign(map);

		map.put("Signature", sign);

		LOG.info("加密后的密文 Signature======" + sign);
		LOG.info("请求支付的请求报文======" + map);

		RestTemplate template = new RestTemplate();

		String doPost = template.postForObject(
				"https://payment.chinapay.com/CTITS/service/rest/forward/syn/000000000060/0/0/0/0/0", map,
				String.class);

		// String doPost = doPost(Url1, map);

		LOG.info("doPost======" + doPost);

		return null;
	}

	// 前台支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/fastpay/notify_call")
	public void yldzFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷支付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		/*String MerOrderNo = request.getParameter("MerOrderNo");
		String OrderStatus = request.getParameter("OrderStatus");

		if ("0000".equals(OrderStatus)) {

			String url = "http://transactionclear/v1.0/transactionclear/payment/update";

			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", MerOrderNo);
			requestEntity.add("third_code", "");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + MerOrderNo + "====================" + result);

			LOG.info("订单已支付!");

			response.setStatus(200);

		}*/

		
		String OrderStatus = request.getParameter("OrderStatus");
		String MerOrderNo = request.getParameter("MerOrderNo");
		String BankInstNo = request.getParameter("BankInstNo");
		String Signature = request.getParameter("Signature");
		String BusiType = request.getParameter("BusiType");
		String TranType = request.getParameter("TranType");
		String CompleteTime = request.getParameter("CompleteTime");
		String CompleteDate = request.getParameter("CompleteDate");
		String CurryNo = request.getParameter("CurryNo");
		String AcqSeqId = request.getParameter("AcqSeqId");
		String ChannelTime = request.getParameter("ChannelTime");
		String ChannelDate = request.getParameter("ChannelDate");
		String MerId = request.getParameter("MerId");
		String RemoteAddr = request.getParameter("RemoteAddr");
		String ChannelSeqId = request.getParameter("ChannelSeqId");
		String Version = request.getParameter("Version");
		String TranTime = request.getParameter("TranTime");
		String TranDate = request.getParameter("TranDate");
		String OrderAmt = request.getParameter("OrderAmt");
		String AcqDate = request.getParameter("AcqDate");
		
		SecssUtil secssUtil = new SecssUtil();
		
		String decode = URLDecoder.decode(Signature, "UTF-8");
		
		Map<String,Object> myMap = new HashMap<String,Object>();
		//签名串字符串
		myMap.put("Signature", decode);
		//参与签名的字段和值
		myMap.put("OrderStatus", OrderStatus);
		myMap.put("BankInstNo", BankInstNo);
		myMap.put("BusiType", BusiType);
		myMap.put("TranType", TranType);
		myMap.put("CompleteTime", CompleteTime);
		myMap.put("CompleteDate", CompleteDate);
		myMap.put("CurryNo", CurryNo);
		myMap.put("AcqSeqId", AcqSeqId);
		myMap.put("ChannelTime", ChannelTime);
		myMap.put("ChannelDate", ChannelDate);
		myMap.put("MerId", MerId);
		myMap.put("MerOrderNo", MerOrderNo);
		myMap.put("RemoteAddr", RemoteAddr);
		myMap.put("ChannelSeqId", ChannelSeqId);
		myMap.put("Version", Version);
		myMap.put("TranTime", TranTime);
		myMap.put("TranDate", TranDate);
		myMap.put("OrderAmt", OrderAmt);
		myMap.put("AcqDate", AcqDate);
		
		secssUtil.init();
		
		secssUtil.verify(myMap);
		
		if("00".equals(secssUtil.getErrCode())) {
			LOG.info("验签通过。");
			
			if("0000".equals(OrderStatus)) {
				
				this.updateOrderCode(MerOrderNo, "1", "");
				
				LOG.info("订单状态修改成功===================" + MerOrderNo + "====================");

				LOG.info("订单已支付!");
				
				response.setStatus(200);
			}
			
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/yldz/fastpay/test")
	public String hljcBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String Version = request.getParameter("Version");// 结算卡银行名称
		String MerId = request.getParameter("MerId");// 结算卡卡号
		String MerOrderNo = request.getParameter("MerOrderNo");
		String OrderAmt = request.getParameter("OrderAmt");
		String TranDate = request.getParameter("TranDate");
		String TranTime = request.getParameter("TranTime");// 结算卡的卡类型
		// String TranType = request.getParameter("TranType");
		String BusiType = request.getParameter("BusiType");// 信用卡的卡类型
		String MerBgUrl = request.getParameter("MerBgUrl");// 充值卡卡号
		String RemoteAddr = request.getParameter("RemoteAddr");// 充值卡银行名称
		String Signature = request.getParameter(URLDecoder.decode("Signature", "UTF-8"));
		String amount = request.getParameter("amount");
		String orderDesc = request.getParameter("orderDesc");

		LOG.info("Signature=====" + Signature);

		model.addAttribute("Version", Version);
		model.addAttribute("MerId", MerId);
		model.addAttribute("MerOrderNo", MerOrderNo);
		model.addAttribute("TranDate", TranDate);
		model.addAttribute("TranTime", TranTime);
		// model.addAttribute("TranType", TranType);
		model.addAttribute("BusiType", BusiType);
		model.addAttribute("MerBgUrl", MerBgUrl);
		model.addAttribute("RemoteAddr", RemoteAddr);
		model.addAttribute("Signature", Signature);
		model.addAttribute("OrderAmt", OrderAmt);
		model.addAttribute("amount", amount);
		model.addAttribute("orderDesc", orderDesc);

		return "hgweb";

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/bindcard/notify_call")
	public void yldzBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("签约异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String CardNo = request.getParameter("CardNo");
		String SignState = request.getParameter("SignState");

		if ("01".equals(SignState)) {
			LOG.info("签约成功======");
			
			long time1 = new Date().getTime();
			boolean isTrue = true;
			while(isTrue) {
				if(System.currentTimeMillis() >= time1 + 1000) {
					isTrue = false;
					break;
				}
			}
			
			YLDZBindCard yldzBindCard = topupPayChannelBusiness.getYLDZBindCardByBankCard(CardNo);

			yldzBindCard.setStatus("1");

			topupPayChannelBusiness.createYLDZBindCard(yldzBindCard);

			response.setStatus(200);
		}

	}

	// 支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yldz/pay/notify_call")
	public void yldzPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("支付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String OrderStatus = request.getParameter("OrderStatus");
		String MerOrderNo = request.getParameter("MerOrderNo");
		String BankInstNo = request.getParameter("BankInstNo");
		String Signature = request.getParameter("Signature");
		String BusiType = request.getParameter("BusiType");
		String TranType = request.getParameter("TranType");
		String CompleteTime = request.getParameter("CompleteTime");
		String CompleteDate = request.getParameter("CompleteDate");
		String CurryNo = request.getParameter("CurryNo");
		String AcqSeqId = request.getParameter("AcqSeqId");
		String ChannelTime = request.getParameter("ChannelTime");
		String ChannelDate = request.getParameter("ChannelDate");
		String MerId = request.getParameter("MerId");
		String RemoteAddr = request.getParameter("RemoteAddr");
		String ChannelSeqId = request.getParameter("ChannelSeqId");
		String Version = request.getParameter("Version");
		String TranTime = request.getParameter("TranTime");
		String TranDate = request.getParameter("TranDate");
		String OrderAmt = request.getParameter("OrderAmt");
		String AcqDate = request.getParameter("AcqDate");
		
		SecssUtil secssUtil = new SecssUtil();
		
		String decode = URLDecoder.decode(Signature, "UTF-8");
		
		Map<String,Object> myMap = new HashMap<String,Object>();
		//签名串字符串
		myMap.put("Signature", decode);
		//参与签名的字段和值
		myMap.put("OrderStatus", OrderStatus);
		myMap.put("BankInstNo", BankInstNo);
		myMap.put("BusiType", BusiType);
		myMap.put("TranType", TranType);
		myMap.put("CompleteTime", CompleteTime);
		myMap.put("CompleteDate", CompleteDate);
		myMap.put("CurryNo", CurryNo);
		myMap.put("AcqSeqId", AcqSeqId);
		myMap.put("ChannelTime", ChannelTime);
		myMap.put("ChannelDate", ChannelDate);
		myMap.put("MerId", MerId);
		myMap.put("MerOrderNo", MerOrderNo);
		myMap.put("RemoteAddr", RemoteAddr);
		myMap.put("ChannelSeqId", ChannelSeqId);
		myMap.put("Version", Version);
		myMap.put("TranTime", TranTime);
		myMap.put("TranDate", TranDate);
		myMap.put("OrderAmt", OrderAmt);
		myMap.put("AcqDate", AcqDate);
		
		secssUtil.init();
		
		secssUtil.verify(myMap);
		
		if("00".equals(secssUtil.getErrCode())) {
			LOG.info("验签通过。");
			
			if("0000".equals(OrderStatus)) {
				
				this.updateOrderCode(MerOrderNo, "1", "");
				
				LOG.info("订单状态修改成功===================" + MerOrderNo + "====================");

				LOG.info("订单已支付!");
				
				response.setStatus(200);
			}else {
				
				String url = "http://transactionclear/v1.0/transactionclear/payment/update/remark";
				MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
				multiValueMap.add("ordercode", MerOrderNo);
				multiValueMap.add("remark", OrderStatus);
				String result = restTemplate.postForObject(url, multiValueMap, String.class);
				
				response.setStatus(200);
			}
			
		}
		
		
		
	}

	
	
	//跳转签约绑卡的页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toyldzbindcard/page")
	public String toYLDZBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentchannel/topup/toyldzbindcard/page=========");
		String bankName = request.getParameter("bankName");
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardtype = request.getParameter("cardtype");
		String nature = request.getParameter("nature");
		String bankCard = request.getParameter("bankCard");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("amount", amount);
		model.addAttribute("orderCode", ordercode);
		model.addAttribute("cardType", cardtype);
		model.addAttribute("nature", nature);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "yldzbindcard";
	}

	
	//跳转待扣款页面
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/yldzpaying")
	public  String returnpaying(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
	
		return "yldzpaying";
	}
	
	
	//跳转支付页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toyldzpay/page")
	public String toYLDZPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentchannel/topup/toyldzbindcard/page=========");
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String desc = request.getParameter("desc");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("amount", amount);
		model.addAttribute("orderCode", ordercode);
		model.addAttribute("desc", desc);
		model.addAttribute("ipAddress", ipAddress);

		return "yldzpay";
	}
	
	
	// 请求方式
	private static String doPost(String url, Map<String, Object> param) throws IOException {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000) // 服务器返回数据(response)的时间，超过该时间抛出read
																						// timeout
				.setConnectTimeout(5000)// 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
				.setConnectionRequestTimeout(1000)// 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
													// Timeout waiting for connection from pool
				.build();

		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		try {
			if (param != null) {
				// 设置2个post参数
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					parameters.add(new BasicNameValuePair(key, (String) param.get(key)));
				}
				// 构造一个form表单式的实体
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				// 将请求实体设置到httpPost对象中
				httpPost.setEntity(formEntity);
			}

			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		} finally {
			response.close();
		}
		return result;
	}

}