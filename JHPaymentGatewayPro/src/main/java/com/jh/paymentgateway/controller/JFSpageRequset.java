package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.JFSBindCard;
import com.jh.paymentgateway.pojo.JFSRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jf.AES;
import com.jh.paymentgateway.util.jf.Base64;
import com.jh.paymentgateway.util.jf.HttpClient4Util;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class JFSpageRequset extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(JFSpageRequset.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static String key = "119BB4E50D4E69773C87C2E1B149C93E";
	private static String partnerNo = "4M3WSB0S";
	private static String requestURL = "http://fast.jfpays.com:19085/rest/api/";// http://fast.jfpays.com:19087/rest/api/

	/**
	 * 用户注册
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入即富s用户注册 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String cardName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String phoneC = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String userId = prp.getUserId();
		String cardNo = prp.getDebitCardNo();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardType = prp.getCreditCardCardType();
		String securityCode = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(cardName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		Map<String, Object> maps = new HashMap<String, Object>();

		JFSRegister jfsRegister = topupPayChannelBusiness.getJFSRegisterByIdCard(idCard);
		JFSBindCard jfsBindCard = topupPayChannelBusiness.getJFSBindCardByBankCard(bankCard);
		if (jfsRegister == null) {
			LOG.info("--------- 即富s新用户，开始进件 --------");

			String url = requestURL + "610001";

			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> head = new HashMap<String, Object>();
			Map<String, Object> rateList = new HashMap<String, Object>();
			rateList.put("QUICKPAY_WK_HF", rate);
			// 公共参数
			String orderId = getRandom();
			head.put("version", "1.0.0");
			head.put("charset", UTF_8);
			head.put("partnerNo", partnerNo);
			head.put("txnCode", "610001");
			head.put("orderId", orderId);// 18-32位纯数字
			head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
			head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
			map.put("head", head);

			map.put("merchantCode", userId);
			map.put("merName", "上海莘丽");
			map.put("merShortName", "富贵商城");// 商户简称
			map.put("bankAccountName", userName);// 持卡人姓名
			map.put("idCardNo", idCard);
			map.put("phoneno", phoneD);
			map.put("merAddress", "上海市宝山区");// 商户地址
			map.put("bankAccountNo", cardNo);// 结算银行卡号
			map.put("bankUnitNo", bankChannelNo);// 联行号
			map.put("bankName", cardName);// 银行名称
			map.put("productList", rateList);// 产品列表
			map.put("province", "000000");// 商户所在省
			map.put("city", "310000");// 商户所在市
			map.put("withdrawDepositSingleFee", getNumber(ExtraFee));// 单笔消费交易手续费
																		// 单位分
			String jsonStr = JSON.toJSONString(map);
			LOG.info("即富s请求明文：" + jsonStr);
			String signData = getSign(key, jsonStr);
			String encryptData = getEncrypt(key, jsonStr);

			Map<String, String> params = Maps.newHashMap();
			params.put("encryptData", encryptData);
			params.put("signData", signData);
			params.put("orderId", orderId);
			params.put("partnerNo", partnerNo);
			params.put("ext", "");

			LOG.info("params : " + JSON.toJSONString(params));

			LOG.info("============ 即富s进件请求地址:" + url);

			try {
				byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
				if (resByte == null) {
					return "请求超时";
				}
				String resStr = new String(resByte, UTF_8);
				System.out.println("============ 返回报文原文:" + resStr);
				JSONObject resJson = JSON.parseObject(resStr);
				String sign = resJson.getString("signature");
				String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
				boolean signChecked = Objects.equals(sign.toUpperCase(),
						DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
				Map<String, Object> result = new HashMap<>();
				result.put("返回源报文", resStr);
				result.put("返回明文", res);
				result.put("验签结果", signChecked);
				LOG.info("返回明文：" + res);
				LOG.info("返回验签结果：" + signChecked);
				LOG.info("返回源报文：" + resStr);
				JSONObject jsonobj = JSONObject.parseObject(res);
				String platMerchantCode = jsonobj.getString("platMerchantCode");
				String headJson = jsonobj.getString("head");
				JSONObject MessageJson = JSONObject.parseObject(headJson);
				String message = MessageJson.getString("respMsg");
				LOG.info("返回平台商户号：" + platMerchantCode);
				LOG.info("返回描述：" + message);

				if (!"".equals(platMerchantCode) && platMerchantCode != null) {
					LOG.info("即富s进件---成功：" + message);
					LOG.info("--------- 保存即富s新用户数据 --------");

					JFSRegister jfsRequest = new JFSRegister();
					jfsRequest.setMerchantNo(platMerchantCode);
					jfsRequest.setExtraFee(ExtraFee);
					jfsRequest.setIdCard(idCard);
					jfsRequest.setPhone(phoneD);
					jfsRequest.setRate(rate);
					jfsRequest.setBankCard(cardNo);
					jfsRequest.setUserName(userName);
					topupPayChannelBusiness.createJFSRegister(jfsRequest);
		        
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jfs/jump-bindcard-view?ordercode=" + orderCode
							+ "&bankCard=" + bankCard
							+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
							+ "&securityCode=" + securityCode
							+ "&expiredTime="+ expiredTime 
							+ "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
				} else {
					LOG.info("即富s进件---异常：" + message);

					this.addOrderCauseOfFailure(orderCode, message, rip);

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (!rate.equals(jfsRegister.getRate()) | !ExtraFee.equals(jfsRegister.getExtraFee())
				| !cardNo.equals(jfsRegister.getBankCard())) {
			LOG.info("------------- 修改交易费率/结算卡/手续费 -------------");
			maps = (Map<String, Object>) changeRate(orderCode);
			String message = (String) maps.get("resp_message");
			if ("000000".equals(maps.get("resp_code"))) {
				if (jfsBindCard==null) {
					LOG.info("------------- 跳转绑卡 -------------");
					
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jfs/jump-bindcard-view?ordercode=" + orderCode
							+ "&bankCard=" + bankCard
							+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
							+ "&securityCode=" + securityCode
							+ "&expiredTime="+ expiredTime  
							+ "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else{
					LOG.info("------------- 跳转交易 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jfs/pay-view?bankName=" + URLEncoder.encode(cardName, "UTF-8")
									+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
									+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				}		
			} else {
				LOG.info("即富s修改费率---异常：" + message);

				this.addOrderCauseOfFailure(orderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;
			}
		} else if (jfsBindCard==null) {
			LOG.info("------------- 跳转绑卡 -------------");
			
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jfs/jump-bindcard-view?ordercode=" + orderCode
					+ "&bankCard=" + bankCard
					+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8")
					+ "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
					+ "&securityCode=" + securityCode
					+ "&expiredTime="+ expiredTime  
					+ "&isRegister=1");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			return maps;
		} else {
			LOG.info("------------- 直接交易 -------------");

			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jfs/pay-view?bankName=" + URLEncoder.encode(cardName, "UTF-8")
							+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
							+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			return maps;
		}
		return maps;
	}

	/**
	 * 申请绑卡
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/bindCard")
	public @ResponseBody Object bindCard(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime) throws IOException { 
		LOG.info("============ 进入即富s申请绑卡 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String bankCard = prp.getBankCard();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String amount = prp.getAmount();
		String cardName = prp.getDebitBankName();
		String cardNo = prp.getDebitCardNo();

		String url = requestURL + "610002";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_WK_HF", rate);

		// 公共参数
		String orderId = getRandom();
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610002");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		JFSRegister jfsRegister = topupPayChannelBusiness.getJFSRegisterByIdCard(idCard);
		
		String merchantNo = "";
		if (jfsRegister != null) {
			merchantNo = jfsRegister.getMerchantNo();
		}

		// 业务参数
		map.put("openOrderId", orderCode);
		map.put("platMerchantCode", merchantNo);// 入网成功返回的商户号
		map.put("accountName", userName);// 持卡人姓名
		map.put("cardNo", bankCard);
		map.put("certNo", idCard);// 身份证号
		map.put("phoneno", phoneC);// 手机号
		map.put("cvn2", securityCode);// 银行代码 有参见表
		String expired = this.expiredTimeToMMYY(expiredTime);
		map.put("expired", expired);// 银行代码 有参见表

		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文：" + jsonStr);
		String signData = getSign(key, jsonStr);
		String encryptData = getEncrypt(key, jsonStr);

		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", orderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富s进件请求地址:" + url);

		try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				return "请求超时";
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			JSONObject resJson = JSON.parseObject(resStr);
			String sign = resJson.getString("signature");
			String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
			boolean signChecked = Objects.equals(sign.toUpperCase(),
					DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
			Map<String, Object> result = new HashMap<>();
			result.put("返回源报文", resStr);
			result.put("返回明文", res);
			result.put("验签结果", signChecked);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String signStatus = jsonobj.getString("signStatus");
			String openCardId = jsonobj.getString("openCardId");
			String rsHead = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			LOG.info("返回签约状态：" + signStatus);
			// INIT 待开通 SIGNING 等待签约中 SUCCESS 开通成功 FAIL 开通失败 INVALID 绑卡状态失效
			if ("SUCCESS".equals(signStatus)) {
				LOG.info("---绑卡成功：" + signStatus);
				
				JFSBindCard jfsBindCard = topupPayChannelBusiness.getJFSBindCardByBankCard(bankCard);
				if (jfsBindCard==null) {
					jfsBindCard = new JFSBindCard();
					jfsBindCard.setBankCard(bankCard);
					jfsBindCard.setBindingNum(openCardId);
					jfsBindCard.setIdCard(idCard);
					jfsBindCard.setPhone(phoneC);
					jfsBindCard.setStatus("0");
					topupPayChannelBusiness.createJFSBindCard(jfsBindCard);
				}
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("redirect_url",
						ip + "/v1.0/paymentgateway/quick/jfs/pay-view?bankName=" + URLEncoder.encode(cardName, "UTF-8")
								+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
								+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
			} else {
				LOG.info("即富s申请绑卡---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maps;
	}
	
	/**
	 * 修改结算卡，交易费率，交易手续费
	 * 
	 * @param orderCode
	 * @param workId
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/changeRate")
	public @ResponseBody Object changeRate(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("========= 进入即富s修改费率接口 ========");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String cardNo = prp.getDebitCardNo();
		String phoneD = prp.getDebitPhone();
		String idCard = prp.getIdCard();
		String rate = prp.getRate();
		String ex = prp.getExtraFee();

		String url = requestURL + "610006";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_WK_HF", rate);
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610006");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		JFSRegister jfsRegister = topupPayChannelBusiness.getJFSRegisterByIdCard(idCard);
		String MerchantNo = jfsRegister.getMerchantNo();
		// 业务参数
		map.put("platMerchantCode", MerchantNo);
		map.put("bankAccountNo", cardNo);
		map.put("phoneno", phoneD);
		map.put("productList", rateList);
		map.put("withdrawDepositSingleFee", getNumber(ex));

		// 发送
		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文：" + jsonStr);
		String signData = getSign(key, jsonStr);
		String encryptData = getEncrypt(key, jsonStr);

		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", orderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富s修改费率请求地址:" + url);

		try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				return "请求超时";
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			JSONObject resJson = JSON.parseObject(resStr);
			String sign = resJson.getString("signature");
			String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
			boolean signChecked = Objects.equals(sign.toUpperCase(),
					DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
			Map<String, Object> result = new HashMap<>();
			result.put("返回源报文", resStr);
			result.put("返回明文", res);
			result.put("验签结果", signChecked);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String rsHead = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			String respCode = headJson.getString("respCode");
			if ("000000".equals(respCode)) {
				jfsRegister.setBankCard(cardNo);
				jfsRegister.setExtraFee(ex);
				jfsRegister.setRate(rate);
				jfsRegister.setCreateTime(new Date());
				topupPayChannelBusiness.createJFSRegister(jfsRegister);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("即富s修改费率,结算卡,手续费---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}

	
	/**
	 * 快捷支付短信
	 * 
	 * @param orderCode
	 * @param amount
	 * @param bankCard
	 * @param idCard
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/pay-sms")
	public @ResponseBody Object paySMS(@RequestParam(value = "ordercode") String orderCode) {
		LOG.info("========= 进入即富s快捷支付短信接口 ========");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<String, Object>();
		// 获取平台商户号
		JFSRegister jfs = topupPayChannelBusiness.getJFSRegisterByIdCard(idCard);
		String MerchantNo = jfs.getMerchantNo();
		String rate = jfs.getRate();
		// 获取开卡机构号
		JFSBindCard jfsBindCard = topupPayChannelBusiness.getJFSBindCardByBankCard(bankCard);
		String openCardId = jfsBindCard.getBindingNum();

		String url = requestURL + "610004";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		String orderId = getRandom();

		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610004");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);
		// 业务参数
		map.put("consumeOrderId", orderCode);
		map.put("platMerchantCode", MerchantNo);
		map.put("openCardId", openCardId);
		map.put("payAmount", getNumber(amount));
		map.put("remark", "快捷充值到卡");
		map.put("productCode", "QUICKPAY_WK_HF");
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/jfs/pay/call-back?consumeOrderId=" + orderCode);

		// 发送
		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文：" + jsonStr);
		String signData = getSign(key, jsonStr);
		String encryptData = getEncrypt(key, jsonStr);

		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", orderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富s支付短信请求地址:" + url);

		try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				return "请求超时";
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			JSONObject resJson = JSON.parseObject(resStr);
			String sign = resJson.getString("signature");
			String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
			boolean signChecked = Objects.equals(sign.toUpperCase(),
					DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
			Map<String, Object> result = new HashMap<>();
			result.put("返回源报文", resStr);
			result.put("返回明文", res);
			result.put("验签结果", signChecked);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String workId = jsonobj.getString("workId");
			String rsHead = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			String respCode = headJson.getString("respCode");
			String requestNo = headJson.getString("orderId");
			LOG.info("第三方流水号:" + requestNo);
			LOG.info("返回平台流水号：" + workId);
			if ("000000".equals(respCode)) {
				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String URL = null;
				String results = null;
				URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/thirdordercode";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", requestNo);
				try {
					results = restTemplate.postForObject(URL, requestEntity, String.class);
					LOG.info("*********************下单成功，添加第三方流水号***********************");
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + results);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("orderId", workId);

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("快捷支付短信---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}
	
	/**
	 * 快捷支付
	 * 
	 * @param orderCode
	 * @param workId
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/fast-pay")
	public @ResponseBody Object fastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "orderId") String workId,@RequestParam(value = "smsCode") String smsCode) {

		LOG.info("--------  进入即富s快捷支付  ---------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		String url = requestURL + "610005";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();

		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610005");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		// 业务参数
		map.put("workId", workId);
		map.put("smsCode", smsCode);

		// 发送
		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文：" + jsonStr);
		String signData = getSign(key, jsonStr);
		String encryptData = getEncrypt(key, jsonStr);

		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", orderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富s确认支付请求地址:" + url);

		try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				return "请求超时";
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			JSONObject resJson = JSON.parseObject(resStr);
			String sign = resJson.getString("signature");
			String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
			boolean signChecked = Objects.equals(sign.toUpperCase(),
					DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
			Map<String, Object> result = new HashMap<>();
			result.put("返回源报文", resStr);
			result.put("返回明文", res);
			result.put("验签结果", signChecked);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String rsHead = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			String respCode = headJson.getString("respCode");
			if ("000000".equals(respCode)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("快捷支付---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maps;
	}

	/**
	 * 支付状态查询
	 * 
	 * @param orderCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jfs/queryFastPay")
	public @ResponseBody Object queryFastPay(@RequestParam(value = "third_order_code") String third_order_code) {

		LOG.info("-------- 进入支付状态查询  ---------");

		String url = requestURL + "610008";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610008");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		// 业务参数
		map.put("consumeOrderId", third_order_code);

		// 发送
		String jsonStr = JSON.toJSONString(map);
		LOG.info("请求明文：" + jsonStr);
		String signData = getSign(key, jsonStr);
		String encryptData = getEncrypt(key, jsonStr);

		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", orderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富s查询支付状态请求地址:" + url);

		try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				return "请求超时";
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			JSONObject resJson = JSON.parseObject(resStr);
			String sign = resJson.getString("signature");
			String res = AES.decode(Base64.decode(resJson.getString("encryptData")), key.substring(0, 16));
			boolean signChecked = Objects.equals(sign.toUpperCase(),
					DigestUtils.sha1Hex(res + key.substring(16)).toUpperCase());
			Map<String, Object> result = new HashMap<>();
			result.put("返回源报文", resStr);
			result.put("返回明文", res);
			result.put("验签结果", signChecked);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String rsHead = jsonobj.getString("head");
			String status = jsonobj.getString("status");
			String statusDesc = jsonobj.getString("statusDesc");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			String respCode = headJson.getString("respCode");
			if ("000000".equals(respCode)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
				maps.put("payStatus", status);//01成功:04处理中
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maps;
	}

	/**
	 * 跳转到绑卡页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jfs/jump-bindcard-view")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("jfsBindCard------------------跳转到绑卡界面");

		String ordercode = request.getParameter("ordercode");
		String bankCard = request.getParameter("bankCard");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String securityCode = request.getParameter("securityCode");
		String expiredTime = request.getParameter("expiredTime");
		
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ip);

		return "jfsbindcard";
	}

	/**
	 * 跳转到交易界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jfs/pay-view")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("jfsPay------------------跳转到交易界面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		// bankCard
		String ordercode = request.getParameter("orderCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String phone = request.getParameter("phone");
		String ips = request.getParameter("ips");
		String amount = request.getParameter("amount");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("phone", phone);
		model.addAttribute("ips", ips);
		model.addAttribute("amount", amount);

		return "jfspay";
	}

	/**
	 * 交易异步通知
	 * 
	 * @param encryptData
	 * @param signature
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/jfs/pay/call-back")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String encryptData = request.getParameter("encryptData");
		String signature = request.getParameter("signature");
		String orderCode = request.getParameter("consumeOrderId");
		LOG.info("消费回调：data: {} ", encryptData, signature);

		String dataPlain = AES.decode(org.apache.commons.codec.binary.Base64.decodeBase64(encryptData),
				key.substring(0, 16));
		LOG.info("消费回调：dataPlain: {} ", dataPlain);
		String checkSign = DigestUtils.sha1Hex(encryptData + key.substring(16));

		LOG.info("消费回调：checkSign: {} ", checkSign);
		try {
			LOG.info("消费回调：signature: {} ", signature);
			signature = URLDecoder.decode(signature, UTF_8.name());
			LOG.info("消费回调：signature urldecode: {} ", signature);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (com.google.common.base.Objects.equal(signature, checkSign)) {
			LOG.error("签名验证成功");
			JSONObject jsonobj = JSONObject.parseObject(dataPlain);
			String head = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(head);
			String orderId = headJson.getString("orderId");
			LOG.info("第三方查询流水号：" + orderId);
			String orderStatus = jsonobj.getString("orderStatus");
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String URL = null;
			String result = null;
			if ("01".equals(orderStatus)) {
				LOG.info("*********************交易成功***********************");
				
				URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(URL, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

				LOG.info("订单已交易成功!");

				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();

			}else if("02".equals(orderStatus)){
				LOG.error("支付失败");
				this.addOrderCauseOfFailure(orderCode, "支付失败", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			}else if("03".equals(orderStatus)){
				LOG.error("初始未支付");
				this.addOrderCauseOfFailure(orderCode, "初始未支付", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			}else if("99".equals(orderStatus)){
				LOG.error("支付超时");
				this.addOrderCauseOfFailure(orderCode, "支付超时", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			}else if("04".equals(orderStatus)){
				LOG.error("支付处理中");
				this.addOrderCauseOfFailure(orderCode, "支付处理中，请稍后查询", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			}
		} else {
			LOG.error("签名验证失败");
			this.addOrderCauseOfFailure(orderCode, "签名验证失败", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("000000");
			pw.close();
		}
	}

	/**
	 * 生成签名
	 * 
	 * @param key
	 * @param plainData
	 * @return
	 */
	public static String getSign(String key, String plainData) {

		return DigestUtils.sha1Hex(plainData + key.substring(16));
	}

	/**
	 * 生成报文
	 * 
	 * @param key
	 * @param plainData
	 * @return
	 */
	public static String getEncrypt(String key, String plainData) {

		return Base64.encode(AES.encode(plainData, key.substring(0, 16)));
	}

	/**
	 * 生成时间格式
	 * 
	 * @param timeType
	 * @return
	 */
	public static String TimeFormat(String timeType) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeType);
		String nowTime = simpleDateFormat.format(new Date());
		LOG.info("当前时间：" + nowTime);
		return nowTime;

	}

	/**
	 * 生成18位数订单号 当前时间：yyyyMMddHHmmss + 4位随机数
	 * 
	 * @return
	 */
	public static String getRandom() {

		String result = "";

		result += TimeFormat("yyyyMMddHHmmss");

		Double rand = Math.random() * 10000;

		if (rand < 10) {

			result += "000" + rand.toString().substring(0, 1);

		} else if (rand < 100) {

			result += "00" + rand.toString().substring(0, 2);

		} else if (rand < 1000) {

			result += "0" + rand.toString().substring(0, 3);

		} else {

			result += rand.toString().substring(0, 4);
		}
		LOG.info("18位数：" + result);

		return result;

	}

	/**
	 * 金额/分
	 * 
	 * @param ExtraFee
	 * @return
	 */
	public static String getNumber(String ExtraFee) {
		BigDecimal num1 = new BigDecimal(ExtraFee);
		BigDecimal num2 = new BigDecimal("100");
		BigDecimal rsNum = num1.multiply(num2);
		BigDecimal MS = rsNum.setScale(0, BigDecimal.ROUND_DOWN);
		LOG.info("金额/分：" + MS.toString());
		return MS.toString();
	}

}
