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
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.pojo.GHTCityMerchant;
import com.jh.paymentgateway.pojo.GHTXwkCityMerchant;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.ght.Base64;
import com.jh.paymentgateway.util.ght.HttpClientUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;
import sun.misc.BASE64Encoder;

@Controller
@EnableAutoConfiguration
public class GHTDHpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(GHTDHpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final String CHAR_ENCODING = "UTF-8";

	private static String merchantNo = "CX0460545";

	/*private static String RSAPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK/NsyEa4+4xgFRfwLr5XKuSJCxpSKItm0Rj120cMGa3HMIoZyxxgVRVbjBu6spn2BwHaDPFWE+7HSjxNRJzxvzTjowrBiNDajdTc8lq9++cH2Kgg7XxXvrCFFiSYhYzIyjtlVVhKt9zfffBdPhCych0H6NNAXEnMh1S2HaY9aYDAgMBAAECgYEAgnKaqUTfvh3DAFhwlQx1A+YTq1SQDa7TmEKRVifljkHvN+5s6CBs+5vMgGm9FGVLiTjBzDBx3++Sqo+YdtGr4y/rOUFImwKDGUw8wDoiUKo/PcjbqvddWfda6epkyBi1av91OIeI12QunuAKtl+r8ccHeyLkpW63GToRH/FQNeECQQDp/GfzplQ58eshM8ywl8Q1WRHCevxJf5BFztWyfXEvxk2urOPbtOxVfvmeamIEu0+JVMY0LP6TnhHv3G3/pSzzAkEAwFf1f3BLjdNZzuE+BxGwj2khqVCEHKUTIAe+QpSIzzd1xVOi3C8EqxtbUqjYyCDQ8/eCfYWvmVGj6j6sx/SmsQJAHaE73KvEYK4U01iG5Bnj28mSqSj/x6LityRRBVBDRAR9k9AK7qiDgPQaUggwr1603LQigTrtZe4PYWttdoEJJwJAXD/TdrOdCQB86kQNQ8awL5f0lQgQ1Mw3R/1uuvASTZAD5Agg7AmA66/LNPHWRuW2Ucw7wYgEmDwXqZk1tREZgQJAAwHTwKL15GnsbwUaRUKh1fSieqYYZ96s+GxxzsiPki7eLh4vwEQn/rtSa1d3A81q4ku9Ok0Ok2rYNcXolsI3Rg==";

	private static String AESKey = "86830258f3c14e7aa3ed688e1372b422";*/

	private static String RSAPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJmIP9DiKx8RS1tJHKQo6ewBg/qUDl/CXeIwxJBdAp4AFrpiwhJE6897MohbnbcVv+/LIsSDVS7mbnRhNF/nYMrsNPdabQMEzHBh2juZ9YqATXWBaHHupzRKVEtbWI8n4TgfgF5eWETVCyzjVGPLtniS5ASyF0RtHjcP470w2LotAgMBAAECgYBPoeO3rBdBne7AAkPgwXfS+VZC+dgawu1/DFxnpFZfLRycv+x5HVZR30xufVZoR9etwGbgFl5wXQSdSG2p8JC64XRnWpovgXSb7WT4r2hXCj/7mfW1i81TMKED8lKpb1zt7TpUivD2CA4f8SNOPqZvd4XiguB0fb3OqgGUKxWmAQJBANRVszBhNYG0zgMRueIvDosP8w3KnUWdFbVG70ooQsvL+q8NWF/aAckvfYur2nNKg82v3SfYJGlawnvqhcA7r50CQQC5Gug7JTZRmLVVcBMv9x4CcThFGBSiRRx9KWbEM0hOx7gms7fPk+JWmLACvgza8nJ3UjQgXa0h7mFMslESMFfRAkAcAlYJyrjpLDWEuCDiEj0D4Q25wwFt/mOvrvS/voHZKkYeM092DWbw5//Snc/KJ4ktZ8ZvjBZ5g0xmVjBghD8NAkEAlNKIUj057eKcMEP6eT7ydr42on6Y4Fo2bH7j6+zbPtOeQCeQKilY+YyrPpk0VrhExaBm3nrSXwx8WCCpw7jwcQJAT1tYOEFaig3KHSw2sd0S0AbSehzp2cK3DJE16GUQlKTDnJ8lRkg3BUE1+whP5X5zYAuWpLDw8f81gmAY2Gi5eg==";

	private static String AESKey = "aadd37fc60c0489490c54c0dd0cc27d2";
	
	private static String url = "http://service.gaohuitong.com/PayApi/quickPay";

	private static String url1 = "http://service.gaohuitong.com/PayApi/agentPay";

	static BASE64Encoder ben = new BASE64Encoder();

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/torepayment")
	public @ResponseBody Object ghtdhToRepayment(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {

		GHTBindCard ghtBindCardByBankCard = topupPayChannelBusiness.getGHTBindCardByBankCard(bankCard);

		if (ghtBindCardByBankCard == null) {

			return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
					ip + "/v1.0/paymentgateway/topup/toght/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode("信用卡", "UTF-8") + "&bankCard=" + bankCard + "&phone="
							+ phone + "&userName=" + URLEncoder.encode(userName, "UTF-8") + "&idCard=" + idCard
							+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);
		} else {
			if (!"1".equals(ghtBindCardByBankCard.getStatus())) {

				return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
						ip + "/v1.0/paymentgateway/topup/toght/bindcard?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode("信用卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
								+ "&userName=" + URLEncoder.encode(userName, "UTF-8") + "&idCard=" + idCard
								+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "已完成鉴权验证!");
			}
		}

	}

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/bindcard")
	public @ResponseBody Object ghtdhBindCard(HttpServletRequest request,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> map = new HashMap<String, String>();

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
			LOG.info("bankCode==="+bankCode);
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
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/bindcard/notifyurl");
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
																						// 内容用AES加密发送:[payChannelType=6，dealType=1|2
																						// 必填(MMyy)]
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡预下单的请求报文======" + map + " &&&&&bankCard======" + bankCard);
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
			
			return ResultWrap.init(CommonConstants.FALIED, "您已成功绑卡鉴权,可以进行还款交易!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/bindcardconfirm")
	public @ResponseBody Object ghtdhBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "orderNo") String orderNo, @RequestParam(value = "smsCode") String smsCode)
			throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		Map<String, String> map = new HashMap<String, String>();

		map.put("service", "quickPayBindConfirm");
		map.put("orderSource", "1");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/bindcard/notifyurl");
		map.put("version", "V2.0");
		map.put("orderNo", orderNo);// 快捷预下单时的订单
		map.put("smsCode", smsCode);// 收到的短信验证码
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡确认的请求报文======" + map);
		String body = HttpClientUtil.post(url, map);
		LOG.info("绑卡确认请求返回的 sendPost======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			return ResultWrap.init(CommonConstants.SUCCESS, dealCode,
					ip + "/v1.0/paymentgateway/topup/toght/bindcardsuccesspage");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 快捷支付预下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/preorder")
	public @ResponseBody Object ghtdhPreOrder(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "storeNo") String storeNo) throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		if (securityCode == null && "".equals(securityCode)) {

			return ResultWrap.init(CommonConstants.FALIED, "安全码为空,需要完善信用卡信息!");
		}

		if (expiredTime == null && "".equals(expiredTime)) {

			return ResultWrap.init(CommonConstants.FALIED, "有效期为空,需要完善信用卡信息!");
		}

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String realAmount = prp.getRealAmount();
		String bankName = prp.getCreditCardBankName();

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
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/fastpay/notifyurl");
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
		map.put("storeNo", storeNo);
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("快捷支付预下单的请求报文======" + map + " &&&&&bankCard======" + prp.getBankCard());
		LOG.info("快捷支付预下单的明文报文======bankCard=" + prp.getBankCard() + "&userName=" + prp.getUserName() + "&idCard=" + this.idCardLastToUppercase(prp.getIdCard()) + "&phone=" + prp.getCreditCardPhone() + "&securityCode=" + securityCode + "&expiredTime=" + this.expiredTimeToYYMM(expiredTime));
		String body = HttpClientUtil.post(url, map);
		LOG.info("请求快捷支付预下单返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			map = (Map<String, String>) ghtdhFastPay(orderCode);

			return map;

		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/fastpay")
	public @ResponseBody Object ghtdhFastPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		map.put("service", "quickPayConfirm");
		map.put("orderSource", "1");
		map.put("merchantNo", merchantNo);
		map.put("bgUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/fastpay/notifyurl");
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

		if ("10000".equals(dealCode)) {

			return ResultWrap.init("999998", "等待银行扣款中!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "extra") String extra
			) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String userName = prp.getUserName();

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
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/transfer/notifyurl");
		map.put("orderSource", "1");

		// map.put("cause", "cause");// cause打款原因:
		// map.put("tel", "");
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代付的请求报文======" + map + "   &&&&&&  bankcard=====" + bankCard);
		LOG.info("代付的明文报文======bankCard=" + bankCard + "&userName=" + userName);
		String body = HttpClientUtil.post(url1, map);
		LOG.info("请求代付返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			return ResultWrap.init("999998", "等待银行还款中!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	
	// 手动代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/transferbymanual")
	public @ResponseBody Object transferByManual(@RequestParam(value = "extra") String extra,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "realAmount") String realAmount, 
			@RequestParam(value = "ipAddress", required = false, defaultValue = "http://106.15.47.73") String ipAddress
			)throws Exception {

		String uuid = UUIDGenerator.getUUID();
		LOG.info("生成的代付订单号=====" + uuid);
		
		RestTemplate restTemplate = new RestTemplate();
		String url = ipAddress + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		long userId;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			userId = resultObj.getLong("id");
		} catch (Exception e) {
			LOG.error("根据手机号查询用户信息失败=============================", e);

			return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
		}
		
		url = ipAddress + "/v1.0/user/bank/default/userid";
		requestEntity.add("user_id", userId + "");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);
		
		jsonObject = JSONObject.fromObject(result);
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if(CommonConstants.SUCCESS.equals(respCode)) {
			resultObj = jsonObject.getJSONObject(CommonConstants.RESULT);
			String bankCard = resultObj.getString("cardNo");
			String bankName = resultObj.getString("bankName");
			String userName = resultObj.getString("userName");
		
			Map<String, String> map = new HashMap<String, String>();

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
			map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/ghtdh/transfer/notifyurl");
			map.put("orderSource", "1");

			map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

			LOG.info("代付的请求报文======" + map + "   &&&&&&  bankcard=====" + bankCard);
			LOG.info("代付的明文报文======bankCard=" + bankCard + "&userName=" + userName);
			String body = HttpClientUtil.post(url1, map);
			LOG.info("请求代付返回的body======" + body);

			JSONObject fromObject = JSONObject.fromObject(body);

			String dealCode = fromObject.getString("dealCode");
			String dealMsg = fromObject.getString("dealMsg");

			if ("10000".equals(dealCode)) {

				return ResultWrap.init("999998", "请求代付成功,等待银行打款!", uuid);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, dealMsg, uuid);
			}
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, jsonObject.getString(CommonConstants.RESP_MESSAGE));
		}

	}

	// 支付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/fastpay/orderquery")
	public @ResponseBody Object ghtdhFastPayOrderQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		map.put("service", "paySearchOrder");
		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);
		map.put("version", "V2.0");
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("支付订单查询的请求报文======" + map);
		String body = HttpClientUtil.post("http://service.gaohuitong.com/PayApi/paySearchOrder", map);
		LOG.info("请求支付订单查询返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String dealCode = fromObject.getString("dealCode");
		String dealMsg = fromObject.getString("dealMsg");

		if ("10000".equals(dealCode)) {
			String orderStatus = fromObject.getString("orderStatus");
			if("1".equals(orderStatus)) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "查询支付成功");
			}else if("2".equals(orderStatus) || "6".equals(orderStatus)) {
				
				return ResultWrap.init(CommonConstants.FALIED, "查询支付失败");
			}else {
				
				return ResultWrap.init("999998", "查询支付处理中");
			}
		} else if ("10001".equals(dealCode)) {

			return ResultWrap.init("999998", dealMsg);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, dealMsg);
		}

	}

	// 代付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/transfer/orderquery")
	public @ResponseBody Object ghtdhTransferOrderQuery(@RequestParam(value = "orderCode") String orderCode)
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

	// 余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/balancequery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) throws Exception {

		return null;

	}

	// 绑卡异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/bindcard/notifyurl")
	public void ghtdhBindCardNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/fastpay/notifyurl")
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
		String cxOrderNo = request.getParameter("cxOrderNo");
		String dealCode = request.getParameter("dealCode");
		String dealMsg = request.getParameter("dealMsg");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
		if ("10000".equalsIgnoreCase(dealCode)) {
			LOG.info("快捷支付交易成功======");

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
			requestEntity.add("third_code", cxOrderNo);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + orderNo + "====================" + result);

			LOG.info("订单已支付!");

		}else {
			
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/transfer/notifyurl")
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/toght/bindcard")
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
		String idCard = request.getParameter("idCard");
		String phone = request.getParameter("phone");
		String userName = request.getParameter("userName");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("idCard", idCard);
		model.addAttribute("phone", phone);
		model.addAttribute("userName", userName);
		model.addAttribute("ipAddress", ipAddress);

		return "ghtbindcard";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/toght/bindcardsuccesspage")
	public String returnGHTBindCardSuccessPage(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		return "bqdhbindcardsuccess";
	}

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

	
	
/*	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtdh/qwer")
	public @ResponseBody Object qwer(@RequestParam(value = "merchantCode") String merchantCode,
			@RequestParam(value = "merchantName") String merchantName
			) throws Exception {

		GHTXwkCityMerchant ghtXwkCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTXwkCityMerchantByMerchantCode(merchantCode);
		
		GHTXwkCityMerchant ghtXwkCityMerchantByMerchantName = topupPayChannelBusiness.getGHTXwkCityMerchantByMerchantName(merchantName);
		
		GHTCityMerchant ghtCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTCityMerchantByMerchantCode(merchantCode);
		
		GHTCityMerchant ghtCityMerchantByMerchantName = topupPayChannelBusiness.getGHTCityMerchantByMerchantName(merchantName);
		
		LOG.info("ghtXwkCityMerchantByMerchantCode======" + ghtXwkCityMerchantByMerchantCode);
		LOG.info("ghtXwkCityMerchantByMerchantName======" + ghtXwkCityMerchantByMerchantName);
		LOG.info("ghtCityMerchantByMerchantCode======" + ghtCityMerchantByMerchantCode);
		LOG.info("ghtCityMerchantByMerchantName======" + ghtCityMerchantByMerchantName);
		
		return null;

	}*/
	
}
