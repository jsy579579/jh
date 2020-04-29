package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.ght.Base64;
import com.jh.paymentgateway.util.ght.HttpClientUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;
import sun.misc.BASE64Encoder;

@Controller
@EnableAutoConfiguration
public class MHGHTQuickpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(MHGHTQuickpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final String CHAR_ENCODING = "UTF-8";

	private static String merchantNo = "CX2246565";

	private static String RSAPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJ66X7jsl7w3+W6GDK9JPGFNZ0MzUnM9vhtKqbECGGnQXXDPfbOIReFkuXXUWuY8ciA9gCGZRCB0f7axj4s6jj4G5zhsg1Ytx0bSh7iBoGpbf+6y2SjGiYXLOWsGk7VVYcVf6Wvr6oPVNq+OuPixYVtcoG8Ep75PtFfV6aIV29j/AgMBAAECgYAdgRtF7oSN8gGlb7lv47cbhx3IugN7dGCgBhLg2jjbgmW8EHWXJ5+FtAldQ6nZ3iaAo63rkOe++Ki8tuitwqapyIQF5eOPVSPqiD1tHo8VoS5nQRmtazXWFXoX2DxTx5KkE4spY9Dee1XqjLBBScQRuxuP5kvJQ+9xVPAlm7EoAQJBAPYEq3C86wEWfNE9MwOaCUISo/TDht0T1uIn0Xc4QYDk1uQh5HGgS+sz+uSuWhpQf2/yLRKQOCSVAm2qHZNaogECQQClKwrc5R3IuAZl02yRu3qwhXMZ21O4DIp8SxwuDlQZngQVKPmxTPxwPlj8XB1ZjG+BpamXvFR0J4CwdxxdYHr/AkBREKrZRDb/rcRFQjA8IJaqYfAKqB8ZW/8zmEFKUowrB7zTgWOAGXzKrN8gWV8xSpMjdR5q/oCxdTROpH6IXpYBAkBUBj2UeopYXiYDDzegO7wCqSEQ+l7wtpCNArjnRSrLjXOOQDYmWH/jqhQi7cmQkLz5O4m3Q3vDS4VQQIgRiM+JAkAQDLKplWl8mOyIYJ6KkvRijzWqvKtTjAnfw4p4TbQA3901M+SAwzr+jF6Ejcy7XJyPyWRRMFLqYe9wp1gvmgvm";

	private static String AESKey = "0521615671494629a8bcf49c32d5360d";
	
	private static String url = "http://service.gaohuitong.com/PayApi/quickPay";

	private static String url1 = "http://service.gaohuitong.com/PayApi/agentPay";

	static BASE64Encoder ben = new BASE64Encoder();

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/bindcard")
	public @ResponseBody Object ghtquickBindCard(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode, 
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> map = new HashMap<String, String>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();
		String userName = prp.getUserName();
		String phone = prp.getCreditCardPhone();
		
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();

			// this.addOrderCauseOfFailure(orderCode, "查询银行编码出错!",
			// paymentRequestParameter.getIpAddress());
			return ResultWrap.init(CommonConstants.FALIED, "查询银行编码出错!");
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		String orderNo = UUID.randomUUID().toString().replaceAll("-", "");

		map.put("service", "quickPayBind");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/bindcard/notifyurl");
		map.put("version", "V2.0");
		map.put("orderSource", "1");
		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");// 支付通道类型 1为储蓄卡；6为信用
		map.put("orderNo", orderNo);
		map.put("orderAmount", "1");
		map.put("curCode", "CNY");
		map.put("orderTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
		map.put("bankCardNo", AESEncode(AESKey, bankCard));// 银行卡号：内容用AES加密发送
		map.put("idType", "01");// 付款人证件类型 01：身份证02：军官证03：护照04：回乡证05：台胞证06：警官证07：士兵证:
		map.put("userName", AESEncode(AESKey, userName));// 持卡人姓名 持卡人姓名: 内容用AES加密发送
		map.put("idCode", AESEncode(AESKey, this.idCardLastToUppercase(idCard)));// 证件号：内容用AES加密发送
		map.put("phone", AESEncode(AESKey, phone));// 电话：内容用AES加密发送
		map.put("cvv2", AESEncode(AESKey, securityCode));// 内容用AES加密发送； [payChannelType=6，dealType=1|2时 必填]
		map.put("validPeriod", AESEncode(AESKey, this.expiredTimeToYYMM(expiredTime)));// 信用卡有效期
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡预下单的请求报文======" + map);
		LOG.info("绑卡预下单的明文报文======bankCard=" + bankCard + "&userName=" + userName + "&idCard=" + this.idCardLastToUppercase(idCard) + "&phone=" + phone + "&securityCode=" + securityCode + "&expiredTime=" + this.expiredTimeToYYMM(expiredTime));
		String body = HttpClientUtil.post(url, map);
		LOG.info("请求绑卡预下单返回的======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);
		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");
		
		String needSms = "-1";
		if(fromObject.containsKey("needSms")) {
			needSms = fromObject.getString("needSms");
		}

		if ("10000".equals(dealCode) && "0".equals(needSms)) {

			GHTBindCard ghtBindCardByBankCard = topupPayChannelBusiness.getGHTBindCardByBankCard(bankCard);

			if (ghtBindCardByBankCard == null) {

				GHTBindCard ghtBindCard = new GHTBindCard();
				ghtBindCard.setBankCard(bankCard);
				ghtBindCard.setIdCard(idCard);
				ghtBindCard.setPhone(phone);
				ghtBindCard.setOrderCode(orderNo);
				ghtBindCard.setStatus("0");

				topupPayChannelBusiness.createGHTBindCard(ghtBindCard);

			} else {

				ghtBindCardByBankCard.setOrderCode(orderNo);
				ghtBindCardByBankCard.setStatus("0");

				topupPayChannelBusiness.createGHTBindCard(ghtBindCardByBankCard);

			}

			return ResultWrap.init(CommonConstants.SUCCESS, dealMsg, orderNo);
		
		}else if("10000".equals(dealCode) && "3".equals(needSms)) {
			
			GHTBindCard ghtBindCardByBankCard = topupPayChannelBusiness.getGHTBindCardByBankCard(bankCard);

			if (ghtBindCardByBankCard == null) {

				GHTBindCard ghtBindCard = new GHTBindCard();
				ghtBindCard.setBankCard(bankCard);
				ghtBindCard.setIdCard(idCard);
				ghtBindCard.setPhone(phone);
				ghtBindCard.setOrderCode(orderNo);
				ghtBindCard.setStatus("0");

				topupPayChannelBusiness.createGHTBindCard(ghtBindCard);

			} else {

				ghtBindCardByBankCard.setOrderCode(orderNo);
				ghtBindCardByBankCard.setStatus("0");

				topupPayChannelBusiness.createGHTBindCard(ghtBindCardByBankCard);

			}
			
			return ResultWrap.init("666666", "经查询您已成功鉴权绑卡,将要跳转到交易支付页面!", ip + "/v1.0/paymentgateway/topup/tomhghtquick/pay?bankName="
					+ URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&bankCard=" + prp.getBankCard() + "&ordercode=" + orderNo
				    + "&nature=" + URLEncoder.encode(prp.getCreditCardCardType(), "UTF-8") + "&phone=" + prp.getCreditCardPhone() + "&amount=" + prp.getAmount()
					+ "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&ips=" + prp.getIpAddress());
		} else {

			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/bindcardconfirm")
	public @ResponseBody Object ghtquickBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "orderNo") String orderNo, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode)
			throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
		
		Map<String, String> map = new HashMap<String, String>();

		map.put("service", "quickPayBindConfirm");
		map.put("orderSource", "1");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/bindcard/notifyurl");
		map.put("version", "V2.0");
		map.put("orderNo", orderNo);
		map.put("smsCode", smsCode);// 收到的短信验证码
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡确认的请求报文======" + map);
		String body = HttpClientUtil.post(url, map);
		LOG.info("绑卡确认请求返回的 sendPost======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/topup/tomhghtquick/pay?bankName="
					+ URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&bankCard=" + prp.getBankCard() + "&ordercode=" + orderNo
				    + "&nature=" + URLEncoder.encode(prp.getCreditCardCardType(), "UTF-8") + "&phone=" + prp.getCreditCardPhone() + "&amount=" + prp.getAmount()
					+ "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&ips=" + prp.getIpAddress());
		} else {

			this.addOrderCauseOfFailure(orderNo, dealMsg, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 快捷支付预下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/preorder")
	public @ResponseBody Object ghtquickPreOrder(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "storeNo") String storeNo,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		List<String> list = new ArrayList<String>();
		Random random = new Random();
		
		LOG.info("初始订单号orderCode======" + orderCode);
		
		orderCode = this.whetherRepeatOrder(orderCode);
		
		LOG.info("确认订单号orderCode======" + orderCode);
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String realAmount = prp.getAmount();
		String bankName = prp.getCreditCardBankName();
		
		LOG.info("storeNo======" + storeNo);
		String merchantCode = null;
		if(storeNo != null && !"".equals(storeNo)) {
			
			merchantCode = storeNo.substring(storeNo.indexOf("(") + 1, storeNo.indexOf(")"));
			
		}else {
			
			if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("华夏")
					|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
					|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
					|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
					|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
					|| bankName.contains("汇丰") || bankName.contains("工商")) {
				
				list.add("MD0638802"); //上海银杉小吃屋
				list.add("MD0638814"); //上海伍吃美食店
				list.add("MD0645022"); //上海弘一花店
				list.add("MD0644980"); //上海市徐汇区家清百货商店
				
				merchantCode = list.get(random.nextInt(list.size()));
				
			}else {
				
				list.add("MD0663583");
				list.add("MD0663595");
				list.add("MD0669803");
				list.add("MD0669761");
				
				merchantCode = list.get(random.nextInt(list.size()));
				
			}
			
		}
		
		LOG.info("merchantCode======" + merchantCode);
		
		if(bankName.contains("光大")) {
			String amount = prp.getAmount();
			
			if(new BigDecimal(amount).compareTo(new BigDecimal("5000"))>0) {
				
				this.addOrderCauseOfFailure(orderCode, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!", prp.getIpAddress());
				
				return ResultWrap.init(CommonConstants.FALIED, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!");
				
			}
			
		}

		RestTemplate rt = new RestTemplate();
		String url1 = prp.getIpAddress() + "/v1.0/notice/sms/vericode?phone=" + prp.getCreditCardPhone();
		String resultStr = rt.getForObject(url1, String.class);
		JSONObject jsonObject = JSONObject.fromObject(resultStr);
		String code = jsonObject.getString("result");
		LOG.info("发送码：" + smsCode + "===校验码：" + code);
		
		if(!smsCode.equals(code)) {

			//this.addOrderCauseOfFailure(orderCode, "验证码输入不正确,请仔细核对重新输入!", prp.getIpAddress());
			
			return ResultWrap.init("666666", "验证码输入不正确,请仔细核对重新输入!");
		}
		
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询银行编码出错!");
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		// 将金额转换为以分为单位:
		String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		map.put("service", "quickPayApply");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/fastpay/notifyurl");
		map.put("version", "V2.0");
		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");// 支付通道类型 1为储蓄卡；6为信用
		map.put("orderNo", orderCode);
		map.put("orderAmount", bigRealAmount);
		map.put("curCode", "CNY");
		map.put("orderTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
		map.put("bankCardNo", AESEncode(AESKey, prp.getBankCard()));// 银行卡号：内容用AES加密发送
		map.put("idType", "01");// 付款人证件类型 01：身份证02：军官证03：护照04：回乡证05：台胞证06：警官证07：士兵证:
		map.put("userName", AESEncode(AESKey, prp.getUserName()));// 持卡人姓名 持卡人姓名: 内容用AES加密发送
		map.put("idCode", AESEncode(AESKey, this.idCardLastToUppercase(prp.getIdCard())));// 证件号：内容用AES加密发送
		map.put("phone", AESEncode(AESKey, prp.getCreditCardPhone()));// 电话：内容用AES加密发送
		map.put("orderSource", "1");
		map.put("cvv2", AESEncode(AESKey, securityCode));// 内容用AES加密发送； [payChannelType=6，dealType=1|2时 必填]
		LOG.info("有效期======" + this.expiredTimeToYYMM(expiredTime));
		map.put("validPeriod", AESEncode(AESKey, this.expiredTimeToYYMM(expiredTime)));// 信用卡有效期
		map.put("storeNo", merchantCode);
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("快捷支付预下单的请求报文======" + map);
		LOG.info("快捷支付预下单的明文报文======bankCard=" + prp.getBankCard() + "&userName=" + prp.getUserName() + "&idCard=" + this.idCardLastToUppercase(prp.getIdCard()) + "&phone=" + prp.getCreditCardPhone() + "&securityCode=" + securityCode + "&expiredTime=" + this.expiredTimeToYYMM(expiredTime));
		String body = HttpClientUtil.post(url, map);
		LOG.info("请求快捷支付预下单返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		//String dealCode = "10000";
		//String dealMsg = "lll";
		
		if ("10000".equals(dealCode)) {

			map = (Map<String, String>) ghtquickFastPay(orderCode);

			return map;

		} else {

			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	
	// 快捷确认支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/fastpay")
	public @ResponseBody Object ghtquickFastPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("service", "quickPayConfirm");
		map.put("orderSource", "1");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/fastpay/notifyurl");
		map.put("version", "V2.0");
		map.put("orderNo", orderCode);// 快捷预下单时的订单
		// map.put("smsCode", "");// 收到的短信验证码
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("快捷支付确认支付的请求报文======" + map);
		String body = HttpClientUtil.post(url, map);
		LOG.info("请求快捷支付确认支付返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");
		
		//String dealCode = "10000";
		//String dealMsg = "lll";
		
		if ("10000".equals(dealCode)) {

			RestTemplate rt = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/remark";
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("ordercode", orderCode);
			multiValueMap.add("remark", "支付成功,等待银行扣款");
			String result = rt.postForObject(url, multiValueMap, String.class);
			
			//return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
			return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", ip + "/v1.0/paymentgateway/topup/topaysuccess?orderCode=" + orderCode + "&bankName=" + URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&bankCard=" + prp.getBankCard() + "&amount=" + prp.getAmount() + "&realAmount=" + prp.getRealAmount());
		} else {

			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "extra") String extra) throws Exception {
		LOG.info("订单号为: " + orderCode + "   开始进入代付接口======");
		
		Map<String, String> map = new HashMap<String, String>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(extra);
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();
		String userName = prp.getUserName();

		String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();

			this.addOrderCauseOfFailure(extra, "暂不支持该到账银行卡!", prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, "查询银行编码出错!");
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		map.put("service", "payForSameName");
		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);
		map.put("orderNoList", extra);// 快捷支付成功的订单号集合，可以填多个订单号，以英文逗号间隔
		map.put("version", "V2.0");
		map.put("accountProp", "1");// 1私人2公司:
		map.put("accountNo", ben.encode(bankCard.getBytes()));// baase64加密：银行账号
		map.put("accountName", ben.encode(userName.getBytes()));// baase64加密：账户名称
		map.put("bankGenneralName", bankName + "宝山支行");// 银行通用名称
		map.put("bankName", bankName);// 开户行名称:
		map.put("bankCode", bankCode);// 开户行号such as CBC:
		map.put("currency", "CNY");
		map.put("bankProvcince", "上海");// bankProvcince开户行所在省
		map.put("bankCity", "上海");// bankCity开户行所在市:
		map.put("orderAmount", bigRealAmount);
		map.put("orderTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/transfer/notifyurl");
		map.put("orderSource", "1");

		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代付的请求报文======" + map);
		LOG.info("代付的明文报文======bankCard=" + bankCard + "&userName=" + userName);
		String body = HttpClientUtil.post(url1, map);
		LOG.info("请求代付返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			RestTemplate rt = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/remark";
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("ordercode", extra);
			multiValueMap.add("remark", "代付成功,等待银行出款");
			String result = rt.postForObject(url, multiValueMap, String.class);
			
			return ResultWrap.init("999998", "代付成功,等待银行出款!");
		} else {

			this.addOrderCauseOfFailure(extra, dealMsg, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}


	// 手动代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/transferbyhand")
	public @ResponseBody Object transferByHand(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "ipAddress", required = false, defaultValue = "http://106.15.47.73") String ipAddress
			) throws Exception {
		LOG.info("订单号为: " + orderCode + "   开始进入代付接口======");

		Map<String, String> map = new HashMap<String, String>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		RestTemplate restTemplate = new RestTemplate();
		String url = ipAddress + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		
		JSONObject jsonObject = JSONObject.fromObject(result);
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		
		if(CommonConstants.SUCCESS.equals(respCode)) {
			JSONObject json = jsonObject.getJSONObject(CommonConstants.RESULT);
			LOG.info("json======" + json);
			
			String realAmount = json.getString("realAmount");
			String userId = json.getString("userid");
			
			String uuid = UUIDGenerator.getUUID();
			LOG.info("生成的代付订单号=====" + uuid);
			
			url = ipAddress + "/v1.0/transactionclear/payment/update/thirdordercode";
			multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("order_code", orderCode);
			multiValueMap.add("third_code", uuid);
			result = restTemplate.postForObject(url, multiValueMap, String.class);
			LOG.info("接口/v1.0/transactionclear/payment/update/thirdordercode====RESULT=========" + result);
			
			url = ipAddress + "/v1.0/user/bank/default/userid";
			multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("user_id", userId);
			result = restTemplate.postForObject(url, multiValueMap, String.class);
			LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);
			
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			if(CommonConstants.SUCCESS.equals(respCode)) {
				JSONObject resultObj = jsonObject.getJSONObject(CommonConstants.RESULT);
				String bankCard = resultObj.getString("cardNo");
				String bankName = resultObj.getString("bankName");
				String userName = resultObj.getString("userName");
				
				String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

				String bankCode;
				try {
					BankNumCode bankNumCode = topupPayChannelBusiness
							.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

					bankCode = bankNumCode.getBankCode();
				} catch (Exception e) {
					e.printStackTrace();

					return ResultWrap.init(CommonConstants.FALIED, "查询银行编码出错!");
				}

				if (bankName.contains("广发") || bankName.contains("广东发展")) {

					bankCode = "GDB";
				}

				map.put("service", "payForSameName");
				map.put("merchantNo", merchantNo);
				map.put("orderNo", uuid);
				map.put("orderNoList", orderCode);// 快捷支付成功的订单号集合，可以填多个订单号，以英文逗号间隔
				map.put("version", "V2.0");
				map.put("accountProp", "1");// 1私人2公司:
				map.put("accountNo", ben.encode(bankCard.getBytes()));// baase64加密：银行账号
				map.put("accountName", ben.encode(userName.getBytes()));// baase64加密：账户名称
				map.put("bankGenneralName", bankName + "宝山支行");// 银行通用名称
				map.put("bankName", bankName);// 开户行名称:
				map.put("bankCode", bankCode);// 开户行号such as CBC:
				map.put("currency", "CNY");
				map.put("bankProvcince", "上海");// bankProvcince开户行所在省
				map.put("bankCity", "上海");// bankCity开户行所在市:
				map.put("orderAmount", bigRealAmount);
				map.put("orderTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
				map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/mhghtquick/transfer/notifyurl");
				map.put("orderSource", "1");

				map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

				LOG.info("代付的请求报文======" + map);
				String body = HttpClientUtil.post(url1, map);
				LOG.info("请求代付返回的body======" + body);

				JSONObject fromObject = JSONObject.fromObject(body);

				String dealCode = fromObject.getString("dealCode");
				String dealMsg = fromObject.getString("dealMsg");
				
				if ("10000".equals(dealCode)) {

					RestTemplate rt = new RestTemplate();

					url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/remark";
					multiValueMap = new LinkedMultiValueMap<String, String>();
					multiValueMap.add("ordercode", orderCode);
					multiValueMap.add("remark", "代付成功,等待银行出款");
					multiValueMap.add("createTime", "1");
					result = rt.postForObject(url, multiValueMap, String.class);

					LOG.info("result======" + result);
					
					JSONObject fromObject1 = JSONObject.fromObject(result);
					respCode = fromObject1.getString(CommonConstants.RESP_CODE);
					
					if(CommonConstants.SUCCESS.equals(respCode)) {
						
						return ResultWrap.init("999998", "代付成功,等待银行出款!");
					}else {
						
						return ResultWrap.init(CommonConstants.FALIED, fromObject1.getString(CommonConstants.RESP_MESSAGE));
					}
				} else {

					this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());

					return ResultWrap.init(CommonConstants.FALIED, dealMsg);
				}
				
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, "查询默认到账卡信息有误!");
			}
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "查询订单信息有误!");
		}
		
	}
	
	
	
	// 代付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/transfer/orderquery")
	public @ResponseBody Object ghtquickTransferOrderQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		map.put("service", "payForAnotherOneSearch");
		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);
		map.put("version", "V2.0");
		map.put("orderSource", "1");
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代付订单查询的请求报文======" + map);
		String body = HttpClientUtil.post(url1, map);
		LOG.info("请求代付订单查询返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			return ResultWrap.init(CommonConstants.SUCCESS, dealMsg);
		} else if ("10001".equals(dealCode)) {

			return ResultWrap.init("999998", dealMsg);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 绑卡异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/bindcard/notifyurl")
	public void ghtquickBindCardNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("绑卡异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String orderNo = request.getParameter("orderNo");
		String dealCode = request.getParameter("dealCode");

		if ("10000".equals(dealCode)) {

			GHTBindCard ghtBindCardByOrderCode = topupPayChannelBusiness.getGHTBindCardByOrderCode(orderNo);

			ghtBindCardByOrderCode.setStatus("1");

			topupPayChannelBusiness.createGHTBindCard(ghtBindCardByOrderCode);

			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();

		}

	}

	// 快捷支付的异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/fastpay/notifyurl")
	public void tradeBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String orderNo = request.getParameter("orderNo");
		//String cxOrderNo = request.getParameter("cxOrderNo");
		String dealCode = request.getParameter("dealCode");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
		if ("10000".equalsIgnoreCase(dealCode)) {
			LOG.info("快捷支付交易成功======");

			String uuid = UUIDGenerator.getUUID();
			LOG.info("生成的代付订单号=====" + uuid);
			
			LOG.info("开始发起代付======");
			try {
				
				updatePaymentOrderThirdOrder(prp.getIpAddress(), orderNo, uuid);
				
				transfer(uuid, orderNo);
			} catch (Exception e) {
				LOG.error("请求代付异常======" + e);
				
				addOrderCauseOfFailure(orderNo, "请求代付异常!", prp.getIpAddress());
			}
			
		}else {
			
			String dealMsg = request.getParameter("dealMsg");
			
			this.addOrderCauseOfFailure(orderNo, dealMsg, prp.getIpAddress());
			
		}

		Map<String, String> map = new HashMap<String, String>();

		map.put("merchantNo", merchantNo);
		map.put("dealResult", "SUCCESS");
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		JSONObject fromObject = JSONObject.fromObject(map);

		PrintWriter pw = response.getWriter();
		pw.print(fromObject.toString());
		pw.close();

	}

	// 代付的异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtquick/transfer/notifyurl")
	public void topupBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("代付异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String orderNo = request.getParameter("orderNo");
		String dealCode = request.getParameter("dealCode");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);

		if ("10000".equals(dealCode)) {
			LOG.info("代付交易成功======");

			RestTemplate restTemplate = new RestTemplate();

			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			requestEntity.add("version", "6");
			String result = null;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + orderNo + "====================" + result);

			LOG.info("订单已代付!");

		}else {
			
			/*String dealMsg = request.getParameter("dealMsg");
			
			this.addOrderCauseOfFailure(orderNo, dealMsg, prp.getIpAddress());*/
			
		}

		Map<String, String> map = new HashMap<String, String>();

		map.put("merchantNo", merchantNo);
		map.put("dealResult", "SUCCESS");
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		JSONObject fromObject = JSONObject.fromObject(map);

		PrintWriter pw = response.getWriter();
		pw.print(fromObject.toString());
		pw.close();
	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tomhghtquick/bindcard")
	public String returnGHTBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String orderCode = request.getParameter("orderCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("ipAddress", ipAddress);

		return "mhghtquickbindcard";
	}


	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tomhghtquick/pay")
	public String returnGHTQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ordercode = request.getParameter("ordercode");
		String nature = request.getParameter("nature");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");
		String amount = request.getParameter("amount");
		String ipAddress = request.getParameter("ipAddress");
		String ips = request.getParameter("ips");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("nature", nature);
		model.addAttribute("orderCode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);
		model.addAttribute("amount", amount);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("ips", ips);

		return "mhghtquickpay";
	}
	
	
	/*@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/topaysuccess")
	public String returnPaySuccess(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String orderCode = request.getParameter("orderCode");
		String amount = request.getParameter("amount");
		String realAmount = request.getParameter("realAmount");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("amount", amount);
		model.addAttribute("realAmount", realAmount);

		return "paysuccess";
	}*/
	
	
	// =================================================

	public String AESEncode(String encodeRules, String content) {
		try {
			// 1.构造密钥生成器，指定为AES算法,不区分大小写
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(encodeRules.getBytes());
			// 2.根据ecnodeRules规则初始化密钥生成器
			// 生成一个128位的随机源,根据传入的字节数组
			keygen.init(128, random);
			// 3.产生原始对称密钥
			SecretKey original_key = keygen.generateKey();
			// 4.获得原始对称密钥的字节数组
			byte[] raw = original_key.getEncoded();
			// 5.根据字节数组生成AES密钥
			SecretKey key = new SecretKeySpec(raw, "AES");
			// 6.根据指定算法AES自成密码器
			Cipher cipher = Cipher.getInstance("AES");
			// 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.ENCRYPT_MODE, key);
			// 8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
			byte[] byte_encode = content.getBytes("utf-8");
			// 9.根据密码器的初始化方式--加密：将数据加密
			byte[] byte_AES = cipher.doFinal(byte_encode);
			// 10.将加密后的数据转换为字符串
			// 这里用Base64Encoder中会找不到包
			// 解决办法：
			// 在项目的Build path中先移除JRE System Library，再添加库JRE System
			// Library，重新编译后就一切正常了。
			String AES_encode = new String(new BASE64Encoder().encode(byte_AES));
			// 11.将字符串返回
			return AES_encode;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 如果有错就返加nulll
		return null;
	}

	public static String createLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (!"data".equals(key) && !"sign".equals(key) && notEmpty(params.get(key))) {
				sb.append(key).append("=").append(value).append("&");
			}
		}
		// 拼接时，不包括最后一个&字符
		sb.deleteCharAt(sb.length() - 1);
		// System.out.println("签串:"+sb.toString());
		return sb.toString();
	}

	public static boolean notEmpty(String s) {
		return s != null && !"".equals(s) && !"null".equals(s);
	}

	public static String generateSign(String key, String content) {
		String sign = "";
		if (key.length() > 32) {
			try {
				System.out.println("rsa签串:" + content);
				sign = rsasign(content, key);
				// System.out.println("encode前:"+sign);
				sign = URLEncoder.encode(sign, "UTF-8");
				// System.out.println("encode后:"+sign);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			sign = md5UpperCase(content + key, "UTF-8");// 商户签名数据
			System.out.println("md5签串:" + content + key);
		}
		// System.out.println(sign);
		return sign;
	}

	public static String rsasign(String content, String privateKey) {
		String charset = CHAR_ENCODING;
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey.getBytes()));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			Signature signature = Signature.getInstance("SHA1WithRSA");

			signature.initSign(priKey);
			signature.update(content.getBytes(charset));

			byte[] signed = signature.sign();

			return new String(Base64.encodeBase64(signed));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String md5UpperCase(String inStr, String charset) {
		return md5(inStr, charset).toUpperCase();
	}

	public static String md5(String inStr, String charset) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		byte[] md5Bytes;
		StringBuffer hexValue = new StringBuffer();
		try {
			md5Bytes = md5.digest(inStr.getBytes(charset));
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hexValue.toString();
	}

}
