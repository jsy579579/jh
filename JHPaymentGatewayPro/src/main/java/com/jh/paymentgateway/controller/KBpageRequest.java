package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.KBBindCard;
import com.jh.paymentgateway.pojo.KBRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.HttpUtils;
import com.jh.paymentgateway.util.kb.AllinpayUtils;
import com.jh.paymentgateway.util.kb.BindCardApplyReq;
import com.jh.paymentgateway.util.kb.BindCardConfirmReq;
import com.jh.paymentgateway.util.kb.BindDebitCardReq;
import com.jh.paymentgateway.util.kb.FastPayApplyReq;
import com.jh.paymentgateway.util.kb.FastPayConfirmReq;
import com.jh.paymentgateway.util.kb.OrderQueryReq;
import com.jh.paymentgateway.util.kb.RegisterReq;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class KBpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(KBpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static String merchantId = "514036527598403584";

	private static String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKb6YXZivuS1TK9eA7siliCf+z91St16yHpNbfSM4SpBfTi2gq1iOitlvBzv3U7dfsHVK+vWMCfP7Y9PKoeOH3El0QYyMeyGgwOj7/vJJtxV0RVmwAEd2++mmtzq0xYJN5+b892iob8xfRQcKojPHFr1KnH7XfJe58HmMnJXv7HDAgMBAAECgYBERFWYeyKkiuMBR6Sq26cZS48DEMc86bgRGJr9wqNEDWZOy399t40ktQFFq2OFnNT4FllQoE8r17y+PJWcuiRsCRYfasK77LCfd98yOtqDaBLGHhwYXGILdmq6dwoabU3pWNpLxBGeBvB1OXc1s29HB9old0HIwIEw5X28/nVRsQJBAPB4m/kv2lgUUMJwY3VO1334luvazM2IPGrLeYCpyiZFckLWNYUXSm68pF5SRor9axZB2jvQ19m6Akg5ocytJ5cCQQCxws7Dt5QbeKckspOC+l+85o5oh5/QqFCvdFiGYSgLTOscH8WYwo74/zAl+7uP9ENAJEuHdH3Oev3ntODcOWy1AkBmjTkPSx4VxAww79cqlwYFffd2/CetW3VQohfeDAreyW0SHeJTMPyYPzDl3Lai3bJGmqzkJ4t2GgKRJKrg69NfAkEAn441WzWHalUU6fqkL1ee4Yas4qSBzZ+WtLYg05WXhPUov5jBGwnfnR4pUJ6wz1i3mHY7mTz1w4VgLD+N6f5dVQJAf5YjjwMM4dc939eqa1kts8pluKaqLzkA09/4NMsXgI4RaA7zxjYvc+gab3FCG5ig6FeRDXyMMFqeV8hb7PiPEA==";

	private static String key = "c5c25fe43f63b12839900dc8faf82160";

	private String url = "https://openapi.allinpaycard.com/kabao2.api/";

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/register")
	public @ResponseBody Object KBQuickRegister(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "productCode") String productCode
			) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phone = prp.getDebitPhone();
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String debitCardNo = prp.getDebitCardNo();
		String creditCardBankName = prp.getCreditCardBankName();
		String creditCardCardType = prp.getCreditCardCardType();
		String creditCardbankCard = prp.getBankCard();
		String expiredTime = prp.getExpiredTime();
		String securityCode = prp.getSecurityCode();
		String ipAddress = prp.getIpAddress();
		
		RegisterReq req = new RegisterReq();
		req.setMerchantId(merchantId);
		req.setTradeNo("kabao" + System.currentTimeMillis());
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setMobile(phone);
		req.setCustomerName(userName);
		req.setIdNo(idCard);
		req.setBankCardNo(debitCardNo);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("进件的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/member/register" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		if ("000000".equals(code)) {
			JSONObject jsonObject = fromObject.getJSONObject("data");

			String userId = jsonObject.getString("userId");

			KBRegister kbRegister = new KBRegister();
			kbRegister.setBankCard(debitCardNo);
			kbRegister.setPhone(phone);
			kbRegister.setIdCard(idCard);
			kbRegister.setMerchantCode(userId);
			kbRegister.setUserName(userName);

			topupPayChannelBusiness.createKBRegister(kbRegister);

			return ResultWrap.init(CommonConstants.SUCCESS, "成功",
					ip + "/v1.0/paymentgateway/topup/tokbbindcard?bankName="
							+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
							+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + creditCardbankCard
							+ "&orderCode=" + orderCode + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&productCode=" + productCode +  "&ipAddress=" + ipAddress);
			
		} else {

			String remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}

	}

	// 申请绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/bindcardapply")
	public @ResponseBody Object KBQuickBindCardApply(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phone = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();

		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);

		BindCardApplyReq req = new BindCardApplyReq();
		req.setMerchantId(merchantId);
		// req.setTradeNo("kabao" + System.currentTimeMillis());
		req.setTradeNo(orderCode);
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setUserId(kbRegisterByIdCard.getMerchantCode());
		req.setMobile(phone);
		req.setBankCardNo(bankCard);
		req.setExpiredDate(this.expiredTimeToMMYY(expiredTime));
		req.setCvv2(securityCode);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("绑卡申请的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/bankcard/bind/apply" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(bankCard);

		if ("000000".equals(code)) {
			String tradeNo = fromObject.getString("tradeNo");

			if (kbBindCardByBankCard == null) {

				KBBindCard kbBindCard = new KBBindCard();
				kbBindCard.setBankCard(bankCard);
				kbBindCard.setIdCard(idCard);
				kbBindCard.setOrderCode(tradeNo);
				kbBindCard.setStatus("0");
				kbBindCard.setPhone(phone);
				kbBindCard.setExpiredTime(expiredTime);
				kbBindCard.setSecurityCode(securityCode);

				topupPayChannelBusiness.createKBBindCard(kbBindCard);

			} else {

				kbBindCardByBankCard.setOrderCode(tradeNo);
				kbBindCardByBankCard.setStatus("0");
				kbBindCardByBankCard.setExpiredTime(expiredTime);
				kbBindCardByBankCard.setSecurityCode(securityCode);

				topupPayChannelBusiness.createKBBindCard(kbBindCardByBankCard);
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "短信发送成功");
		} else {

			String remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/bindcardconfirm")
	public @ResponseBody Object KBQuickBindCardComfirm(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "productCode") String productCode,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String creditCardBankName = prp.getCreditCardBankName();
		String creditCardCardType = prp.getCreditCardCardType();
		String amount = prp.getAmount();

		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);

		BindCardConfirmReq req = new BindCardConfirmReq();
		req.setMerchantId(merchantId);
		req.setTradeNo("kabao" + System.currentTimeMillis());
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setUserId(kbRegisterByIdCard.getMerchantCode());
		req.setApplyTradeNo(orderCode);
		req.setVerCode(smsCode);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("绑卡确认的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/bankcard/bind/confirm" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(bankCard);

		if ("000000".equals(code)) {

			JSONObject data = fromObject.getJSONObject("data");
			
			String status = data.getString("status");
			String remark = data.getString("remark");
			
			if("07".equals(status)) {
				
				kbBindCardByBankCard.setStatus("1");
				
				topupPayChannelBusiness.createKBBindCard(kbBindCardByBankCard);
				
				return ResultWrap.init(CommonConstants.SUCCESS, "成功",
						ip + "/v1.0/paymentgateway/topup/tokbquickpay?bankName="
								+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + bankCard
								+ "&orderCode=" + orderCode+ "&amount=" + amount + "&productCode=" + productCode + "&expiredTime=" + kbBindCardByBankCard.getExpiredTime()
								+ "&securityCode=" + kbBindCardByBankCard.getSecurityCode() + "&ipAddress=" + ip);
			
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, remark);
			}
		} else {

			String remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}

	}

	
	//绑定到账卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/binddebitcard")
	public @ResponseBody Object KBQuickBindDebitCard(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String phone = prp.getDebitPhone();
		String idCard = prp.getIdCard();
		String bankCard = prp.getDebitCardNo();

		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);

		BindDebitCardReq req = new BindDebitCardReq();
		req.setMerchantId(merchantId);
		req.setTradeNo("kabao" + System.currentTimeMillis());
		//req.setTradeNo(orderCode);
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setUserId(kbRegisterByIdCard.getMerchantCode());
		req.setMobile(phone);
		req.setBankCardNo(bankCard);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("绑定借记卡的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/bankcard/bind" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		if ("000000".equals(code)) {
			
			JSONObject data = fromObject.getJSONObject("data");
			
			String status = data.getString("status");
			String remark = data.getString("remark");
			
			if("07".equals(status)) {
				
				kbRegisterByIdCard.setBankCard(bankCard);
				kbRegisterByIdCard.setPhone(phone);
				kbRegisterByIdCard.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
				
				topupPayChannelBusiness.createKBRegister(kbRegisterByIdCard);
				
				return ResultWrap.init(CommonConstants.SUCCESS, remark);
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, remark);
			}
		} else {

			String remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}

	}
	
	
	// 快捷支付申请接口 KB005
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/fastpayapply")
	public @ResponseBody Object kbFastPayApply(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "productCode") String productCode
			) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String debitPhone = prp.getDebitPhone();
		String idCard = prp.getIdCard();
		String debitCard = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String creditCardPhone = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String extraFee = prp.getExtraFee();
		String ipAddress = prp.getIpAddress();
		
		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);
		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(bankCard);
		
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
		
		LOG.info("bigRate======" + bigRate);
		
		FastPayApplyReq req = new FastPayApplyReq();
		req.setMerchantId(merchantId);
		req.setTradeNo(orderCode);
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setUserId(kbRegisterByIdCard.getMerchantCode());
		req.setCreditCardNo(bankCard);
		req.setCreditMobile(creditCardPhone);
		req.setCvv2(kbBindCardByBankCard.getSecurityCode());
		req.setExpiredDate(this.expiredTimeToMMYY(kbBindCardByBankCard.getExpiredTime()));
		req.setDebitCardNo(debitCard);
		req.setDebitMobile(debitPhone);
		req.setProductCode(productCode);
		req.setAmount(amount);
		req.setFeeRate(bigRate);
		req.setFixFee(extraFee);
		req.setBackUrl(ipAddress + "/v1.0/paymentgateway/topup/kbquick/fastpay/notifyurl");

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("快捷支付申请的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/transfer/bk2bk/apply" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		if ("000000".equals(code)) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "短信发送成功!");
		} else {

			String remark = null;
			try {
				remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			} catch (Exception e) {
				LOG.info("查询失败响应码异常======", e);
				
				return ResultWrap.init(CommonConstants.FALIED, "未知异常,请稍后重试!");
			}
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}
		
	}

	
	//快捷支付确认接口 KB005
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/fastpayconfirm")
	public @ResponseBody Object kbFastPayConfirm(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode
			) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		
		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);
		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(bankCard);
		
		FastPayConfirmReq req = new FastPayConfirmReq();
		req.setMerchantId(merchantId);
		req.setTradeNo("kabao" + System.currentTimeMillis());
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		String uuid = UUIDGenerator.getUUID();
		LOG.info("生成的代付订单号=====" + uuid);
		
		this.updatePaymentOrderThirdOrder(prp.getIpAddress(), orderCode, uuid);
		
		req.setOrgOrderNo(uuid);
		req.setApplyTradeNo(orderCode);
		req.setVerCode(smsCode);

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("快捷支付确认的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/transfer/bk2bk/confirm" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		if ("000000".equals(code)) {
			
			JSONObject data = fromObject.getJSONObject("data");
			JSONObject withholdOrder = data.getJSONObject("withholdOrder");
			
			String status = withholdOrder.getString("status");
			
			if("00".equals(status)) {
				
				JSONObject remitOrder = data.getJSONObject("remitOrder");
				String status1 = remitOrder.getString("status");
				
				if("00".equals(status1)) {
					
					RestTemplate restTemplate = new RestTemplate();
					
					url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
					//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code", orderCode);
					requestEntity.add("third_code", "");
					String result = null;
					try {
						result = restTemplate.postForObject(url, requestEntity, String.class);
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
					}

					LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

					LOG.info("订单已代付!");
					
					return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
				}else {
					
					this.addOrderCauseOfFailure(orderCode, "请求代付失败", prp.getIpAddress());
					
					return ResultWrap.init(CommonConstants.FALIED, "请求代付失败");
				}
			}else {
				
				String message = withholdOrder.getString("message");
				
				this.addOrderCauseOfFailure(orderCode, message, prp.getIpAddress());
				
				return ResultWrap.init(CommonConstants.FALIED, message);
			}
		} else {

			String remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}
		
	}
	
	
	//====================================KB001
	//快捷支付申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/accountpayapply")
	public @ResponseBody Object kbAccountPayApply(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "productCode") String productCode
			) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String creditCardPhone = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String ipAddress = prp.getIpAddress();
		
		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);
		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(bankCard);
		
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
		
		LOG.info("bigRate======" + bigRate);
		
		FastPayApplyReq req = new FastPayApplyReq();
		req.setMerchantId(merchantId);
		req.setTradeNo(orderCode);
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setUserId(kbRegisterByIdCard.getMerchantCode());
		req.setCreditCardNo(bankCard);
		req.setCreditMobile(creditCardPhone);
		req.setCvv2(kbBindCardByBankCard.getSecurityCode());
		req.setExpiredDate(this.expiredTimeToMMYY(kbBindCardByBankCard.getExpiredTime()));
		req.setProductCode(productCode);
		req.setAmount(amount);
		req.setFeeRate(bigRate);
		req.setBackUrl(ipAddress + "/v1.0/paymentgateway/topup/kbquick/fastpay/notifyurl");

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("快捷支付申请的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/transfer/bk2account/apply" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		JSONObject fromObject = JSONObject.fromObject(forObject);

		String code = fromObject.getString("code");

		if ("000000".equals(code)) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "短信发送成功!");
		} else {

			String remark = null;
			try {
				remark = topupPayChannelBusiness.getKBErrorDescByErrorCode(code);
			} catch (Exception e) {
				LOG.info("查询失败响应码异常======", e);
				
				return ResultWrap.init(CommonConstants.FALIED, "未知异常,请稍后重试!");
			}
			
			this.addOrderCauseOfFailure(orderCode, remark, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, remark);
		}
		
	}
	
	
	
	
	// 交易查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/orderquery")
	public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		
		OrderQueryReq req = new OrderQueryReq();
		req.setMerchantId(merchantId);
		req.setTradeNo("kabao" + System.currentTimeMillis());
		req.setTradeTime(new Date());
		req.setDeviceId("iphone-000000001");

		req.setOrgOrderNo(orderCode);
		
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonStrReq = objectMapper.writeValueAsString(req);
		LOG.info("交易查询的请求报文================== jsonStrReq:" + jsonStrReq);

		// 接口版本号
		String paramsEncrypt = null;
		// 1 私钥生成的密文
		try {
			paramsEncrypt = new AllinpayUtils().encryptByPrivateKey(jsonStrReq, privateKey);
		} catch (Exception e) {
			LOG.info("encrypt fail! " + e.fillInStackTrace());
			throw e;
		}

		String sign = new AllinpayUtils().sign(jsonStrReq, privateKey);

		String reqUrl = url + "v1/order/query" + "?key=" + key + "&sign=" + URLEncoder.encode(sign, "UTF-8")
				+ "&params=" + URLEncoder.encode(paramsEncrypt, "UTF-8");
		LOG.info("reqUrl:" + reqUrl);

		String forObject = HttpUtils.doGet(reqUrl);

		LOG.info("forObject======" + forObject);

		return null;

	}

	
	
	// 交易异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kbquick/fastpay/notifyurl")
	public void notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String orgOrderNo = request.getParameter("orgOrderNo");
		
		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tokbbindcard")
	public String returnKBBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String orderCode = request.getParameter("orderCode");
		String cardType = request.getParameter("cardType");
		String productCode = request.getParameter("productCode");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("productCode", productCode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "kbbindcard";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tokbquickpay")
	public String returnKBQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String amount = request.getParameter("amount");
		String orderCode = request.getParameter("orderCode");
		String orderDesc = request.getParameter("orderDesc");// 结算卡的卡类型
		String productCode = request.getParameter("productCode");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("amount", amount);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("orderDesc", orderDesc);
		model.addAttribute("productCode", productCode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "kbquickpay";
	}

}
