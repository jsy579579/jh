package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.ght.Base64;
import com.jh.paymentgateway.util.ght.HttpClientUtil;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;
import sun.misc.BASE64Encoder;

@Controller
@EnableAutoConfiguration
public class KKDHpageRequest extends BaseChannel {
	public static final Logger LOG = LoggerFactory.getLogger(KKDHpageRequest.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	private static String merchantNo = "10000103";
	private static String RSAPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIYsjWlBZdyF1CdoPJq5XtjtM1XCx47E3deoqSH42aciC4w1AuqDt/qKhI6TdkQ4GjokJW0dSnb1/urPzI0V1Uu045fqmo9Yl5IgWXmJZsCZnbnh0mwLjX7xyvXSEgtKqpMZH45W0GnGiszTyd8W27rYH1zlt0NLOFQtRHBdcz1dAgMBAAECgYBGtKOwm+35z+yE98E4KAd7eURcJVDUQ5pEU3UNmf3YvXAoaqcmvlLtjKFeIQdp28a2bOXsIktjdS7ovqikkKTdhKcEl6oYffITvX82OYY2aBR6vr9Rzim4pvT5RAr5WkTzHzZNSyuxELUzJld5Culsb5YINT/2xulVtzklG55oAQJBAMY7BiPwZcSiENeGqD65dqJ6TatSoVcxuO35bjMkhSheEfK+RZILT9bF3Y7oHzz9N9JPt7HH36QqPo5g7skEWp0CQQCtRpiLygFuZxy2zc8IZ8oRJl3lxqbYrvsVcsQZh5sWZFzTw5Ir8gUHvVDKw0YHv9u6imJJo0bmckSVHNFNuJHBAkBN+iHsOUCPHQFrBrlgEyyrtVigMBzvY8vPMPM8gv0uZ/K/fkF3taNuZN9Gu+Ct6R3wSROFh45d0ZBSCadkw6WxAkA2FvWDnZl4x0NVZUWdkBip5dol4i8vMOA8P8krVwN4p/e6OzCj+zbKxbQ0t2RjAucqggxQhGGbikwSaB6GLnmBAkASrimTm6PBaO4ZdMAVTswYW999SPin9mXRH6d4QqnLZKrqT6yjhujLUCsxdNDdkYy6qhuRhPFhep/nu0BkXLeW";
	private static String AESKey = "sThLirjOOa7HVo0EHCZ0+w==";

	// 进入绑卡
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/jumpbindcard")
	public @ResponseBody Object ghtdhToRepayment(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {

		return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
				ip + "/v1.0/paymentgateway/topup/kkdh/bindcard/info?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode("信用卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
						+ "&userName=" + URLEncoder.encode(userName, "UTF-8") + "&idCard=" + idCard + "&expiredTime="
						+ expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ip);

	}

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/bindcard")
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
		} catch (Exception e) {
			e.printStackTrace();
			return ResultWrap.init(CommonConstants.FALIED, "查询银行编码出错!");
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		String orderNo = UUID.randomUUID().toString().replaceAll("-", "");

		map.put("merchantNo", merchantNo);
		map.put("backURL", ip + "/v1.0/paymentgateway/topup/kkdh/bindcard/notifyurl");
		map.put("orderNo", orderNo);
		map.put("idType", "IDCard");// 付款人证件类型
		map.put("requestTimeStamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
		map.put("bankCode", bankCode);
		map.put("bankCardNo", AESEncode(AESKey, bankCard));// 银行卡号：内容用AES加密发送
		map.put("userName", AESEncode(AESKey, userName));// 持卡人姓名 持卡人姓名:
															// 内容用AES加密发送
		map.put("idCode", AESEncode(AESKey, this.idCardLastToUppercase(idCard)));// 证件号：内容用AES加密发送
		map.put("bankCardPhone", AESEncode(AESKey, phone));// 电话：内容用AES加密发送
		map.put("validity", AESEncode(AESKey, this.expiredTimeToYYMM(expiredTime)));// 信用卡有效期
		map.put("cvv2", AESEncode(AESKey, securityCode));// 内容用AES加密发送

		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡预下单的请求报文======" + map + " &&&&&bankCard======" + bankCard);
		LOG.info("绑卡预下单的明文报文======bankCard=" + bankCard + "&userName=" + userName + "&idCard="
				+ this.idCardLastToUppercase(idCard) + "&phone=" + phone + "&securityCode=" + securityCode
				+ "&expiredTime=" + this.expiredTimeToYYMM(expiredTime));
		String URL = "http://api.zhclwlkj.com/payapi/api/prebind";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("请求绑卡预下单返回的======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);
		String code = fromObject.getString("code");
		String msg = fromObject.getString("msg");

		String needSms = "-1";
		if (fromObject.containsKey("needSMS")) {
			needSms = fromObject.getString("needSMS");
		}

		if ("0".equals(code) && "0".equals(needSms)) {

			GHTBindCard ghtBindCardByBankCard = topupPayChannelBusiness.getGHTBindCardByBankCard(bankCard);

			GHTBindCard ghtBindCard = new GHTBindCard();
			ghtBindCard.setBankCard(bankCard);
			ghtBindCard.setIdCard(idCard);
			ghtBindCard.setPhone(phone);
			ghtBindCard.setOrderCode(orderNo);
			ghtBindCard.setStatus("0");

			topupPayChannelBusiness.createGHTBindCard(ghtBindCard);

			return ResultWrap.init(CommonConstants.SUCCESS, msg, orderNo);

		} else {

			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/bindcardconfirm")
	public @ResponseBody Object ghtdhBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "orderNo") String orderNo, @RequestParam(value = "smsCode") String smsCode)
					throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		Map<String, String> map = new HashMap<String, String>();

		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderNo); // 快捷预下单时的订单
		map.put("merchantNo", merchantNo);
		map.put("smsCode", smsCode);
		map.put("requestTimeStamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("绑卡确认的请求报文======" + map);
		String URL = "http://api.zhclwlkj.com/payapi/api/confirmbind";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("绑卡确认请求返回的 sendPost======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String code = fromObject.getString("code");
		String msg = fromObject.getString("msg");

		if ("0".equals(code)) {

			return ResultWrap.init(CommonConstants.SUCCESS, msg,
					ip + "/v1.0/paymentgateway/topup/toght/bindcardsuccesspage");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/agentPay")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "realAmount") String realAmount,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phone) throws Exception {

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

		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);
		map.put("accountProp", "1");// 1私人2公司:
		map.put("bankGenneralName", bankName);// 银行通用名称
		map.put("bankName", bankName);// 开户行名称
		map.put("bankCode", bankCode);
		map.put("accountName", AESEncode(AESKey, userName));// 账户名
		map.put("accountNo", AESEncode(AESKey, bankCard));// 开户卡号
		map.put("phone", AESEncode(AESKey, phone));
		map.put("bankProvince", "上海");// 
		map.put("bankCity", "上海");// bankCity开户行所在市:
		map.put("orderAmount", bigRealAmount);
		map.put("curCode", "RMB");
		map.put("orderTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);

		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代付的请求报文======" + map + "   &&&&&&  bankcard=====" + bankCard);
		LOG.info("代付的明文报文======bankCard=" + bankCard + "&userName=" + userName);
		String URL = "http://api.zhclwlkj.com/payapi/api/payForAnotherOne";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("请求代付返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String code = fromObject.getString("code");
		String msg = fromObject.getString("msg");

		if ("0".equals(code) | "3602".equals(code)) {

			return ResultWrap.init("999998", "等待银行还款中!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 代扣接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/withhold")
	public @ResponseBody Object withhold(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "realAmount") String realAmount,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phone, @RequestParam("payAnotherOrderNo") String payAnotherOrderNo,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {

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

		map.put("merchantNo", merchantNo);
		map.put("backURL", ip + "/v1.0/paymentgateway/topup/kkdh/withhold/notifyurl");
		map.put("orderID", orderCode);
		map.put("orderAmount", bigRealAmount);// 代扣金额
		map.put("curCode", "RMB");
		map.put("idType", "IDCard");
		map.put("requestTimeStamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));// 订单时间);
		map.put("bankCode", bankCode);//
		map.put("userCardNo", AESEncode(AESKey, bankCard));
		map.put("userName", AESEncode(AESKey, userName));
		map.put("idCode", AESEncode(AESKey, this.idCardLastToUppercase(idCard)));
		map.put("userCardPhone", AESEncode(AESKey, phone));
		map.put("validity", AESEncode(AESKey, this.expiredTimeToYYMM(expiredTime)));
		map.put("cvv2", AESEncode(AESKey, securityCode));
		map.put("payAnotherOrderNo", payAnotherOrderNo);
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代扣的请求报文======" + map + "   &&&&&&  bankcard=====" + bankCard);
		LOG.info("代扣的明文报文======bankCard=" + bankCard + "&userName=" + userName);
		String URL = "http://api.zhclwlkj.com/payapi/api/sameQuickMerge";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("请求代付返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);

		String code = fromObject.getString("code");
		String msg = fromObject.getString("msg");

		if ("0".equals(code) | "3601".equals(code)) {

			return ResultWrap.init("999998", "等待银行出款中!");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 代付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/agentPay/orderquery")
	public @ResponseBody Object kkdhTransferOrderQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);// 预下单时的订单号
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代付订单查询的请求报文======" + map);
		String URL = "http://api.zhclwlkj.com/payapi/api/queryPayForAnotherOneOrder";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("请求代付订单查询返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);
		String code = fromObject.getString("code");
		String orderStatus = fromObject.getString("orderStatus");
		String msg = fromObject.getString("msg");

		if ("0".equals(code)) {
			String message = null;
			if ("1".equals(orderStatus)) {
				message = "代付成功";
			} else if ("2".equals(orderStatus)) {
				message = "代付中";
			} else if ("3".equals(orderStatus)) {
				message = "代付失败";
			} else if ("4".equals(orderStatus)) {
				message = "其他情况";
			} else if ("5".equals(orderStatus)) {
				message = "订单超时";
			} else if ("6".equals(orderStatus)) {
				message = "处理中";
			}
			return ResultWrap.init(CommonConstants.SUCCESS, message);
		} else {
			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 代扣订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/withhold/orderquery")
	public @ResponseBody Object WithholdOrderQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderCode);// 代扣预下单时的订单号
		map.put("sign", generateSign(RSAPrivateKey, createLinkString(map)));

		LOG.info("代扣订单查询的请求报文======" + map);
		String URL = "http://api.zhclwlkj.com/payapi/api/queryOrder";
		String body = HttpClientUtil.post(URL, map);
		LOG.info("请求扣付订单查询返回的body======" + body);

		JSONObject fromObject = JSONObject.fromObject(body);
		String code = fromObject.getString("code");
		String orderStatus = fromObject.getString("orderStatus");
		String msg = fromObject.getString("msg");

		if ("0".equals(code)) {
			String message = null;
			if ("1".equals(orderStatus)) {
				message = "预支付";
			} else if ("2".equals(orderStatus)) {
				message = "已支付";
			} else if ("3".equals(orderStatus)) {
				message = "过期失效";
			} else if ("4".equals(orderStatus)) {
				message = "支付失败";
			} else if ("5".equals(orderStatus)) {
				message = "支付中";
			} else if ("6".equals(orderStatus)) {
				message = "订单取消";
			} else if ("7".equals(orderStatus)) {
				message = "处理中";
			} else if ("8".equals(orderStatus)) {
				message = "订单支付超时";
			} else if ("9".equals(orderStatus)) {
				message = "退款中";
			} else if ("10".equals(orderStatus)) {
				message = "已退款";
			} else if ("11".equals(orderStatus)) {
				message = "退款失败";
			} else if ("12".equals(orderStatus)) {
				message = "其他情况";
			}
			return ResultWrap.init(CommonConstants.SUCCESS, message);
		} else {
			return ResultWrap.init(CommonConstants.FALIED, msg);
		}

	}

	// 绑卡异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/bindcard/notifyurl")
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
		String tradeStatus = request.getParameter("tradeStatus");
		String tradeMsg = request.getParameter("tradeMsg");

		if ("0".equals(tradeStatus)) {
			LOG.info("==============异步通知绑卡订单状态:" + tradeStatus);
			LOG.info("==============异步通知绑卡订单状态描述:" + tradeMsg);

			GHTBindCard ghtBindCardByOrderCode = topupPayChannelBusiness.getGHTBindCardByOrderCode(orderNo);

			ghtBindCardByOrderCode.setStatus("1");

			topupPayChannelBusiness.createGHTBindCard(ghtBindCardByOrderCode);

		} else if ("3701".equals(tradeStatus)) {
			LOG.info("==============异步通知绑卡订单状态:" + tradeStatus);
			LOG.info("==============异步通知绑卡订单状态描述:" + tradeMsg);

		}
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();

	}

	// 代扣的异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/kkdh/withhold/notifyurl")
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

		String orderNo = request.getParameter("orderNo");// 平台唯一订单号
		String outTradeNo = request.getParameter("outTradeNo");// 商户系统唯一订单号
		String tradeStatus = request.getParameter("tradeStatus");// 订单状态|0：成功,3701:系统异常
		String tradeMsg = request.getParameter("tradeMsg");

		LOG.info("==============异步通知银行代扣订单状态:" + tradeStatus);
		LOG.info("==============异步通知银行代扣订单状态描述:" + tradeMsg);

		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();

	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/kkdh/bindcard/info")
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

		return "kkdhbindcard";
	}

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

		return sign;
	}

	public static String rsasign(String content, String privateKey) {
		String charset = "UTF-8";
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

}
