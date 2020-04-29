package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BankNumCode;
import com.jh.paymentchannel.pojo.WMYKBindCard;
import com.jh.paymentchannel.pojo.WMYKNewBindCard;
import com.jh.paymentchannel.pojo.WMYKNewChooseCity;
import com.jh.paymentchannel.pojo.WMYKNewCity;
import com.jh.paymentchannel.pojo.WMYKNewCityMerchant;
import com.jh.paymentchannel.pojo.WMYKNewProvince;
import com.jh.paymentchannel.pojo.WMYKXWKCityMerchant;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WMYKNewpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(WMYKNewpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	// 支付请求地址
	private String fastPayUrl = "http://pay.junet.tech/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	// 代付请求地址
	private String transferUrl = "http://pay.junet.tech:28080/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	// 订单查询请求地址
	private String orderCodeQueryUrl = "http://pay.junet.tech:38080/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	// 全渠道======
	private String productUuid = "f628eb0c541947389b03027b8f841647_dcea678b2a4845e79e9c5d9654e001b7_d206b206fef24a74913459e480421ef4";

	private String merchantNo = "CX0008371";
	// 全渠道======

	// 新无卡======
	private String productUuid1 = "fc89350d5670454880dbf96797ca6b3b_dcea678b2a4845e79e9c5d9654e001b7_96e1b8f36dfa49dcbde0d33553fb69b8";

	private String merchantNo1 = "CX0175502";
	// 新无卡======

	@Autowired
	private RestTemplate restTemplate;

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/to/repayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userId") String userId)
			throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		String bankName = null;
		String cardtype = null;
		String securityCode = null;
		String expiredTime = null;
		String phone = null;
		String idCard = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
			cardtype = fromObject.getString("cardType");
			securityCode = fromObject.getString("securityCode");
			expiredTime = fromObject.getString("expiredTime");
			phone = fromObject.getString("phone");
			idCard = fromObject.getString("idcard");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		WMYKNewBindCard wmykNewBindCard = topupPayChannelBusiness.getWMYKNewBindCardByBankCard(bankCard);

		if (wmykNewBindCard == null) {
			LOG.info("用户需要绑卡======");

			/*maps = (Map<String, Object>) wmykBindCardQuery(request, bankCard, userId);

			Object respCode1 = maps.get("resp_code");
			Object respMessage1 = maps.get("resp_message");
			if ("000000".equals(respCode1.toString())) {

				WMYKNewBindCard bindCard = new WMYKNewBindCard();
				bindCard.setPhone(phone);
				bindCard.setBankCard(bankCard);
				bindCard.setIdCard(idCard);
				bindCard.setStatus("1");
				
				topupPayChannelBusiness.createWMYKNewBindCard(bindCard);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
				return maps;
			} else {*/
				
				maps.put(CommonConstants.RESP_CODE, "999996");
				maps.put(CommonConstants.RESULT,
						ipAddress + "/v1.0/paymentchannel/topup/towmyknew/bindcard?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
								+ "&bankCard=" + bankCard + "&userId=" + userId + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress);
				maps.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");
				return maps;
			//}
			
		} else {

			if (!"1".equals(wmykNewBindCard.getStatus())) {

				maps = (Map<String, Object>) wmykBindCardQuery(request, bankCard, userId);

				Object respCode1 = maps.get("resp_code");
				Object respMessage1 = maps.get("resp_message");
				if ("000000".equals(respCode1.toString())) {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
					return maps;
				} else {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				}

			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
				return maps;

			}
		}
	}

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/bindcard")
	public @ResponseBody Object wmykBindCard(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> maps = new HashMap<String, String>();

		String phone = null;
		String idCard = null;
		String userName = null;
		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			phone = fromObject.getString("phone");
			idCard = fromObject.getString("idcard");
			userName = fromObject.getString("userName");
			bankName = fromObject.getString("bankName");
		} catch (Exception e1) {
			e1.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			return maps;
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merchantNo", merchantNo);
		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");
		map.put("orderNo", UUID.randomUUID().toString().replace("-", ""));
		map.put("bankCardNo", bankCard);
		map.put("idType", "01");
		map.put("userName", userName);
		map.put("idCode", idCard);
		map.put("phone", phone);
		map.put("cvv2", securityCode);
		map.put("validPeriod", this.expiredTimeToYYMM(expiredTime));

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("绑卡申请的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(fastPayUrl + "?methodname=quickPayBind&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("绑卡申请请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			if (fromObject2.containsKey("status")) {

				String status = fromObject2.getString("status");

				if ("SUCCESS".equalsIgnoreCase(status)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put("channel_tag", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "您已绑卡,请勿重复操作!");

					return maps;
				}

			}

			String orderNo = fromObject2.getString("orderNo");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("channel_tag", "sdj");
			maps.put("orderNo", orderNo);

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_tag", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

			return maps;
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/bindcardconfirm")
	public @ResponseBody Object wmykBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "orderNo") String orderNo, @RequestParam(value = "smsCode") String smsCode)
			throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		String phone = null;
		String idCard = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			phone = fromObject.getString("phone");
			idCard = fromObject.getString("idcard");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merchantNo", merchantNo);
		map.put("orderNo", orderNo);
		map.put("verifiyCode", smsCode);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("绑卡确认的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(
				fastPayUrl + "?methodname=quickPayBindConfirm&jsondata={fromObject3}", map, String.class,
				fromObject3.toString());

		LOG.info("绑卡确认请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			WMYKNewBindCard wmykNewBindCard = new WMYKNewBindCard();
			wmykNewBindCard.setBankCard(bankCard);
			wmykNewBindCard.setIdCard(idCard);
			wmykNewBindCard.setPhone(phone);
			wmykNewBindCard.setStatus("0");
			wmykNewBindCard.setOrderCode(orderNo);

			topupPayChannelBusiness.createWMYKNewBindCard(wmykNewBindCard);

			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put("channel_type", "sdj");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, "999999");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

			return maps;
		}

	}

	// 绑卡查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/querybindcard")
	public @ResponseBody Object wmykBindCardQuery(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userId") String userId)
			throws Exception {
		LOG.info("开始进入绑卡查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		String phone = null;
		String userName = null;
		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			phone = fromObject.getString("phone");
			userName = fromObject.getString("userName");
			bankName = fromObject.getString("bankName");
		} catch (Exception e1) {
			e1.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			return maps;
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		WMYKNewBindCard wmykNewBindCard = topupPayChannelBusiness.getWMYKNewBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merchantNo", merchantNo);
		map.put("orderNo", wmykNewBindCard.getOrderCode());
		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");
		map.put("bankCardNo", bankCard);
		map.put("userName", userName);
		map.put("phone", phone);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("绑卡查询的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(fastPayUrl + "?methodname=selBankBindInfo&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("绑卡查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			String bindStatus = fromObject2.getString("bindStatus");

			if ("0".equals(bindStatus)) {

				wmykNewBindCard.setStatus("1");

				topupPayChannelBusiness.createWMYKNewBindCard(wmykNewBindCard);

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put(CommonConstants.RESP_MESSAGE, "绑卡成功");

				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, "999999");
				maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

				return maps;
			}

		} else {

			maps.put(CommonConstants.RESP_CODE, "999999");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

			return maps;
		}

	}

	// 快捷支付预下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/preorder")
	public @ResponseBody Object wmykNewPreOrder(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "storeNo") String storeNo) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String realAmount = resultObj.getString("realAmount");
		String userId = resultObj.getString("userid");

		String phone = null;
		String idCard = null;
		String userName = null;
		String bankName = null;
		String securityCode = null;
		String expiredTime = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			phone = fromObject.getString("phone");
			idCard = fromObject.getString("idcard");
			userName = fromObject.getString("userName");
			bankName = fromObject.getString("bankName");
			securityCode = fromObject.getString("securityCode");
			expiredTime = fromObject.getString("expiredTime");
		} catch (Exception e1) {
			e1.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "您的信用卡有效期信息为空,请及时更新您的信息!");

			return maps;
		}

		if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "您的信用卡安全码信息为空,请及时更新您的信息!");

			return maps;

		}

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			return maps;
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");
		map.put("orderNo", orderCode);
		map.put("orderAmount", RealAmount);
		map.put("bankCardNo", bankCard);
		map.put("idType", "01");
		map.put("userName", userName);
		map.put("idCode", idCard);
		map.put("phone", phone);
		map.put("cvv2", securityCode);
		map.put("validPeriod", this.expiredTimeToYYMM(expiredTime));
		map.put("storeNo", storeNo);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("快捷支付预下单的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(fastPayUrl + "?methodname=quickPayApply&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("快捷支付预下单请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {
			LOG.info("请求快捷支付预下单成功======");

			map = (Map<String, Object>) wmykFastPay(orderCode, bankName);

			return map;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;

		}

	}

	// 快捷支付确认支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/fastpay")
	public @ResponseBody Object wmykFastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankName") String bankName) throws Exception {
		LOG.info("开始进入快捷支付确认支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String realAmount = resultObj.getString("realAmount");

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", orderCode);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("快捷支付确认支付的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(fastPayUrl + "?methodname=quickPayConfirm&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("快捷支付确认支付请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		maps.put(CommonConstants.RESP_CODE, "999998");
		maps.put("channel_type", "sdj");
		maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
		return maps;

	}

	// 快捷支付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/orderquery")
	public @ResponseBody Object wmykOrderQuery(@RequestParam(value = "orderCode") String orderCode

	) throws Exception {

		LOG.info("开始进入快捷支付订单查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String userId = resultObj.getString("userid");

		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", orderCode);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("快捷支付订单查询的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(
				orderCodeQueryUrl + "?methodname=paySearchOrder&jsondata={fromObject3}", map, String.class,
				fromObject3.toString());

		LOG.info("快捷支付订单查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {
			String orderStatus = fromObject2.getString("orderStatus");

			if ("1".equals(orderStatus)) {
				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				return maps;

			} else if ("2".equals(orderStatus)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
				return maps;

			}
			// else if("0".equals(orderStatus) || "7".equals(orderStatus)) {
			else {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;

			}

		} else if ("30001".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/transfer")
	public @ResponseBody Object wmykTransfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "extra") String extra) throws Exception {

		LOG.info("开始进入代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String userId = resultObj.getString("userid");

		String userName = null;
		String bankName = null;
		String phone = null;
		String idCard = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			phone = fromObject.getString("phone");
			idCard = fromObject.getString("idcard");
			userName = fromObject.getString("userName");
			bankName = fromObject.getString("bankName");
		} catch (Exception e1) {
			e1.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			return maps;
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", orderCode);
		map.put("orderNoList", extra);
		map.put("bankCardNo", bankCard);
		map.put("userName", userName);
		map.put("bankGenneralName", bankName);
		map.put("bankName", "上海宝山区支行");
		map.put("bankCode", bankCode);
		map.put("bankProvcince", "上海");
		map.put("bankCity", "上海");
		map.put("orderAmount", RealAmount);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(transferUrl + "?methodname=payForSameName&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("代付请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		/*
		 * if ("10000".equals(dealCode)) {
		 * 
		 * maps.put(CommonConstants.RESP_CODE, "000000"); maps.put("channel_type",
		 * "sdj"); maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
		 * 
		 * return maps;
		 * 
		 * } else {
		 */

		maps.put(CommonConstants.RESP_CODE, "999998");
		maps.put("channel_type", "sdj");
		maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
		return maps;

		// }

	}

	// 代付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/transferquery")
	public @ResponseBody Object wmykTransferQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		LOG.info("开始进入代付查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String userId = resultObj.getString("userid");

		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", orderCode);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付查询的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(
				orderCodeQueryUrl + "?methodname=payForAnotherOneSearch&jsondata={fromObject3}", map, String.class,
				fromObject3.toString());

		LOG.info("代付查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
			return maps;
		} else if ("90001".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "代付失败");
			return maps;
		} else if ("30001".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknewprovince/query")
	public @ResponseBody Object wmykNewProvince() throws Exception {

		List<WMYKNewProvince> wmykNewProvince = topupPayChannelBusiness.getWMYKNewProvince();

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykNewProvince);
	}

	// 查询城市
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknewcity/query")
	public @ResponseBody Object wmykNewCity(@RequestParam(value = "provinceCode") String provinceCode)
			throws Exception {

		List<WMYKNewCity> wmykNewCity = topupPayChannelBusiness.getWMYKNewCityByProvinceCode(provinceCode);

		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykNewCity);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknewcitymerchant/querybycode")
	public @ResponseBody Object wmykCityMerchant(@RequestParam(value = "cityCode") String cityCode,
			@RequestParam(value = "bankName") String bankName) throws Exception {

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {
			LOG.info("全渠道支持的商户======");

			List<WMYKNewCityMerchant> wmykNewCit = topupPayChannelBusiness
					.getWMYKNewCityMerchantByCityCode(cityCode.trim());

			if (wmykNewCit != null && wmykNewCit.size() > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykNewCit);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}

		} else {
			LOG.info("新无卡支持的商户======");

			List<WMYKXWKCityMerchant> wmykxwkCity = topupPayChannelBusiness
					.getWMYKXWKCityMerchantByCityCode(cityCode.trim());

			if (wmykxwkCity != null && wmykxwkCity.size() > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykxwkCity);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}

		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknewchoosecity/create")
	public @ResponseBody Object wmykNewCreate(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "cityCode") String cityCode)
			throws Exception {

		try {

			WMYKNewChooseCity wmykNewChooseCity = topupPayChannelBusiness.getWMYKNewChooseCityByBankCard(bankCard);

			if (wmykNewChooseCity == null) {
				WMYKNewChooseCity chooseCity = new WMYKNewChooseCity();

				chooseCity.setBankCard(bankCard.trim());
				chooseCity.setCityCode(cityCode.trim());
				chooseCity
						.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

				topupPayChannelBusiness.createWMYKNewChooseCity(chooseCity);

			} else {

				wmykNewChooseCity.setCityCode(cityCode.trim());
				wmykNewChooseCity
						.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

				topupPayChannelBusiness.createWMYKNewChooseCity(wmykNewChooseCity);
			}
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "亲,选择地区出现了一点小问题,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "选择地区成功!");
	}

	// 用户选择城市的另外一个接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknewchoosecity/createother")
	public @ResponseBody Object wmykNewCreateOther(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "cityCode") String cityCode, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "version", required = false, defaultValue = "6") String version) throws Exception {

		String url = "http://creditcardmanager/v1.0/creditcardmanager/doeshave/task/execute";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		requestEntity.add("version", version);
		requestEntity.add("creditCardNumber", bankCard);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		String respCode = jsonObject.getString("resp_code");

		if (CommonConstants.FALIED.equals(respCode)) {

			return ResultWrap.init(CommonConstants.FALIED, "您当前有还款任务正在执行,暂不支持选择其他地区!");

		}

		try {

			WMYKNewChooseCity wmykNewChooseCity = topupPayChannelBusiness.getWMYKNewChooseCityByBankCard(bankCard);

			if (wmykNewChooseCity == null) {
				WMYKNewChooseCity chooseCity = new WMYKNewChooseCity();

				chooseCity.setBankCard(bankCard.trim());
				chooseCity.setCityCode(cityCode.trim());
				chooseCity
						.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

				topupPayChannelBusiness.createWMYKNewChooseCity(chooseCity);

			} else {

				wmykNewChooseCity.setCityCode(cityCode.trim());
				wmykNewChooseCity
						.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

				topupPayChannelBusiness.createWMYKNewChooseCity(wmykNewChooseCity);
			}
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "亲,选择地区出现了一点小问题,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "选择地区成功!");
	}

	// 查询是否绑卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/bindcardquery")
	public @ResponseBody Object wmykNewBindCardQuery(@RequestParam(value = "bankCard") String bankCard)
			throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		WMYKNewBindCard wmykNewBindCard = topupPayChannelBusiness.getWMYKNewBindCardByBankCard(bankCard);

		if (wmykNewBindCard == null || !"1".equals(wmykNewBindCard.getStatus())) {

			maps.put(CommonConstants.RESP_CODE, "999996");
			maps.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作!");

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put(CommonConstants.RESP_MESSAGE, "已完成绑卡!");

			return maps;
		}

	}

	// 保存绑卡信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyknew/bindcardcreate")
	public @ResponseBody Object wmykNewBindCardCreate(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "idCard") String idCard)
			throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		WMYKNewBindCard wmykNewBindCard = new WMYKNewBindCard();
		wmykNewBindCard.setPhone(phone);
		wmykNewBindCard.setIdCard(idCard);
		wmykNewBindCard.setBankCard(bankCard);
		wmykNewBindCard.setStatus("1");

		WMYKNewBindCard createWMYKNewBindCard = topupPayChannelBusiness.createWMYKNewBindCard(wmykNewBindCard);

		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESULT, createWMYKNewBindCard);
		maps.put(CommonConstants.RESP_MESSAGE, "保存成功");

		return maps;

	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/towmyknew/bindcard")
	public String returnWMYKNewBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String userId = request.getParameter("userId");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("userId", userId);
		model.addAttribute("ipAddress", ipAddress);

		return "wmyknewbindcard";
	}

	// 用于TX快捷的代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykquick/transfer")
	public @ResponseBody Object wmykQuickTransfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "realAmount") String realAmount, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "extra") String extra)
			throws Exception {

		LOG.info("开始进入代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByUserId.get("resp_message"));
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName = fromObject.getString("userName");
		String bankNameOfDebitCard = fromObject.getString("bankName");

		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankNameOfDebitCard));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			return maps;
		}

		if (bankNameOfDebitCard.contains("广发") || bankNameOfDebitCard.contains("广东发展")) {

			bankCode = "GDB";
		}

		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", orderCode);
		map.put("orderNoList", extra);
		map.put("bankCardNo", cardNo);
		map.put("userName", userName);
		map.put("bankGenneralName", bankNameOfDebitCard);
		map.put("bankName", "上海宝山区支行");
		map.put("bankCode", bankCode);
		map.put("bankProvcince", "上海");
		map.put("bankCity", "上海");
		map.put("orderAmount", RealAmount);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(transferUrl + "?methodname=payForSameName&jsondata={fromObject3}",
				map, String.class, fromObject3.toString());

		LOG.info("代付请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
			return maps;

		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;

		}

	}

	// 用于TX代付订单查询的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykquick/transferquery")
	public @ResponseBody Object wmykQuickTransferQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		LOG.info("开始进入TX代付查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String userId = resultObj.getString("userid");
		String thirdOrderCode = resultObj.getString("thirdOrdercode");

		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0",
					userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
		} catch (Exception e) {
			e.printStackTrace();

			return ResultWrap.init(CommonConstants.FALIED, "查询不到该银行卡信息,可能已被删除!");
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {

			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);

		} else {

			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);

		}

		map.put("orderNo", thirdOrderCode);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付查询的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(
				orderCodeQueryUrl + "?methodname=payForAnotherOneSearch&jsondata={fromObject3}", map, String.class,
				fromObject3.toString());

		LOG.info("代付查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String dealCode = fromObject2.getString("dealCode");
		String dealMsg = fromObject2.getString("dealMsg");

		if ("10000".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
			return maps;
		} else if ("90001".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "代付失败");
			return maps;
		} else if ("30001".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			return maps;
		}

	}

}
