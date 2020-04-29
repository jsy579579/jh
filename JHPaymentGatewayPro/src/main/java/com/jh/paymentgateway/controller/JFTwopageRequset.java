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
import com.jh.paymentgateway.pojo.JFBindCard;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.JFXBindCard;
import com.jh.paymentgateway.pojo.JFXRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jf.AES;
import com.jh.paymentgateway.util.jf.Base64;
import com.jh.paymentgateway.util.jf.HttpClient4Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.UUIDGenerator;

@Controller
@EnableAutoConfiguration
public class JFTwopageRequset extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(JFTwopageRequset.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static String key = "GEAuet26XPLaqAYnsufuKieqBXz9NiSS";
	private static String partnerNo = "F9msNmmz";
	private static String requestURL = "http://fast.jfpays.com:19087/rest/api/";
	private static String rateCode = "101002";// 0.45%+0.5元/0.55%+0.5 （现在支持的有101001 101002）  

	/**
	 * 用户注册
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入101002用户注册 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String bankName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String phoneC = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String userId = prp.getUserId();
		String cardNo = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String amount = prp.getAmount();
				
		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		Map<String, Object> maps = new HashMap<String, Object>();

		JFXRegister jfxRequest = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		JFXBindCard jfxBindCard = topupPayChannelBusiness.getJFXBindCardByBankCard(bankCard);
		if (jfxRequest == null) {
			LOG.info("--------- 新用户，开始进件 --------");

			String url = requestURL + "701001";

			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> head = new HashMap<String, Object>();
			Map<String, Object> rateList = new HashMap<String, Object>();
			rateList.put("QUICKPAY_OF_NP", rate);
			// 公共参数
			String orderId = getRandom();
			head.put("version", "1.0.0");
			head.put("charset", UTF_8);
			head.put("partnerNo", partnerNo);
			head.put("txnCode", "701001");
			head.put("orderId", orderId);// 18-32位纯数字
			head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
			head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
			map.put("head", head);

			// 获取token
			Map<String, Object> tokenMap = (Map<String, Object>) getToken();

			String token = "";
			String respCode = (String) tokenMap.get("resp_code");
			if ("000000".equals(respCode)) {
				token = (String) tokenMap.get("resp_message");

				LOG.info("获取token成功：" + token);
			} else {
				String msg = (String) tokenMap.get("resp_message");

				LOG.info("获取token失败：" + msg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, msg);
				return maps;
			}

			// 业务参数
			map.put("token", token);
			map.put("merchantCode", userId);
			map.put("rateCode", rateCode);// 费率编码
			map.put("merName", "上海莘丽");
			map.put("merAbbr", "富贵商城");// 商户简称
			map.put("idCardNo", idCard);
			map.put("bankAccountNo", cardNo);// 银行卡号
			map.put("phoneno", phoneD);
			map.put("bankAccountName", userName);// 持卡人姓名
			map.put("bankAccountType", "PRIVATE");// 银行账户类型
			map.put("bankName", bankName);// 银行名称
			map.put("bankSubName", "上海宝山区支行");
			map.put("bankCode", bankCode);// 银行代码 有参见表
			map.put("bankAbbr", bankAbbr);// 银行代码 有参见表
			map.put("bankChannelNo", bankChannelNo);// 支行银联号 有参见表
			map.put("bankProvince", "2900");// 参见省市数据格式
			map.put("bankCity", "2916");// 参见省市数据格式
			map.put("debitRate", rate);// 如千分之五
			map.put("debitCapAmount", "99999900");// 借记卡封顶 单位：分
			map.put("creditRate", rate);// 如千分之五
			map.put("creditCapAmount", "99999900");// 信用卡封顶 单位：分
			map.put("withdrawDepositRate", "0");// 提现费率 如千分之五
			map.put("withdrawDepositSingleFee", getNumber(ExtraFee));// 单笔提现手续费
																		// 单位：分

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

			LOG.info("============ 即富进件请求地址:" + url);

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
				String paltMerchantCode = jsonobj.getString("paltMerchantCode");
				String headJson = jsonobj.getString("head");
				JSONObject MessageJson = JSONObject.parseObject(headJson);
				String message = MessageJson.getString("respMsg");
				LOG.info("返回平台商户号：" + paltMerchantCode);
				LOG.info("返回描述：" + message);
				
				if (!"".equals(paltMerchantCode) && paltMerchantCode != null) {
					LOG.info("即富进件---成功：" + message);
					LOG.info("--------- 保存新用户数据 --------");

					JFXRegister jFXRequest = new JFXRegister();
					jFXRequest.setMerchantNo(paltMerchantCode);
					jFXRequest.setExtraFeeTwo(ExtraFee);
					jFXRequest.setIdCard(idCard);
					jFXRequest.setPhone(phoneD);
					jFXRequest.setRateTwo(rate);
					jFXRequest.setBankCard(cardNo);
					topupPayChannelBusiness.createJFXRegister(jFXRequest);
					
					maps.put(CommonConstants.RESULT,
								ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
				} else {
					LOG.info("即富进件---异常：" + message);

					this.addOrderCauseOfFailure(orderCode, message, rip);

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (jfxRequest.getRateTwo()==null){// 用户第一次是101001进件，第二次用101002消费，无101002费率
			LOG.info("用户第一次是101001进件，第二次用101002消费，无101002费率-------------------");
			// 新增101002的费率
			maps = (Map<String, Object>) newRate(orderCode);
			String msg = (String) maps.get("resp_message");
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("------------- 新增101002的费率 -->> 成功,判断是否需要修改101002费率或绑卡或直接交易  -------------");
				if (!rate.equals(jfxRequest.getRateTwo()) | !ExtraFee.equals(jfxRequest.getExtraFeeTwo())
						| !cardNo.equals(jfxRequest.getBankCard())){
					LOG.info("------------- 修改101002交易费率/结算卡/手续费 -------------");
					maps = (Map<String, Object>) changeRate(orderCode);
					if ("000000".equals(maps.get("resp_code"))) {
						LOG.info("------------- 修改费率/结算卡/手续费 -->> 成功,判断是否绑卡或直接交易  -------------");

						if (jfxBindCard == null) {// 已进件，未绑卡
							LOG.info("------------- 已进件，未绑卡 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
							LOG.info("------------- 以前申请过绑卡，待确认状态 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						} else {
							LOG.info("------------- 直接交易 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
											+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
											+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						}
					}
				} else if (jfxBindCard == null) {// 已进件，未绑卡
					LOG.info("------------- 已进件，未绑卡 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
					LOG.info("------------- 以前申请过绑卡，待确认状态 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else {
					LOG.info("------------- 直接交易 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
									+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
									+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				}
			}
		}  else if (jfxRequest.getRateOne()==null){// 无101001费率
			LOG.info("已进件101002用户，无101001费率，进入开通101001费率-------------------");
			// 新增101001的费率
			maps = (Map<String, Object>) newRate(orderCode);
			String msg = (String) maps.get("resp_message");
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("------------- 新增101001的费率 -->> 成功,判断是否需要修改101002费率或绑卡或直接交易  -------------");
				if (!rate.equals(jfxRequest.getRateTwo()) | !ExtraFee.equals(jfxRequest.getExtraFeeTwo())
						| !cardNo.equals(jfxRequest.getBankCard())){
					LOG.info("------------- 修改101002交易费率/结算卡/手续费 -------------");
					maps = (Map<String, Object>) changeRate(orderCode);
					if ("000000".equals(maps.get("resp_code"))) {
						LOG.info("------------- 修改费率/结算卡/手续费 -->> 成功,判断是否绑卡或直接交易  -------------");

						if (jfxBindCard == null) {// 已进件，未绑卡
							LOG.info("------------- 已进件，未绑卡 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
							LOG.info("------------- 以前申请过绑卡，待确认状态 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						} else {
							LOG.info("------------- 直接交易 -------------");

							maps.put(CommonConstants.RESULT,
									ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
											+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
											+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put(CommonConstants.RESP_MESSAGE, "成功");
							return maps;
						}
					}
				} else if (jfxBindCard == null) {// 已进件，未绑卡
					LOG.info("------------- 已进件，未绑卡 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
					LOG.info("------------- 以前申请过绑卡，待确认状态 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else {
					LOG.info("------------- 直接交易 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
									+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
									+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				}
			}
		} else if (!rate.equals(jfxRequest.getRateTwo()) | !ExtraFee.equals(jfxRequest.getExtraFeeTwo())
				| !cardNo.equals(jfxRequest.getBankCard())) {
			LOG.info("------------- 修改101002交易费率/结算卡/手续费 -------------");
			maps = (Map<String, Object>) changeRate(orderCode);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("------------- 修改费率/结算卡/手续费 -->> 成功,判断是否绑卡或直接交易  -------------");

				if (jfxBindCard == null) {// 已进件，未绑卡
					LOG.info("------------- 已进件，未绑卡 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
					LOG.info("------------- 以前申请过绑卡，待确认状态 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				} else {
					LOG.info("------------- 直接交易 -------------");

					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
									+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
									+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					return maps;
				}
			}
		} else if (jfxBindCard == null) {// 已进件，绑卡或再次绑卡
			LOG.info("------------- 已进件，绑卡或再次绑卡 -------------");

			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			return maps;
		} else if (jfxBindCard != null && !"SUCCESS".equals(jfxBindCard.getStatus())) {// 以前申请过绑卡，待确认状态
			LOG.info("------------- 重新绑定待确认状态卡 -------------");

			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jft/jump-bindcard-view?ordercode=" + orderCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			return maps;
		} else {
			LOG.info("------------- 直接交易 -------------");

			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
							+ prp.getIpAddress() + "&phone=" + phoneC + "&amount=" + amount + "&isRegister=1");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			return maps;
		}

		return maps;
	}

	/**
	 * 申请绑卡 获取绑卡短信
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/bindCard")
	public @ResponseBody Object bindCard(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		LOG.info("============ 进入申请绑卡 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		// String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		String url = requestURL + "701002";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_OF_NP", rate);

		// 公共参数
		String orderId = getRandom();
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "701002");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respCode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respCode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		
		JFXBindCard jFXBindCard = topupPayChannelBusiness.getJFXBindCardByBankCard(bankCard);
		
		JFXRegister jFXRegister = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String merchantNo = "";
		if (jFXRegister != null) {
			merchantNo = jFXRegister.getMerchantNo();
		}

		// 业务参数
		map.put("token", token);
		map.put("smsOrderId", orderCode);
		map.put("rateCode", rateCode);// 费率编码
		map.put("platMerchantCode", merchantNo);// 入网成功返回的商户号
		map.put("accountName", userName);// 持卡人姓名
		map.put("cardNo", bankCard);
		map.put("cardType", "C");// C 信用卡 D 借记卡
		map.put("certType", "ID");// ID身份证
		map.put("certNo", idCard);// 身份证号
		map.put("bankAccountType", "PRIVATE");// 银行账户类型
		map.put("phoneno", phoneC);// 手机号
		map.put("bankCode", bankCode);// 银行代码 有参见表
		map.put("bankAbbr", bankAbbr);// 银行代码 有参见表

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

		LOG.info("============ 即富进件请求地址:" + url);

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
			String activateStatus = jsonobj.getString("activateStatus");
			String headJson = jsonobj.getString("head");
			JSONObject MessageJson = JSONObject.parseObject(headJson);
			String message = MessageJson.getString("respMsg");
			String workId = MessageJson.getString("workId");
			LOG.info("返回签约状态：" + activateStatus);
			// INIT 待开通 SIGNING 等待签约中 SUCCESS 开通成功 FAIL 开通失败 INVALID 绑卡状态失效
			if ("SUCCESS".equals(activateStatus)) {
				LOG.info("---绑卡成功：" + activateStatus);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else if ("SIGNING".equals(activateStatus)) {
				LOG.info("申请绑卡---成功：" + activateStatus);
				LOG.info("-------- 保存用户绑卡信息 ---------");
				
				if (jFXBindCard==null) {
					
					JFXBindCard jfxBindCard = new JFXBindCard();
					
					jfxBindCard.setBankCard(bankCard);
					jfxBindCard.setIdCard(idCard);
					jfxBindCard.setPhone(phoneC);
					jfxBindCard.setStatus(activateStatus);
					jfxBindCard.setBindingNum(workId);
					topupPayChannelBusiness.createJFXBindCard(jfxBindCard);
				}

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("申请绑卡---异常：" + activateStatus);
				this.addOrderCauseOfFailure(orderCode, activateStatus, rip);

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
	 * 确认绑卡
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/confirmBindCard")
	public @ResponseBody Object confirmBindCard(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime) throws IOException {

		LOG.info("============ 进入确认绑卡 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String bankCard = prp.getBankCard();
		String bankName = prp.getDebitBankName();// 借记卡银行名称
		String cardNo = prp.getDebitCardNo();
		String cardName = prp.getCreditCardBankName();// 信用卡银行名称
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(cardName);
		// String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		String url = requestURL + "701003";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_OF_NP", rate);
		// 公共参数
		String orderId = getRandom();
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "701003");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respCode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respCode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		JFXRegister jFXRegister = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		JFXBindCard jFXBindCard = topupPayChannelBusiness.getJFXBindCardByBankCard(bankCard);
		String merchantNo = "";
		if (jFXRegister != null) {
			merchantNo = jFXRegister.getMerchantNo();
		}

		// 业务参数
		map.put("token", token);
		map.put("smsOrderId", orderCode);
		map.put("platMerchantCode", merchantNo);// 入网成功返回的商户号
		map.put("rateCode", rateCode);// 费率编码
		map.put("accountName", userName);// 持卡人姓名
		map.put("cardNo", bankCard);
		map.put("cardType", "C");// C 信用卡 D 借记卡
		map.put("certType", "ID");// ID身份证
		map.put("certNo", idCard);// 身份证号
		map.put("phoneno", phoneC);// 手机号
		map.put("cvn2", securityCode);

		String expired = this.expiredTimeToYYMM(expiredTime);

		map.put("expired", expired);// YYMM
		map.put("smsCode", smsCode);
		map.put("bankCode", bankCode);// 银行代码 有参见表
		map.put("bankAbbr", bankAbbr);// 银行代码 有参见表

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

		LOG.info("============ 即富进件请求地址:" + url);

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
			String activateStatus = jsonobj.getString("activateStatus");
			String headJson = jsonobj.getString("head");
			JSONObject MessageJson = JSONObject.parseObject(headJson);
			String message = MessageJson.getString("respMsg");
			LOG.info("返回签约状态：" + activateStatus);
			// INIT 待开通 SIGNING 等待签约中 SUCCESS 开通成功 FAIL 开通失败 INVALID 绑卡状态失效
			if ("SUCCESS".equals(activateStatus)) {
				LOG.info("确认绑卡---成功：" + message);

				jFXBindCard.setStatus(activateStatus);
				topupPayChannelBusiness.createJFXBindCard(jFXBindCard);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("redirect_url",
						ip + "/v1.0/paymentgateway/quick/jft/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
								+ prp.getIpAddress() + "&phone=" + phoneC + "&isRegister=1");
			} else {
				LOG.info("确认绑卡---异常：" + message);
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
	 * 新增费率
	 * 
	 * @param orderCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/newRate")
	public @ResponseBody Object newRate(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("========= 进入101002新增费率接口 ========");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String cardNo = prp.getDebitCardNo();
		String phoneD = prp.getDebitPhone();
		String bankName = prp.getDebitBankName();
		String idCard = prp.getIdCard();
		String rate = prp.getRate();
		String ex = prp.getExtraFee();

		JFXRegister jfxRegister = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfxRegister.getMerchantNo();
		String rateOne = jfxRegister.getRateOne();
		String rateTwo = jfxRegister.getRateTwo();
		String rateCode = "";
		if (rateOne == null) {
			rateCode = "101001"; //  
		} else if(rateTwo == null){
			rateCode = "101002";
		}
		LOG.info("费率编码：" + rateCode + "-----------------------------------");

		String url = requestURL + "701004";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_OF_NP", rate);
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "701004");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);
		map.put("changeType", "2");// 1 交易费率变更 2 交易费率新增 3 提现费率变更
		map.put("bankAccountNo", cardNo);// 银行卡卡号(结算卡) 银行卡信息变更时必填
		map.put("phoneno", phoneD);
		map.put("bankName", bankName);
		map.put("bankSubName", "上海宝山支行");
		map.put("bankCode", bankCode);// 银行代码 有参见表
		map.put("bankAbbr", bankAbbr);// 银行代码 有参见表
		map.put("bankChannelNo", bankChannelNo);// 支行联行号 有参见支行表
		map.put("bankProvince", "2900");// 开户行省份 参见省市数据格式
		map.put("bankCity", "2916");// 参见省市数据格式
		map.put("rateCode", rateCode);// 费率编码
		map.put("debitRate", "0.006");// 借记卡费率 如：千分之五
		map.put("debitCapAmount", "100000000");// 借记卡封顶 单位分
		map.put("creditRate", "0.006");// 信用卡费率 如：千分之五
		map.put("creditCapAmount", "100000000");// 信用卡封顶 单位分
		map.put("withdrawDepositRate", "0");// 提现费率 如千分之五
		map.put("withdrawDepositSingleFee", getNumber(ex));// 单笔提现手续费 单位分

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

		LOG.info("============ 即富新增费率请求地址:" + url);

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
				//保存费率及手续费
				if (rateOne == null) {
					LOG.info("新增101001费率---成功：" + message);
					jfxRegister.setExtraFeeOne(ex);
					jfxRegister.setRateOne("0.006");
					topupPayChannelBusiness.createJFXRegister(jfxRegister);
				} else if(rateTwo == null){
					LOG.info("新增101002费率---成功：" + message);
					jfxRegister.setExtraFeeTwo(ex);
					jfxRegister.setRateTwo("0.006");
					topupPayChannelBusiness.createJFXRegister(jfxRegister);
				}

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("新增费率---异常：" + message);
				
				this.addOrderCauseOfFailure(orderCode, message, rip);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);	
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/changeRate")
	public @ResponseBody Object changeRate(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("========= 进入修改费率接口 ========");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String cardNo = prp.getDebitCardNo();
		String phoneD = prp.getDebitPhone();
		String cardName = prp.getDebitBankName();
		String idCard = prp.getIdCard();
		String rate = prp.getRate();
		String ex = prp.getExtraFee();
		String bankName = prp.getCreditCardBankName();

		String url = requestURL + "701004";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> rateList = new HashMap<String, Object>();
		rateList.put("QUICKPAY_OF_NP", rate);
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "701004");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		JFXRegister jfxRegister = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfxRegister.getMerchantNo();

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);
		map.put("changeType", "1");// 1 交易费率变更 2 交易费率新增 3 提现费率变更
		map.put("bankAccountNo", cardNo);// 银行卡卡号(结算卡) 银行卡信息变更时必填
		map.put("phoneno", phoneD);
		map.put("bankName", cardName);
		map.put("bankSubName", "上海宝山支行");
		map.put("bankCode", bankCode);// 银行代码 有参见表
		map.put("bankAbbr", bankAbbr);// 银行代码 有参见表
		map.put("bankChannelNo", bankChannelNo);// 支行联行号 有参见支行表
		map.put("bankProvince", "2900");// 开户行省份 参见省市数据格式
		map.put("bankCity", "2916");// 参见省市数据格式
		map.put("rateCode", rateCode);// 费率编码
		map.put("debitRate", rate);// 借记卡费率 如：千分之五
		map.put("debitCapAmount", "100000000");// 借记卡封顶 单位分
		map.put("creditRate", rate);// 信用卡费率 如：千分之五
		map.put("creditCapAmount", "100000000");// 信用卡封顶 单位分
		map.put("withdrawDepositRate", "0");// 提现费率 如千分之五
		map.put("withdrawDepositSingleFee", getNumber(ex));// 单笔提现手续费 单位分

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

		LOG.info("============ 即富修改费率请求地址:" + url);

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
				LOG.info("修改101002费率/手续费/结算卡成功-----------------");
				jfxRegister.setBankCard(cardNo);
				jfxRegister.setExtraFeeTwo(ex);
				jfxRegister.setRateTwo(rate);
				jfxRegister.setCreateTime(new Date());
				topupPayChannelBusiness.createJFXRegister(jfxRegister);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("修改费率,结算卡,手续费---异常：" + message);
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/fast-pay")
	public @ResponseBody Object fastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode) {

		LOG.info("--------  进入快捷支付  ---------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String securityCode = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();

		String url = requestURL + "702001";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();

		RestTemplate rt = new RestTemplate();
		String urls = prp.getIpAddress() + "/v1.0/notice/sms/vericode?phone=" + prp.getCreditCardPhone();
		String resultStr = rt.getForObject(urls, String.class);
		net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(resultStr);
		String code = jsonObject.getString("result");
		LOG.info("发送码：" + smsCode + "===校验码：" + code);

		if (!smsCode.equals(code)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
			maps.put(CommonConstants.RESP_MESSAGE, "验证码输入不正确,请仔细核对重新输入!");

			this.addOrderCauseOfFailure(orderCode, "验证码输入不正确,请仔细核对重新输入!", prp.getIpAddress());

			return maps;
		}

		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702001");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		JFXRegister jfxRegister = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfxRegister.getMerchantNo();

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		// 业务参数
		map.put("token", token);
		map.put("consumeOrderId", orderCode);
		map.put("platMerchantCode", MerchantNo);
		map.put("smsCode", smsCode);
		map.put("merchantRateCode", rateCode);// 商户费率编号 渠道编码
		map.put("payAmount", getNumber(amount));// 单位分
		map.put("accountName", userName);
		map.put("cardNo", bankCard);
		map.put("cardType", "C");// C 信用卡 D 借记卡
		map.put("certType", "ID");// ID身份证
		map.put("certNo", idCard);
		map.put("phoneno", phoneC);
		map.put("bankCode", bankCode);
		map.put("bankAbbr", bankAbbr);
		map.put("productName", "休闲鞋");
		map.put("productDesc", "流行鞋/包");
		map.put("callBackUrl", ip + "/v1.0/paymentgateway/topup/jft/pay/call-back?consumeOrderId=" + orderCode);
		map.put("cvv", securityCode);// 
		
		String validity = this.expiredTimeToYYMM(expiredTime);
		
		map.put("validity", validity);// YYMM

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

		LOG.info("============ 即富确认支付请求地址:" + url);

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
				LOG.info("快捷支付------异步状态为准-----------------");
				LOG.info("------------------跳转到进入快捷支付查询-----------------");
				
				Map<String, Object> queryFast = (Map<String, Object>)queryFastPay(orderCode);

				String queryFastMsg = (String) tokenMap.get("resp_message");
				String queryFastCode = (String) queryFast.get("resp_code");
				if ("000000".equals(queryFastCode)) {
					LOG.info("查询支付成功：" + queryFastMsg);
					
					maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					return maps;
				} else {
					LOG.info("查询支付失败：" + queryFastMsg);

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, queryFastMsg);
					return maps;
				}	
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
	 * 支付提现
	 * 
	 * @param orderCode
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/putPay")
	public @ResponseBody Object putPay(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("-------- 进入支付提现  ---------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String userName = prp.getUserName();
		String cardNo = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();
		String realAmount = prp.getRealAmount();
		String extraFee = prp.getExtraFee();
		
		String amount = String.valueOf(Double.valueOf(extraFee)+Double.valueOf(realAmount));
		LOG.info("提现金额不包含手续费：" + amount + "--------------------------");

		String url = requestURL + "702002";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702002");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);
		map.put("withdrawOrderId", orderCode);
		map.put("walletType", "400");// 402、快捷支付T1钱包 400、快捷支付D0钱包
		map.put("amount", getNumber(amount));// 单位分
		map.put("bankAccountName", userName);
		map.put("bankAccountNo", cardNo);
		map.put("bankAccountType", "PRIVATE");
		map.put("bankName", bankName);
		map.put("bankSubName", "上海宝山区支行");
		map.put("bankChannelNo", bankChannelNo);
		map.put("bankCode", bankCode);
		map.put("bankAbbr", bankAbbr);
		map.put("bankProvince", "2900");
		map.put("bankCity", "2916");
		map.put("bankArea", "4");

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

		LOG.info("============ 即富进入支付提现地址:" + url);

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
			LOG.info("提现订单号：" + orderCode);
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String Url = null;
			String Result = null;
			if ("000000".equals(respCode)) {
				LOG.info("支付提现---成功：" + message +",提现订单号：" + orderCode);
				
				Url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//Url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					Result = restTemplate.postForObject(Url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				
				LOG.info("修改订单状态成功---------：" + orderCode);

				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("支付提现---异常：" + message);

				this.addOrderCauseOfFailure(orderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}
	
	/**
	 *  手动参数,对T1单个支付提现
	 * 
	 * @param orderCode
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/putPayT1")
	public @ResponseBody Object putPayT1(@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "cardNo") String cardNo,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "payOrderCode") String payOrderCode) {

		LOG.info("-------- 进入手动参数T1(当天未提现的，T1钱包提现)支付提现  ---------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(payOrderCode);
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		
		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();
		
		String url = requestURL + "702002";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702002");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		/*// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();*/

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);
		map.put("withdrawOrderId", orderCode);
		map.put("walletType", "402");// 402、快捷支付T1钱包 400、快捷支付D0钱包   隔了一天的钱T1提现
		map.put("amount", getNumber(amount));// 单位分
		map.put("bankAccountName", userName);
		map.put("bankAccountNo", cardNo);
		map.put("bankAccountType", "PRIVATE");
		map.put("bankName", bankName);
		map.put("bankSubName", "上海宝山区支行");
		map.put("bankChannelNo", bankChannelNo);
		map.put("bankCode", bankCode);
		map.put("bankAbbr", bankAbbr);
		map.put("bankProvince", "2900");
		map.put("bankCity", "2916");
		map.put("bankArea", "4");

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

		LOG.info("============ 即富进入支付提现地址:" + url);

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
			LOG.info("提现订单号：" + orderCode);
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String Url = null;
			String Result = null;
			if ("000000".equals(respCode)) {
				LOG.info("支付提现---成功：" + message +",提现订单号：" + orderCode);
				
				Url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//Url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", payOrderCode);
				requestEntity.add("third_code", orderCode);
				try {
					Result = restTemplate.postForObject(Url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				
				LOG.info("修改订单状态成功---------" + payOrderCode);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("支付提现---异常：" + message);

				this.addOrderCauseOfFailure(payOrderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}
	
	/**
	 *  手动参数,D0钱包单个支付提现
	 * 
	 * @param orderCode
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/putPayD0")
	public @ResponseBody Object putPayD0(@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "cardNo") String cardNo,
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "payOrderCode") String payOrderCode) {

		LOG.info("-------- 进入手动参数D0(当天未提现的，T1钱包提现)支付提现  ---------");
		

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(payOrderCode);
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		
		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();
		
		String url = requestURL + "702002";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702002");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		String bankAbbr = bcode.getBankCode();// 缩写
		String bankCode = bcode.getBankNum();// 编号

		/*// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();*/

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);
		map.put("withdrawOrderId", orderCode);
		map.put("walletType", "400");// 402、快捷支付T1钱包 400、快捷支付D0钱包   隔了一天的钱T1提现
		map.put("amount", getNumber(amount));// 单位分
		map.put("bankAccountName", userName);
		map.put("bankAccountNo", cardNo);
		map.put("bankAccountType", "PRIVATE");
		map.put("bankName", bankName);
		map.put("bankSubName", "上海宝山区支行");
		map.put("bankChannelNo", bankChannelNo);
		map.put("bankCode", bankCode);
		map.put("bankAbbr", bankAbbr);
		map.put("bankProvince", "2900");
		map.put("bankCity", "2916");
		map.put("bankArea", "4");

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

		LOG.info("============ 即富进入支付提现地址:" + url);

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
			LOG.info("提现订单号：" + orderCode);
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String Url = null;
			String Result = null;
			if ("000000".equals(respCode)) {
				LOG.info("支付提现---成功：" + message +",提现订单号：" + orderCode);

				Url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//Url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", payOrderCode);
				requestEntity.add("third_code", orderCode);// 手动提现订单号
				try {
					Result = restTemplate.postForObject(Url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				
				LOG.info("修改订单状态成功---------" + payOrderCode);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("支付提现---异常：" + message);

				this.addOrderCauseOfFailure(payOrderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}
	
	/**
	 * 余额查询  
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/queryBalance")
	public @ResponseBody Object queryBalance(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("-------- 进入余额查询  ---------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		
		String url = requestURL + "703001";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "703001");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}
		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();

		// 业务参数
		map.put("token", token);
		map.put("platMerchantCode", MerchantNo);

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

		LOG.info("============ 即富进入余额查询地址:" + url);

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
			String quickPayWalletBalance = jsonobj.getString("quickPayWalletBalance");
			String quickPayD0WalletWithdrawBalance = jsonobj.getString("quickPayD0WalletWithdrawBalance");
			String quickPayT1WalletWithdrawBalance = jsonobj.getString("quickPayT1WalletWithdrawBalance");
			if ("000000".equals(respCode)) {
				LOG.info("查询余额---成功：" + message +",商户钱包余额：" + quickPayWalletBalance 
						+ ",D0钱包可提现余额：" + quickPayD0WalletWithdrawBalance
						+ ",T1钱包可提现余额：" + quickPayT1WalletWithdrawBalance);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "查询余额：" + message +",商户钱包余额：" + quickPayWalletBalance 
						+ ",D0钱包可提现余额：" + quickPayD0WalletWithdrawBalance
						+ ",T1钱包可提现余额：" + quickPayT1WalletWithdrawBalance);
			} else {
				LOG.info("查询余额---异常：" + message);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/queryFastPay")
	public @ResponseBody Object queryFastPay(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("-------- 进入支付状态查询  ---------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		
		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();

		String url = requestURL + "702010";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702010");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}
		// 业务参数
		map.put("token", token);
		map.put("consumeOrderId", orderCode);
		map.put("merchantCode", MerchantNo);

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

		LOG.info("============ 即富支付状态查询请求地址:" + url);

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
			String orderStatus = jsonobj.getString("orderStatus");// 04-支付中  01-支付成功  02-支付失败
			System.out.println(orderStatus+"----------------------");
			if ("000000".equals(respCode) && "01".equals(orderStatus)) {
				LOG.info("查询支付---成功：" + orderStatus + "：04-支付中  01-支付成功  02-支付失败");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, orderStatus);
			}else if("04".equals(orderStatus)){
				LOG.info("查询支付---支付中：" + orderStatus + "：04-支付中  01-支付成功  02-支付失败");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, orderStatus);
			} else {
				LOG.info("查询支付---异常：" + orderStatus+ "：04-支付中  01-支付成功  02-支付失败");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, orderStatus);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}

	/**
	 * 提现状态查询
	 * 
	 * @param orderCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/queryPutPay")
	public @ResponseBody Object queryPutPay(@RequestParam(value = "orderCode") String orderCode) {

		LOG.info("-------- 进入提现状态查询  ---------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		
		// 获取平台商户号
		JFXRegister jfx = topupPayChannelBusiness.getJFXRegisterByIdCard(idCard);
		String MerchantNo = jfx.getMerchantNo();

		String url = requestURL + "702020";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "702020");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

		Map<String, Object> tokenMap = (Map<String, Object>) getToken();

		String token = "";
		String respcode = (String) tokenMap.get("resp_code");
		if ("000000".equals(respcode)) {
			token = (String) tokenMap.get("resp_message");

			LOG.info("获取token成功：" + token);
		} else {
			String msg = (String) tokenMap.get("resp_message");

			LOG.info("获取token失败：" + msg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, msg);
			return maps;
		}

		// 业务参数
		map.put("token", token);
		map.put("queryType", "2");// 查询类型 2提现的订单号
		map.put("withDrawOrderId", orderCode);
		map.put("platMerchantCode", MerchantNo);

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

		LOG.info("============ 即富查询提现请求地址:" + url);

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
			String drawStatus = jsonobj.getString("drawStatus");
			if ("000000".equals(respCode) && "01".equals(drawStatus)) {
				LOG.info("查提现---成功：" + message);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			} else {
				LOG.info("查提现---异常：" + message);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return maps;
	}

	/**
	 * 获取token
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jft/getToken")
	public @ResponseBody Object getToken() {

		LOG.info("============ 进入获取token ============");

		String url = requestURL + "700001";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		// 公共参数
		String orderId = getRandom();
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "700001");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);

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

		LOG.info("============ 即富获取token请求地址:" + url);

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
			String token = jsonobj.getString("token");
			String rsHead = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(rsHead);
			String respCode = headJson.getString("respCode");
			String respMsg = headJson.getString("respMsg");
			LOG.info("token:" + token);
			LOG.info("获取token返回信息:" + respMsg);
			if ("000000".equals(respCode)) {
				LOG.info("获取token成功：===================" + token);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, token);
			} else {
				LOG.info("获取token异常：===================" + token);

				// this.addOrderCauseOfFailure(orderCode, message, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jft/jump-bindcard-view")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("jftBindCard------------------跳转到绑卡界面");

		String ordercode = request.getParameter("ordercode");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardType = prp.getCreditCardCardType();
		String securityCode = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ip);

		return "jftbindcard";
	}

	/**
	 * 跳转到交易界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jft/pay-view")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("jftPay------------------跳转到交易界面");

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

		return "jftpay";
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
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/jft/pay/call-back")
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

		if (com.google.common.base.Objects.equal(signature, checkSign)) {
			LOG.error("签名验证成功");
			JSONObject jsonobj = JSONObject.parseObject(dataPlain);
			String head = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(head);
			String orderId = headJson.getString("orderId");
			LOG.info("第三方查询流水号：" + orderId);
			String orderStatus = jsonobj.getString("orderStatus");
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			if ("01".equals(orderStatus)) {
				LOG.info("*********************交易成功***********************");
				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String URL = null;
				String results = null;
				URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/thirdordercode";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", orderId);
				try {
					results = restTemplate.postForObject(URL, requestEntity, String.class);
					LOG.info("*********************下单成功，添加第三方流水号***********************");
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + results);
				
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
				
				LOG.info("订单交易成功,等待支付提现!");
				
				Map<String, Object> putMap = (Map<String, Object>) putPay(orderCode);

				String rcode = (String) putMap.get("resp_code");
				String putMessage = (String) putMap.get("resp_message");
				if ("000000".equals(rcode)) {

					LOG.info("支付提现成功：" + putMessage);
				} else {

					LOG.info("支付提现失败：" + putMessage);
				}	

			}

		} else {
			LOG.error("签名验证失败");
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
