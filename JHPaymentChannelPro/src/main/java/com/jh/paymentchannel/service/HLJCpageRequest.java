package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.HLJCBindCard;
import com.jh.paymentchannel.pojo.HLJCRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.hljc.Base64;
import com.jh.paymentchannel.util.hljc.RSAUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class HLJCpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(HLJCpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;

	//private String agentId = "1001055";
	
	private String agentId = "1001127";

	private String Url = "http://39.108.137.8:8099/v1.0/facade/repay";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	//私钥
	private String Key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAM1i1bY9wqNKPk0ps4USK9pM0BP0bY5n/NwOY2YHAefXJYTLp34YnIuEsZ+uRj72qvMgxHUMwwPlL9xuchU81/up6DDzS30BpbDXQBOdzNMDp+ivXIdqsR+8UwwdzgffatMBZkhGpOMXZWQ6zBK0vsPAVhDE41cj4hOZslm5ewIhAgMBAAECgYAd7PGwrQ0IF9A+E/5pPD2RgDGtRqcT4cjxE1OeURUQ/8Mitz2/XLyqg3oSByWLFQvRgwu89lAP6DvyBVGwEH5zlOyluGl2XM8g8gDiTMA/80c9akk6ZNPz+PA4Lkw/UEbZHNNqoODLv6zTya4eed70b3SHc/iXTskDogQN36+NBQJBAO7ZkDXw7Pf942lqjjusJBu3cb86DcLEjTymeciISIqE8WT8GqRuGih9uBOCcKEOF1W5ZrrnCsg/sX8vW6xaeaMCQQDcIieIv9p6sjze8jm2Bn+uWSmNilebXj+HCp1dVJwrvd8nPq6GG1DKAA5sB1R93o/84iEH+ULpOzXr30yOJdlrAkA0rXsmymoZD7+2IjAYbRDRpBXMLQuX5y2XMMgvOA93rXZn5Uoi9b2DLKcKdnxMqQTwfSFxGz+/hnypJlK7ooCtAkEA0mY2oSa2PJWFRpYAAPGfMdX4uFbkuxRO5dSIaf8HsWsuEcWAa59KDXgWULyEzjVeLBc5+PQONvun4wUvl6GndwJBAOwg0P0Y837h+tpX4hlEiXVFBWUuiukKtWmGCGRELtaqhiS+K0lLIM0oFT3ffMVllpFaQuLp5wpk2c9Gx6G71rY=";
	//公钥
	private String Key1 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDNYtW2PcKjSj5NKbOFEivaTNAT9G2OZ/zcDmNmBwHn1yWEy6d+GJyLhLGfrkY+9qrzIMR1DMMD5S/cbnIVPNf7qegw80t9AaWw10ATnczTA6for1yHarEfvFMMHc4H32rTAWZIRqTjF2VkOswStL7DwFYQxONXI+ITmbJZuXsCIQIDAQAB";
	
	//还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/to/repayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "returnUrl", required = false) String returnUrl
			) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		
		HLJCRegister hljcRegisterByBankCard = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		HLJCBindCard hljcBindCard = topupPayChannelBusiness.getHLJCBindCardByBankCard(bankCard);
		
		if(hljcRegisterByBankCard == null) {
			map = (Map<String, Object>) hljcRegister(request, bankCard, rate, extraFee, userId);
			Object respCode = map.get("resp_code");
			Object respMessage = map.get("resp_message");
			LOG.info("respCode====="+respCode);
			
			if("000000".equals(respCode.toString())) {
				LOG.info("进件成功,开始绑卡======");
				map = (Map<String, Object>) hljcBindCard(request, bankCard, returnUrl, userId);
				Object respCode1 = map.get("resp_code");
				Object respMessage1 = map.get("resp_message");
				if("000000".equals(respCode1.toString())) {
					
					maps.put(CommonConstants.RESP_CODE, "999996");
					maps.put(CommonConstants.RESP_MESSAGE, "首次使用需进行绑卡授权,点击确定进入授权页面!");
					maps.put(CommonConstants.RESULT, map.get("result"));
					return maps;
				}else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				}
				
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
			
		}else {
			if(hljcBindCard == null || !"1".equals(hljcBindCard.getStatus())) {
				maps = (Map<String, Object>) hljcBindCard(request, bankCard, returnUrl, userId);
				return maps;
			}
			/*else if(){
				maps.put(CommonConstants.RESP_CODE, "999995");
				maps.put(CommonConstants.RESP_MESSAGE, "等待绑卡完成");
				return maps;
			}*/
			else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "已完成进件和绑卡");
				return maps;
			}
			
		}
		
	}
	
	
	
	
	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/register")
	public @ResponseBody Object hljcRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "userId") String userId
			) throws Exception {
		LOG.info("开始进入进件接口========================");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);
		
		String userName = fromObject.getString("userName");
		/** 身份证号 */
		String idcard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");

		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");

			return maps;
		}

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/bankcode/getcodebyname";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("name", Util.queryBankNameByBranchName(bankName));
		String result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		String code = null;
		JSONObject jsonObject;
		try {
			jsonObject = JSONObject.fromObject(result);
			// 银行编号
			code = jsonObject.getString("result");
		} catch (Exception e) {
			LOG.error("根据银行名称获取银行编码失败");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");

			return maps;
		}

		String rates = new BigDecimal(rate).multiply(new BigDecimal("10000")).setScale(2).toString();
		rates = rates.substring(0, rates.indexOf("."));
		String extraFees = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + "-" + before;
			} else {
				expiredTime = before + "-" + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");

			return maps;
		}

		LOG.info("转换过的有效期格式======" + expiredTime);

		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "SJ0001");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("isCompay", "0");
		map.put("idcardType", "01");
		map.put("idcard", idcard);
		map.put("name", userName);
		map.put("phone", phone);
		map.put("bankId", inBankUnitNo);
		map.put("bankCard", bankCard);
		map.put("bankName", bankName);
		map.put("bankNo", code);
		map.put("rate", rates);
		map.put("extraFee", extraFees);
		map.put("expDate", expiredTime);
		map.put("CVN2", securityCode);
		LOG.info("map======" + map);
		map.put("sign", signParam(map, Key));

		LOG.info("进件的请求报文======" + map);

		String doPost = doPost(Url, map);

		LOG.info("请求进件返回的 doPost====" + doPost);
		
		fromObject = JSONObject.fromObject(doPost);
		
		String sign = fromObject.getString("sign");
		
		String Code = fromObject.getString("code");
		String message = fromObject.getString("message");
		String respCode = fromObject.getString("respCode");
		String respMessage = fromObject.getString("respMessage");
		
		if("10000".equals(Code) && "SUCCESS".equals(message)) {
			if("10000".equals(respCode) && "SUCCESS".equals(respMessage)) {
				String merId = fromObject.getString("merId");
				
				HLJCRegister hljcRegister = new com.jh.paymentchannel.pojo.HLJCRegister();
				hljcRegister.setPhone(phone);
				hljcRegister.setBankCard(bankCard);
				hljcRegister.setIdCard(idcard);
				hljcRegister.setMerchantCode(merId);
				hljcRegister.setRate(rate);
				hljcRegister.setExtraFee(extraFee);
				
				topupPayChannelBusiness.createHLJCRegister(hljcRegister);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				
				return maps;
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		}else {
			LOG.info("请求失败====");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
		}
		
	}

	
	
	// 绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/bindcard")
	public @ResponseBody Object hljcBindCard(HttpServletRequest request, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "returnUrl", required = false) String returnUrl,
			@RequestParam(value = "userId") String userId
			) throws Exception {
		LOG.info("开始进入绑卡接口======");
		Map<String, String> maps = new HashMap<String, String>();
		
		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);
		
		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");
		
		String expiredTimeToMMYY = null;
		try {
			expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
		} catch (Exception e) {
			LOG.error("转换有效期出错======");
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "您的信用卡有效期填写有误,请及时更改!");
			return maps;
		}
		
		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		
		String orderNo = UUID.randomUUID().toString().replace("-", "");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "YQ0001");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("merId", hljcRegister.getMerchantCode());
		map.put("orderNo", orderNo);
		map.put("phone", phone);
		map.put("bankCard", bankCard);
		map.put("returnUrl", returnUrl);
		map.put("notifyUrl", ipAddress+"/v1.0/paymentchannel/topup/hljc/bindcard/notify_call");
		
		/*map.put("cvv", securityCode);
		map.put("expDate", expiredTimeToMMYY);*/
		LOG.info("map======"+map);
		map.put("sign", signParam(map, Key));
		
		LOG.info("绑卡的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求绑卡返回的 doPost===="+doPost);
		
		fromObject = JSONObject.fromObject(doPost);
		
		String sign = fromObject.getString("sign");
		
		String Code = fromObject.getString("code");
		String message = fromObject.getString("message");
		String respCode = fromObject.getString("respCode");
		String respMessage = fromObject.getString("respMessage");
		
		if("10000".equals(Code) && "SUCCESS".equals(message)) {
			if("10000".equals(respCode) && "SUCCESS".equals(respMessage)) {
				String url = fromObject.getString("url");
				
				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put(CommonConstants.RESP_MESSAGE, "首次使用需进行绑卡授权,点击确定进入授权页面!");
				maps.put(CommonConstants.RESULT, url);
				HLJCBindCard hljcBindCard = topupPayChannelBusiness.getHLJCBindCardByBankCard(bankCard);
				
				if(hljcBindCard == null) {
					HLJCBindCard bindCard = new HLJCBindCard();
					bindCard.setPhone(phone);
					bindCard.setIdCard(idCard);
					bindCard.setBankCard(bankCard);
					bindCard.setStatus("0");
					bindCard.setOrderCode(orderNo);
					bindCard.setToken("");
					
					topupPayChannelBusiness.createHLJCBindCard(bindCard);
					
				}else {
					hljcBindCard.setPhone(phone);
					hljcBindCard.setIdCard(idCard);
					hljcBindCard.setStatus("0");
					hljcBindCard.setOrderCode(orderNo);
					hljcBindCard.setToken("");
					
					topupPayChannelBusiness.createHLJCBindCard(hljcBindCard);
				}
				
				return maps;
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		}else {
			LOG.info("请求失败====");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
		}
		
	}

	
	
	//修改进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/updatemerchant")
	public @ResponseBody Object hljcUpdateMerchant(HttpServletRequest request, 
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "type", required = false) String type
			) throws Exception {
		
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		/** 身份证号 */
		String idcard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");
		
		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");

			return maps;
		}

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/bankcode/getcodebyname";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("name", Util.queryBankNameByBranchName(bankName));
		String result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		String code = null;
		JSONObject jsonObject;
		try {
			jsonObject = JSONObject.fromObject(result);
			// 银行编号
			code = jsonObject.getString("result");
		} catch (Exception e) {
			LOG.error("根据银行名称获取银行编码失败");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");

			return maps;
		}

		String rates = new BigDecimal(rate).multiply(new BigDecimal("10000")).setScale(2).toString();
		rates = rates.substring(0, rates.indexOf("."));
		String extraFees = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + "-" + before;
			} else {
				expiredTime = before + "-" + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");

			return maps;
		}

		LOG.info("转换过的有效期格式======" + expiredTime);
		
		
		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "SJ0002");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("merId", hljcRegister.getMerchantCode());
		
		//修改费率
		if("R".equals(type)) {
			LOG.info("修改费率信息======");
			map.put("type", type);
			map.put("rate", rates);
			map.put("extraFee", extraFees);
			
		}else {
			map.put("type", type);
			map.put("phone", phone);
		}
		
		
		LOG.info("map======"+map);
		map.put("sign", signParam(map, Key));
		
		LOG.info("修改进件的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求修改进件返回的doPost===="+doPost);
		
		fromObject = JSONObject.fromObject(doPost);
		
		String Code = fromObject.getString("code");
		String message = fromObject.getString("message");
		String respCode = fromObject.getString("respCode");
		String respMessage = fromObject.getString("respMessage");
		
		if("10000".equals(Code) && "SUCCESS".equals(message)) {
			if("10000".equals(respCode) && "修改成功！".equals(respMessage)) {
				
				if("R".equals(type)) {
					hljcRegister.setRate(rate);
					hljcRegister.setExtraFee(extraFee);
				}else {
					hljcRegister.setPhone(phone);
				}
				
				topupPayChannelBusiness.createHLJCRegister(hljcRegister);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		}else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
		}
	}
	
	
	
	//快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/fastpay")
	public @ResponseBody Object hljcFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode
			) throws Exception {
		LOG.info("开始进入快捷支付接口======");
		
		Map<String, Object> maps = new HashMap<String, Object>();
		
		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		
		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		
		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		
		HLJCBindCard hljcBindCard = topupPayChannelBusiness.getHLJCBindCardByBankCard(bankCard);
		
		String Amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "YQ0002");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("merId", hljcRegister.getMerchantCode());
		map.put("orderNo", ordercode);
		map.put("amount", Amount);
		map.put("bankCard", bankCard);
		map.put("notifyUrl", ipAddress+"/v1.0/paymentchannel/topup/hljc/fastpay/notify_call");
		map.put("token", hljcBindCard.getToken());
		
		LOG.info("map======"+map);
		map.put("sign", signParam(map, Key));
		
		LOG.info("快捷支付的请求报文======"+map);
		
		String Code = null;
		String message = null;
		String respCode = null;
		String respMessage = null;
		try {
			String doPost = doPost(Url, map);
			
			LOG.info("请求快捷支付返回的doPost===="+doPost);
			
			fromObject = JSONObject.fromObject(doPost);
			
			Code = fromObject.getString("code");
			message = fromObject.getString("message");
			respCode = fromObject.getString("respCode");
			respMessage = fromObject.getString("respMessage");
		} catch (Exception e) {
			
		}
		
		if("10000".equals(Code) && "SUCCESS".equals(message)) {
			if("10000".equals(respCode)) {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;
			}else if("10002".equals(respCode)){
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
			
		}else {
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
		}
		
	}
	
	
	//代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/transfer")
	public @ResponseBody Object hljcTransfer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode
		) throws Exception {
		LOG.info("开始进入代付接口======");
		Map<String, Object> maps = new HashMap<String, Object>();
		
		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		/** 身份证号 */
		String idcard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		
		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		
		String Amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "YQ0003");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("merId", hljcRegister.getMerchantCode());
		map.put("orderNo", ordercode);
		map.put("amount", Amount);
		map.put("bankCard", bankCard);
		map.put("bankName", bankName);
		map.put("phone", phone);
		map.put("notifyUrl", ipAddress+"/v1.0/paymentchannel/topup/hljc/transfer/notify_call");
		
		LOG.info("map======"+map);
		//String readFile = RSAUtils.readFile(prv, "UTF-8");
		//log.info("秘钥======"+readFile);
		map.put("sign", signParam(map, Key));
		
		LOG.info("代付的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求代付返回的doPost===="+doPost);
		
		fromObject = JSONObject.fromObject(doPost);
		
		String Code = fromObject.getString("code");
		String message = fromObject.getString("message");
		String respCode = fromObject.getString("respCode");
		String respMessage = fromObject.getString("respMessage");
		
		if("10000".equals(Code) && "SUCCESS".equals(message)) {
			if("10000".equals(respCode)) {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
				return maps;
			}else if("10002".equals(respCode)){
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
				return maps;
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
			
		}else {
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
		}
		
	}
	
	
	
	
	//订单查询接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/hljc/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode
	) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "YQ0004");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("orderNo", ordercode);
		map.put("sign", signParam(map, Key));
		
		LOG.info("查询订单的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求查询订单返回的doPost===="+doPost);
		
		JSONObject fromObject = JSONObject.fromObject(doPost);
		
		String code = fromObject.getString("code");
		String message = fromObject.getString("message");
		
		if("10000".equals(code)) {
			if(fromObject.has("payStatus")) {
				
				String payStatus = fromObject.getString("payStatus");
				String payComment = fromObject.getString("payComment");
				if("1".equals(payStatus)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, payComment);
				}else if("2".equals(payStatus)){
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, payComment);
				}else if("3".equals(payStatus)){
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, payComment);
				}else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, payComment);
				}
				
			}else {
				String respMessage = fromObject.getString("respMessage");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			}
			
		}else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
		}
		
		return maps;		
	}
	
	
	
	//代理商余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/queryagencybalance")
	public @ResponseBody Object agencyBalanceQuery(HttpServletRequest request
			) throws Exception {
		
		Map<String, String> maps = new HashMap<String, String>();
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "SJ0003");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("agentId", agentId);
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("sign", signParam(map, Key));
		
		LOG.info("查询代理商余额的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求查询代理商余额返回的doPost===="+doPost);
		
		JSONObject fromObject = JSONObject.fromObject(doPost);
		
		String code = fromObject.getString("code");
		String message = fromObject.getString("message");
		String respCode = fromObject.getString("respCode");
		String respMessage = fromObject.getString("respMessage");
		String balance = fromObject.getString("balance");
		String freezeBalance = fromObject.getString("freezeBalance");
		
		if("10000".equals(code) && "SUCCESS".equalsIgnoreCase(message)) {
			if("10000".equals(respCode)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("balance", balance);
				maps.put("freezeBalance", freezeBalance);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			}
		}else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
		}
		
		return maps;
	}
	
	
	
	//子商户余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/querycustomerbalance")
	public @ResponseBody Object customerBalanceQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode
			) throws Exception {
		
		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		String bankCard = fromObject.getString("bankcard");
		
		HLJCRegister hljcRegister = topupPayChannelBusiness.getHLJCRegisterByBankCard(bankCard);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "1.0");
		map.put("serviceUri", "SJ0004");
		map.put("charset", "UTF-8");
		map.put("signType", "RSA");
		map.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
		map.put("agentId", agentId);
		map.put("merId", hljcRegister.getMerchantCode());
		map.put("sign", signParam(map, Key));
		
		LOG.info("查询代理商余额的请求报文======"+map);
		
		String doPost = doPost(Url, map);
		
		LOG.info("请求查询代理商余额返回的doPost===="+doPost);
		
		fromObject = JSONObject.fromObject(doPost);
		
		
		
		return null;
	}
	
	
	
	//代付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/transfer/notify_call")
	public void hljcTransferNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("代付异步通知进来了=======");

		String respMessage = request.getParameter("respMessage");
		String respCode = request.getParameter("respCode");
		String orderNo = request.getParameter("orderNo");
		String orderNum = request.getParameter("orderNum");
		String sign = request.getParameter("sign");
		
		if("10000".equals(respCode)) {
			
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			requestEntity.add("version", "2");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			requestEntity.add("third_code", orderNum);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+orderNo+"====================" + result);

			LOG.info("订单已代付!");
			
			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();
			
		}
		
		
		
		
	}
	
	
	
	//快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/fastpay/notify_call")
	public void hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷支付异步通知进来了=======");

		String respMessage = request.getParameter("respMessage");
		String respCode = request.getParameter("respCode");
		String orderNo = request.getParameter("orderNo");
		String orderNum = request.getParameter("orderNum");
		String sign = request.getParameter("sign");
		
		if("10000".equals(respCode)) {
			
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			requestEntity.add("version", "2");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			requestEntity.add("third_code", orderNum);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+orderNo+"====================" + result);

			LOG.info("订单已支付!");
			
			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();
			
		}
		
		
	}
	
	
	
	//绑卡同步跳转
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/hljc/bindcard/return_call")
	public String hljcReturnCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map map = new HashMap();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		LOG.info("卡开通同步通知进来了");

		return "cjbindcardsuccess";

	}
	
	
	
	
	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/hljc/bindcard/notify_call")
	public void hljcBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("银联绑卡异步通知进来了=======");

		String msg = request.getParameter("msg");
		String code = request.getParameter("code");
		String orderNo = request.getParameter("orderNo");
		String sign = request.getParameter("sign");
		
		if("10000".equals(code) && "绑定成功".equals(msg)) {
			
			HLJCBindCard hljcBindCard = topupPayChannelBusiness.getHLJCBindCardByOrderCode(orderNo);
			
			hljcBindCard.setStatus("1");
			
			topupPayChannelBusiness.createHLJCBindCard(hljcBindCard);
			
			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();
			
		}
		

	}

	// 加密方法
	public static String signParam(Map<String, String> map, String key) {
		TreeMap<String, String> param = new TreeMap<String, String>(map);
		String signInfo = "";
		for (String pkey : param.keySet()) {
			if (StringUtils.isEmpty(param.get(pkey)) || "sign".equals(pkey)) {
				continue;
			}
			signInfo += pkey + "=" + param.get(pkey) + "&";
		}
		signInfo = signInfo.substring(0, signInfo.length() - 1);
		System.out.println("RSA加签参数：" + signInfo);
		String sign = null;
		try {
			byte[] data = RSAUtils.encryptByPrivateKey(signInfo.getBytes(), key);
			sign = Base64.encode(data);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
		}
		return sign;
	}

	// 请求方式
	private static String doPost(String url, Map<String, String> param) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000) //服务器返回数据(response)的时间，超过该时间抛出read timeout
                .setConnectTimeout(5000)//连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                .setConnectionRequestTimeout(1000)//从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
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
			e.printStackTrace();LOG.error("",e);
		} finally {
			response.close();
		}
		return result;
	}

}