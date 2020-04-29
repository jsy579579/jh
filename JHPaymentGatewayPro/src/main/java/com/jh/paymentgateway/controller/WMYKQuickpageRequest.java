package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
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

import com.alibaba.druid.sql.dialect.postgresql.parser.PGExprParser;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WMYKQuickpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(WMYKQuickpageRequest.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	// 支付请求地址
	private String fastPayUrl = "http://pay.junet.tech/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	// 代付请求地址
	private String transferUrl = "http://pay.junet.tech:28080/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	// 订单查询请求地址
	private String orderCodeQueryUrl = "http://pay.junet.tech:38080/callpaypipe/com/callpaypipe/servlet/ReleaseCallPayServlet";

	private String productUuid = "f628eb0c541947389b03027b8f841647_dcea678b2a4845e79e9c5d9654e001b7_d206b206fef24a74913459e480421ef4";

	private String merchantNo = "CX0008371";

	//新无卡======
	private String productUuid1 = "fc89350d5670454880dbf96797ca6b3b_dcea678b2a4845e79e9c5d9654e001b7_96e1b8f36dfa49dcbde0d33553fb69b8";
		
	private String merchantNo1 =  "CX0175502";
	//新无卡======
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisUtil redisUtil;

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/bindcard")
	public @ResponseBody Object wmykBindCard(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> maps = new HashMap<String, String>();

		PaymentRequestParameter paymentRequestParameter = redisUtil.getPaymentRequestParameter(orderCode);
		String bankName = paymentRequestParameter.getCreditCardBankName();
		
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			this.addOrderCauseOfFailure(orderCode, "查询银行编码出错!", paymentRequestParameter.getIpAddress());
			
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
		map.put("bankCardNo", paymentRequestParameter.getBankCard());
		map.put("idType", "01");
		map.put("userName", paymentRequestParameter.getUserName());
		map.put("idCode", paymentRequestParameter.getIdCard());
		map.put("phone", paymentRequestParameter.getCreditCardPhone());
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

			String orderNo = fromObject2.getString("orderNo");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("orderNo", orderNo);

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

			this.addOrderCauseOfFailure(orderCode, dealMsg, paymentRequestParameter.getIpAddress());
			
			return maps;
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/bindcardconfirm")
	public @ResponseBody Object wmykBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "orderNo") String orderNo,
			@RequestParam(value = "smsCode") String smsCode)
			throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
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

			RestTemplate rt = new RestTemplate();
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("bankCard", prp.getBankCard());
			multiValueMap.add("phone", prp.getCreditCardPhone());
			multiValueMap.add("idCard", prp.getIdCard());
			multiValueMap.add("status", "0");
			multiValueMap.add("orderCode", orderNo);

			String result = rt.postForObject("http://106.15.47.73/v1.0/paymentchannel/topup/wmyknew/bindcardcreate", multiValueMap, String.class);
			JSONObject jsonObject;
			String respCode;
			try {
				jsonObject = JSONObject.fromObject(result);
				respCode = jsonObject.getString("resp_code");
			} catch (Exception e) {
				LOG.error("保存绑卡信息出错======");
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
				
				this.addOrderCauseOfFailure(orderCode, "保存绑卡信息出错!", prp.getIpAddress());
				
				return map;
			}
			
			if("000000".equals(respCode)) {
				
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/towmykquickpay?bankName="
						+ URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&nature=" + URLEncoder.encode(prp.getCreditCardNature(), "UTF-8")
						+ "&bankCard=" + prp.getBankCard() + "&ordercode=" + orderCode + "&expiredTime=" + prp.getExpiredTime()
						+ "&securityCode=" + prp.getSecurityCode() + "&phone=" + prp.getCreditCardPhone() + "&ipAddress=http://ds.jiepaypal.cn" + "&ip=" + ip + "&ips=" + prp.getIpAddress() );
				maps.put(CommonConstants.RESP_MESSAGE, "发起交易");
				
				return maps;
				
			}else {
				
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
				
				this.addOrderCauseOfFailure(orderCode, "交易排队中,请稍后重试!", prp.getIpAddress());
				
				return map;
				
			}
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);

			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return maps;
		}

	}

	// 快捷支付预下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/preorder")
	public @ResponseBody Object wmykNewPreOrder(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "storeNo") String storeNo,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankName = prp.getCreditCardBankName();
		
		if(bankName.contains("光大")) {
			String amount = prp.getAmount();
			
			if(new BigDecimal(amount).compareTo(new BigDecimal("5000"))>0) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!", prp.getIpAddress());
				
				return maps;
				
			}
			
			
		}
		
		RestTemplate rt = new RestTemplate();
		String url = prp.getIpAddress() + "/v1.0/notice/sms/vericode?phone=" + prp.getCreditCardPhone();
		String resultStr = rt.getForObject(url, String.class);
		JSONObject jsonObject = JSONObject.fromObject(resultStr);
		String code = jsonObject.getString("result");
		LOG.info("发送码：" + smsCode + "===校验码：" + code);
		
		if(!smsCode.equals(code)) {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "验证码输入不正确,请仔细核对重新输入!");

			this.addOrderCauseOfFailure(orderCode, "验证码输入不正确,请仔细核对重新输入!", prp.getIpAddress());
			
			return maps;
		}
		
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("bankCard", prp.getBankCard());
		
		String result = restTemplate.postForObject("http://106.15.47.73/v1.0/paymentchannel/topup/wmyknew/bindcardquery", multiValueMap, String.class);
		String respCode;
		try {
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("查询绑卡信息出错======");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "交易排队中,请稍后重试!");
			
			this.addOrderCauseOfFailure(orderCode, "查询绑卡信息出错!", prp.getIpAddress());
			
			return maps;
		}
		
		if(!"000000".equals(respCode)) {
			
			multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("bankCard", prp.getBankCard());
			multiValueMap.add("userId", prp.getUserId());

			result = rt.postForObject("http://106.15.47.73/v1.0/paymentchannel/topup/wmyknew/querybindcard", multiValueMap, String.class);
			try {
				jsonObject = JSONObject.fromObject(result);
				respCode = jsonObject.getString("resp_code");
			} catch (Exception e) {
				LOG.error("查询绑卡信息出错======");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
				
				this.addOrderCauseOfFailure(orderCode, "查询绑卡信息出错!", prp.getIpAddress());
				
				return maps;
			}
			
			if(!"000000".equals(respCode)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "绑卡失败,需要重新绑卡!");
				
				this.addOrderCauseOfFailure(orderCode, "绑卡失败,需要重新绑卡!", prp.getIpAddress());
				
				return maps;
			}
			
		}
		
		if("".equals(storeNo)) {
			LOG.info("用户没有选择商户======");
			
			if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
					|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
					|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
					|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
					|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
					|| bankName.contains("汇丰")) {
				
				storeNo = "MD0059829";
				
			}else {
				
				storeNo = "MD0234358";
				
			}
		}
		
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询银行编码出错!");

			this.addOrderCauseOfFailure(orderCode, "查询银行编码出错!", prp.getIpAddress());
			
			return maps;
		}

		if (bankName.contains("广发") || bankName.contains("广东发展")) {

			bankCode = "GDB";
		}

		String Amount = new BigDecimal(prp.getAmount()).multiply(new BigDecimal("100")).setScale(0).toString();

		Map<String, Object> map = new HashMap<String, Object>();

		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {
			
			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);
		
		}else {
			
			map.put("productUuid", productUuid1);
			map.put("merchantNo", merchantNo1);
			
		}
		
		map.put("payChannelCode", bankCode);
		map.put("payChannelType", "6");
		map.put("orderNo", orderCode);
		map.put("orderAmount", Amount);
		map.put("bankCardNo", prp.getBankCard());
		map.put("idType", "01");
		map.put("userName", prp.getUserName());
		map.put("idCode", prp.getIdCard());
		map.put("phone", prp.getCreditCardPhone());
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

			map = (Map<String, Object>) wmykFastPay(orderCode);

			return map;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			
			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return maps;

		}

	}

	// 快捷支付确认支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/fastpay")
	public @ResponseBody Object wmykFastPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入快捷支付确认支付接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<String, Object>();

		String bankName = prp.getCreditCardBankName();
		
		if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {
			
			map.put("productUuid", productUuid);
			map.put("merchantNo", merchantNo);
		
		}else {
			
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

		if("10000".equals(dealCode)) {
			
			RestTemplate rt = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/remark";
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("ordercode", orderCode);
			multiValueMap.add("remark", "支付成功,等待银行扣款");
			String result = rt.postForObject(url, multiValueMap, String.class);
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");

			return maps;
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, dealMsg);
			
			this.addOrderCauseOfFailure(orderCode, dealMsg, prp.getIpAddress());
			
			return maps;
			
		}
		
	}

	/*// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/transfer")
	public @ResponseBody Object wmykTransfer(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "extra") String extra) throws Exception {

		LOG.info("开始进入代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		// 充值卡卡号
		String bankCard = prp.getBankCard();
		String realAmount = prp.getRealAmount();
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String bankName = fromObject.getString("bankName");

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

		map.put("productUuid", productUuid);
		map.put("merchantNo", merchantNo);
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

		if ("10000".equals(dealCode)) {

			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "代付成功");

			return maps;

		} else {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
			return maps;

		//}

	}*/

	

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/tobindcardpage")
	public @ResponseBody Object wmykToBindCardPage(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			)throws Exception {
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String bankCard = prp.getBankCard();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("redirect_url", ip + "/v1.0/paymentgateway/topup/towmykquick/bindcard?bankName="
				+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
				+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&expiredTime=" + expiredTime
				+ "&securityCode=" + securityCode + "&ipAddress=" + ip);
		map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");
		
		return map;
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wmykquick/topaypage")
	public @ResponseBody Object wmykToPayPage(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			)throws Exception {
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankName = prp.getCreditCardBankName();
		String nature = prp.getCreditCardNature();
		String bankCard = prp.getBankCard();
		String creditCardPhone = prp.getCreditCardPhone();
		
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put("redirect_url", ip + "/v1.0/paymentgateway/topup/towmykquickpay?bankName="
				+ URLEncoder.encode(bankName, "UTF-8") + "&nature=" + URLEncoder.encode(nature, "UTF-8")
				+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&expiredTime=" + expiredTime
				+ "&securityCode=" + securityCode + "&phone=" + creditCardPhone + "&ipAddress=http://ds.jiepaypal.cn" + "&ip=" + ip + "&ips=" + prp.getIpAddress());
		map.put(CommonConstants.RESP_MESSAGE, "发起交易");
		
		return map;
	}
	

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/towmykquick/bindcard")
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
		String ordercode = request.getParameter("ordercode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("ipAddress", ipAddress);

		return "wmykquickbindcard";
	}

	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/towmykquickbankinfo")
	public String returnWMYKQuickBankInfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");// 结算卡的卡类型
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");
		String isRegister = request.getParameter("isRegister");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("isRegister", isRegister);

		return "wmykquickbankinfo";
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/towmykquickpay")
	public String returnWMYKQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
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
		String ipAddress = request.getParameter("ipAddress");
		String ipa = request.getParameter("ip");
		String ips = request.getParameter("ips");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("nature", nature);
		model.addAttribute("orderCode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("ip", ipa);
		model.addAttribute("ips", ips);

		return "wmykquickpay";
	}
	
	
}
