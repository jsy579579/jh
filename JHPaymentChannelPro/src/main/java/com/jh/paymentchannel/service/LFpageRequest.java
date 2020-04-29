package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.LFQuickRegister;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.Base64;
import cn.jh.common.tools.Log;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LFpageRequest {

	private static final Logger log = LoggerFactory.getLogger(LFpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Value("${lf.registerUrl}") 
	private String registerUrl;

	@Value("${lf.modifyUrl}")
	private String modifyUrl;

	@Value("${lf.sendUrl}")
	private String sendUrl; 

	@Value("${lf.payUrl}")
	private String payUrl; 
	
	@Value("${lf.updateUrl}")
	private String updateUrl; 
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	private String merchantCode = "46533032";

	private String PublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDh+FImTmTPeyx3cx9rhfM2YlZ40vZuZUkp7/Q3zvikriaWUsKBxDrONJAPklJma2nfXMlEmhg6G3ZDeNGAb6TZMOMjIlxC/68xYDOrydh2UE7wHKyZg+UyHfUhQgOw+bhjb+Z2kzXJW08J6S7skcw24O9LLBjFSBLh5Pmky2AuywIDAQAB";

	// 商户进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/register")
	public @ResponseBody Object ldRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		log.info("开始进入进件接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}
		

		String cardName = resultObj.getString("bankName");

		String cardname = Util.queryBankNameByBranchName(cardName);
		String cardtype = resultObj.getString("cardType");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/randomuserid";
		requestEntity = new LinkedMultiValueMap<String, String>();

		String shopUserId;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			shopUserId = jsonObject.getString("result");
		} catch (Exception e1) {
			log.error("查询用户ID出错！！！！");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "没有查询到用户ID");
			return maps;
		}

		log.info("随机获取的userId" + shopUserId);

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/query/uid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询商铺信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询商铺信息有误");
			return map;
		}

		String shopName = resultObj.getString("name");// 商户全称
		String address = resultObj.getString("address");
		String shopsaddress = resultObj.getString("shopsaddress");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bankcode/getcodebyname";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("name", Util.queryBankNameByBranchName(bankName));
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		String code = null;
		try {
			jsonObject = JSONObject.fromObject(result);
			// 银行编号
			code = jsonObject.getString("result");
		} catch (Exception e) {
			log.error("根据银行名称获取银行编码失败");
			maps.put("resp_code", "failed");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "暂不支持该结算银行,请及时更换结算银行卡!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return maps;
		}

		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "暂不支持该结算银行,请及时更换结算银行卡!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}


		MultiValueMap<String, String> resp = new LinkedMultiValueMap<String, String>();

		resp.add("merchName", shopName);// 商户全称
		resp.add("nickName", shopName);// 商户简称
		resp.add("servicePhone", phone);// 客服电话
		resp.add("contacts", userName);// 联系人名称
		resp.add("contactsTel", phone);// 联系人电话
		resp.add("contactMobile", phone);// 联系人手机号
		resp.add("contactAdress", address + shopsaddress);// 联系人地址
		resp.add("categoryCode", "205");// 经营类目
		resp.add("remark", "商户进件");// 备注
		resp.add("merchantCode", merchantCode);// 接入大商户号
		resp.add("outMerchantCode", userid);// 接入商二级商户号
		resp.add("accNo", cardNo);// 银行卡卡号
		resp.add("accName", userName);// 银行卡的开户人姓名
		resp.add("accType", "D");// 账户类型
		resp.add("openBank", bankName);// 开户行
		resp.add("bankCode", code);// 开户行简称
		resp.add("alliedBankCode", inBankUnitNo);// 联行号
		resp.add("tradeRate", rate);// 商户费率
		resp.add("remitFee", extraFee);// 代出款手续费（每笔）
		resp.add("settlePeriod", "T0");// 结算周期
		resp.add("merchantNotifyUrl", ipAddress + "/v1.0/paymentchannel/topup/lf/register/notify_call");// 商户通知url
		resp.add("certNo", idcard);// 证件号
		resp.add("fastpayPointsTradeRate", rate);// 快捷费率

		log.info("上送报文======" + resp);

		try {
			String postForObject = restTemplate.postForObject(registerUrl, resp, String.class);

			log.info("postForObject======" + postForObject);

			JSONObject fromObject = JSONObject.fromObject(postForObject);

			String errorCode = fromObject.getString("error_code");
			String errorMessage = fromObject.getString("error_message");

			if("0".equals(errorCode)) {
				
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("redirect_url",
						ipAddress + "/v1.0/paymentchannel/topup/lfpay?ordercode=" + ordercode + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&bankName=" + URLEncoder.encode(cardname, "UTF-8")
								+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ipAddress="
								+ ipAddress);
				return map;
				
			}else {
				log.info("进件失败=====");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", errorMessage);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", errorCode+" : "+errorMessage);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}
		} catch (Exception e) {
			log.error("请求进件接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,进件失败了,请稍后重试");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "很抱歉,进件失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
		
	}

	
	// 获取短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/consumesms")
	public @ResponseBody Object lfPaySms(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		log.info("开始进入获取短信接口=======");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String amount = resultObj.getString("amount");
		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String phone = resultObj.getString("phone");
		// 身份证号
		String idCard = resultObj.getString("idcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String userName = resultObj.getString("userName");
		String idcard = resultObj.getString("idcard");
		String phone1 = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		LFQuickRegister lfQuickRegister;
		String merchantNo = null;
		try {
			lfQuickRegister = topupPayChannelBusiness.getLFQuickRegisterByIdCard(idCard);
			merchantNo = lfQuickRegister.getMerchantNo();
		} catch (Exception e) {
			log.error("获取莱付进件信息有误======"+e);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/deletepayment/byordercode";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试!");
			return map;
		}
		
		
		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = before + after;
			} else {
				expiredTime = after + before;
			}
		} catch (Exception e) {
			log.error("转换有效期格式有误======="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		log.info("转换过的有效期格式======" + expiredTime);

		MultiValueMap<String, String> resp = new LinkedMultiValueMap<String, String>();

		resp.add("orderNo", ordercode);// 商户订单号
		resp.add("amount", amount);// 交易金额
		resp.add("merchCode", merchantCode);// 接入商编号
		resp.add("subMerchCode", merchantNo);// 莱付商户号
		resp.add("goodsName", "充值缴费");// 商品名称
		resp.add("channelCode", "fastSoopay");// 通道编码
		resp.add("typeCode", "T0");// 交易类型
		resp.add("notifyUrl", ipAddress + "/v1.0/paymentchannel/topup/lf/notify_call");// 回调地址
		resp.add("remark", "获取短信");// 摘要
		resp.add("criditNo", bankCard);// 交易卡号
		resp.add("bankPhoneNo", phone1);// 银行预留手机号

		securityCode = this.encryptByPublicKey(securityCode.getBytes(), PublicKey);

		log.info("加密后的securityCode======" + securityCode);

		resp.add("cvv", securityCode);// Cvv
		resp.add("expireDate", expiredTime);// 有效期

		log.info("上送报文======" + resp);

		try {
			String postForObject = restTemplate.postForObject(sendUrl, resp, String.class);

			log.info("postForObject======" + postForObject);

			JSONObject fromObject = JSONObject.fromObject(postForObject);

			String errorCode = fromObject.getString("errorCode");
			String errorMessage = fromObject.getString("errorMessage");
			String flag = fromObject.getString("flag");

			if ("000000".equals(errorCode)) {
				log.info("请求获取短信接口成功======");
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("user", "error url request!");
				url = uri.toString() + "/v1.0/user/bank/update/bynewcardno";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("cardNo", bankCard);
				requestEntity.add("type", "0");
				requestEntity.add("securityCode", securityCode);
				requestEntity.add("expiredTime", expiredTime);
				result = restTemplate.postForObject(url, requestEntity, String.class);

				log.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");

				map.put("resp_code", "success");
				map.put("channel_type", "jf");

				return map;
			} else {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", errorMessage);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", errorCode+" : "+errorMessage);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}
		} catch (Exception e) {
			log.error("请求获取短信接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中，请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "交易排队中，请稍后重试!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

	}

	
	// 支付确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/consume")
	public @ResponseBody Object consume(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

		log.info("开始进入支付确认接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		String amount = resultObj.getString("amount");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}

		// 身份证号
		String idCard = resultObj.getString("idcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = before + after;
			} else {
				expiredTime = after + before;
			}
		} catch (Exception e) {
			log.error("转换有效期格式有误======="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		log.info("转换过的有效期格式======" + expiredTime);
		
		String phone1 = resultObj.getString("phone");

		LFQuickRegister lfQuickRegister;
		String merchantNo = null;
		try {
			lfQuickRegister = topupPayChannelBusiness.getLFQuickRegisterByIdCard(idCard);
			merchantNo = lfQuickRegister.getMerchantNo();
		} catch (Exception e) {
			log.error("获取莱付进件信息有误======"+e);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/deletepayment/byordercode";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试!");
			return map;
		}

		MultiValueMap<String, String> resp = new LinkedMultiValueMap<String, String>();

		resp.add("orderNo", ordercode);// 商户订单号
		resp.add("subMerchCode", merchantNo);// 莱付商户号
		resp.add("amount", amount);// 交易金额
		resp.add("notifyUrl", ipAddress + "/v1.0/paymentchannel/topup/lf/notify_call");// 回调地址
		resp.add("smsCode", smsCode);// 接入商编号

		securityCode = this.encryptByPublicKey(securityCode.getBytes(), PublicKey);

		log.info("加密后的securityCode======" + securityCode);

		resp.add("cvv", securityCode);// Cvv
		resp.add("expireDate", expiredTime);// 有效期
		resp.add("channelCode", "fastSoopay");// 通道编码

		resp.add("criditNo", bankCard);// 交易卡号
		resp.add("bankPhoneNo", phone1);// 银行预留手机号

		log.info("上送报文======" + resp);

		String errorCode = null;
		String errorMessage = null;
		try {
			String postForObject = restTemplate.postForObject(payUrl, resp, String.class);

			log.info("postForObject======" + postForObject);

			JSONObject fromObject = JSONObject.fromObject(postForObject);
			
			errorCode = fromObject.getString("errorCode");
			errorMessage = fromObject.getString("errorMessage");
			
			log.info("errorCode=====" + errorCode);
			log.info("errorMessage=====" + errorMessage);
		} catch (Exception e) {
			log.error("请求支付接口失败======="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", errorMessage);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", errorMessage);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		if ("000000".equals(errorCode)) {
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");

		} else {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			//map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpayerror");
			map.put("resp_message", "支付失败！ 失败原因: "+errorMessage);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", errorCode+" : "+errorMessage);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
		}

		return map;
	}

	
	
	// 结算卡修改接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/updateRegister")
	public @ResponseBody Object updateCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		log.info("开始进入结算卡修改接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");

		String userid = resultObj.getString("userid");

		String rate = resultObj.getString("rate");
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		resultObj = jsonObject.getJSONObject("result");

		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");

		String cardname = Util.queryBankNameByBranchName(cardName);
		String bankname = Util.queryBankNameByBranchName(bankName);
		String cardtype = resultObj.getString("cardType");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bankcode/getcodebyname";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("name", Util.queryBankNameByBranchName(bankName));
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		String code = null;
		try {
			jsonObject = JSONObject.fromObject(result);
			// 银行编号
			code = jsonObject.getString("result");
		} catch (Exception e) {
			log.error("根据银行名称获取银行编码失败");
			maps.put("resp_code", "failed");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "暂不支持该结算银行,请及时更换结算银行卡!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return maps;
		}


		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "暂不支持该结算银行,请及时更换结算银行卡!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
		LFQuickRegister lfQuickRegister;
		String merchantNo = null;
		try {
			lfQuickRegister = topupPayChannelBusiness.getLFQuickRegisterByIdCard(idcard);
			merchantNo = lfQuickRegister.getMerchantNo();
		} catch (Exception e) {
			log.error("获取莱付进件信息有误======"+e);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/deletepayment/byordercode";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试!");
			return map;
		}
		
		MultiValueMap<String, String> resp = new LinkedMultiValueMap<String, String>();

		resp.add("subMerchantCode", merchantNo);// 商户id
		resp.add("AccNo", cardNo);// 银行卡号
		resp.add("mobileBank", phone);// 银行预留手机号
		resp.add("alliedBankCode", inBankUnitNo);// 联行号
		resp.add("openBank", bankname);// 开户行
		resp.add("bankCode", code);// 开户行编号
		resp.add("merchantCode", merchantCode);// 大商户号
		
		log.info("上送报文======" + resp);

		try {
			String postForObject = restTemplate.postForObject(modifyUrl, resp, String.class);

			log.info("postForObject======" + postForObject);
			
			JSONObject fromObject = JSONObject.fromObject(postForObject);
			
			String errorCode = fromObject.getString("error_code");
			String errorMessage = fromObject.getString("error_message");
			
			if("000000".equals(errorCode)) {
				log.info("用户结算卡变更成功======");
				
				resp = new LinkedMultiValueMap<String, String>();

				resp.add("subMerchantCode", lfQuickRegister.getMerchantNo());
				resp.add("merchantCode", merchantCode);
				resp.add("tradeRate", rate);
				resp.add("fastpayRate", rate);
				
				log.info("上送报文======" + resp);
				
				postForObject = restTemplate.postForObject(updateUrl, resp, String.class);

				log.info("postForObject======" + postForObject);

				fromObject = JSONObject.fromObject(postForObject);
				errorCode = fromObject.getString("error_code");
				errorMessage = fromObject.getString("error_message");

				log.info("errorCode====="+errorCode);
				log.info("errorMessage====="+errorMessage);
				
				if("0".equals(errorCode)) {
					log.info("修改费率成功======");
					
					lfQuickRegister.setRate(rate);
					lfQuickRegister.setBankCard(cardNo);
					lfQuickRegister.setPhone(phone);
					
					topupPayChannelBusiness.createLFQuickRegister(lfQuickRegister);
					
					map.put("resp_code", "success");
					map.put("channel_type", "jf");
					map.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/lfpay?ordercode=" + ordercode + "&expiredTime=" + expiredTime
									+ "&securityCode=" + securityCode + "&bankName=" + URLEncoder.encode(cardname, "UTF-8")
									+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ipAddress="
									+ ipAddress);
					return map;
					
				}else {
					log.info("修改费率失败======"+errorMessage);
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", errorMessage);
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", errorCode+" : "+errorMessage);
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}
				
			}else {
				log.info("用户结算卡变更失败======");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", errorMessage);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", errorCode+" : "+errorMessage);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
				
			}
		} catch (Exception e) {
			log.error("请求结算卡变更接口出错啦======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中，请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "交易排队中，请稍后重试!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/turntopaypage")
	public @ResponseBody Object turnToCJPayPage(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}
		if (resultObj == null) {
			return null;
		}

		String cardName = resultObj.getString("bankName");
		String cardtype = resultObj.getString("cardType");

		String cardname = Util.queryBankNameByBranchName(cardName);

		map.put("resp_code", "success");
		map.put("channel_type", "jf");
		map.put("redirect_url",
				ipAddress + "/v1.0/paymentchannel/topup/lfpay?ordercode=" + ordercode + "&expiredTime=" + expiredTime
						+ "&securityCode=" + securityCode + "&bankName=" + URLEncoder.encode(cardname, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ipAddress="
						+ ipAddress);
		return map;

	}

	// 中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/lfpay")
	public String returnjfapipay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("ordercode");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "lfpaymessage";
	}

	// 跳转确认提现卡页面的中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tolfbankinfo")
	public String tojfshangaobankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		log.info("/v1.0/paymentchannel/topup/toldbankinfo=========toldbankinfo");
		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");// 结算卡的卡类型
		String isRegister = request.getParameter("isRegister");
		String cardtype = request.getParameter("cardtype");// 信用卡的卡类型
		String bankCard = request.getParameter("bankCard");// 充值卡卡号
		String cardName = request.getParameter("cardName");// 充值卡银行名称
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("isRegister", isRegister);
		model.addAttribute("cardtype", cardtype);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("cardName", cardName);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "lfbankinfo";
	}

	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/register/notify_call")
	public void registernotifyCall(HttpServletRequest request, HttpServletResponse response

	) throws Exception {
		log.info("商户进件异步通知进来了=====");

		String errorMessage = request.getParameter("error_message");
		String returnValue = request.getParameter("return_value");
		String errorCode = request.getParameter("error_code");

		log.info("errorMessage======" + errorMessage);
		log.info("returnValue======" + returnValue);
		log.info("errorCode======" + errorCode);

		if ("0".equals(errorCode)) {

			JSONObject fromObject = JSONObject.fromObject(returnValue);

			String accNo = fromObject.getString("accNo");
			String outMerchantCode = fromObject.getString("outMerchantCode");
			String subMerchantCode = fromObject.getString("subMerchantCode");
			String tradeRate = fromObject.getString("tradeRate");

			Map<String, String> maps = new HashMap<String, String>();
			RestTemplate restTemplate = new RestTemplate();
			URI uri = util.getServiceUrl("user", "error url request!");
			String url = uri.toString() + "/v1.0/user/bank/default/cardno";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("cardno", accNo);
			requestEntity.add("type", "2");
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			log.info("RESULT================" + result);
			JSONObject jsonObject;
			JSONObject resultObj = null;
			try {
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e1) {
				log.error("查询银行卡信息出错====="+e1);
			}

			String idcard = resultObj.getString("idcard");
			String phone = resultObj.getString("phone");

			LFQuickRegister lfQuickRegister = new LFQuickRegister();
			lfQuickRegister.setPhone(phone);
			lfQuickRegister.setBankCard(accNo);
			lfQuickRegister.setIdCard(idcard);
			lfQuickRegister.setMerchantNo(subMerchantCode);
			lfQuickRegister.setRate(tradeRate);
			lfQuickRegister.setExtraFee("2.0");

			topupPayChannelBusiness.createLFQuickRegister(lfQuickRegister);

		}

	}

	// 支付接口异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/lf/notify_call")
	public void paynotifyCall(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		log.info("支付接口异步回调进来了====");
		/*Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for(String key:keySet) {
			String[] strings = parameterMap.get(key);
			for(String s : strings) {
				System.out.println(key + "=============" + s);
			}
		}*/
		
		String orderno = request.getParameter("Orderno");
		String status = request.getParameter("Status");
		String tradeOrderNo = request.getParameter("TradeOrderNo");
		String statusDesc = request.getParameter("StatusDesc");

		log.info("订单号 orderno======" + orderno);
		log.info("待付状态 status======" + status);
		log.info("代付单号 tradeOrderNo======" + tradeOrderNo);
		log.info("代付信息描述 statusDesc======" + statusDesc);

		try {
			Log.setLogFlag(true);
			Log.println("---交易： 订单结果异步通知-------------------------");

			log.info("交易： 订单结果异步通知===================");
			if ("Y".equals(status)) { // 订单已支付;

				// 1、检查Amount和商户系统的订单金额是否一致
				// 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
				// 3、返回响应内容

				synchronized (this) {
					// **更新订单状态*//*
					// **调用下单，需要得到用户的订单信息*//*
					RestTemplate restTemplate = new RestTemplate();

					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";

					// **根据的用户手机号码查询用户的基本信息*//*
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("third_code", tradeOrderNo);
					requestEntity.add("order_code", orderno);
					String result = restTemplate.postForObject(url, requestEntity, String.class);

					log.info("订单状态修改成功===================");

					log.info("订单已支付!");
				}
			} else {
				// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
				// 2、返回响应内容

				log.info("订单支付失败!");
			}
		} catch (Exception e) {
			log.error("",e);
		}
		log.info("-----处理完成----");
	}

	
	public String encryptByPublicKey(byte[] data, String publicKey) throws Exception {
		byte[] keyBytes = Base64.decode(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(1, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > 117) {
				cache = cipher.doFinal(data, offSet, 117);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * 117;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return Base64.encode(encryptedData);
	}

}