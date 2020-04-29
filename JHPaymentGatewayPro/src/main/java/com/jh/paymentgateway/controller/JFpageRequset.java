package com.jh.paymentgateway.controller;

import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.Area;
import com.jh.paymentgateway.pojo.JFBindCard;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.jf.AES;
import com.jh.paymentgateway.util.jf.Base64;
import com.jh.paymentgateway.util.jf.HttpClient4Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


@SuppressWarnings("ALL")
@Controller
@EnableAutoConfiguration
public class JFpageRequset extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(JFpageRequset.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static String key = "EBBB88D551BE13CBCAB9231C611CCD04";
	private static String partnerNo = "001BYTH5";
	private static String requestURL = "https://fast.jfpays.com:19000/api/v1/";
	private static String DATA_KEY = "";
	private static String SIGN_KEY = "";

	// 进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/register")
	public @ResponseBody Object getRegister(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);


		String ordercode = prp.getOrderCode();
		String bankCard = prp.getBankCard();
		String bankName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String idCard = prp.getIdCard();
		String orderType = prp.getOrderType();
		String userId = prp.getUserId();
		String cardtype = prp.getCreditCardCardType();
		String bankNo = prp.getDebitCardNo();
		String cardType = prp.getDebitCardCardType();
		String cardName = prp.getCreditCardBankName();
		String amount = prp.getAmount();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		Map<String, String> maps = new HashMap<>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		LOG.info(ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
				+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
				+ "&bankCard=" + bankCard
				+ "&ordercode=" + ordercode
				+ "&amount=" + amount
				+ "&expiredTime=" + expiredTime
				+ "&securityCode=" + securityCode
				+ "&phone=" + phoneC
				+"&ipAddress=" + ip);
		maps.put(CommonConstants.RESULT,
				ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
						+ "&bankCard=" + bankCard
						+ "&ordercode=" + ordercode
						+ "&amount=" + amount
						+ "&expiredTime=" + expiredTime
						+ "&securityCode=" + securityCode
						+ "&phone=" + phoneC
						+"&ipAddress=" + ip);
		return maps;
		/*String ordercode = prp.getOrderCode();
		String bankCard = prp.getBankCard();
		String bankName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String idCard = prp.getIdCard();
		String orderType = prp.getOrderType();
		String userId = prp.getUserId();
		String cardtype = prp.getCreditCardCardType();
		String bankNo = prp.getDebitCardNo();
		String cardType = prp.getDebitCardCardType();
		String cardName = prp.getCreditCardBankName();
		String amount = prp.getAmount();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		if (bcode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "该到账卡银行暂不支持!");
		}
		String bankUnitNo = bcode.getBankBranchcode();

		Map<String, Object> maps = new HashMap<String, Object>();
		JFRegister jfR = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
		JFBindCard jfB = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
		if (cardName.contains("中国银行")) {

			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;

			}

		} else if (cardName.contains("邮政")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为4000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为4000以内,请核对重新输入金额!", rip);

				return maps;

			}
		} else if (cardName.contains("招商")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "招商银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "招商银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("光大 ")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("华夏")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "华夏银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "华夏银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("北京")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("上海")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "上海银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "上海银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("江苏")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "江苏银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "江苏银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		}
		if (jfR == null) {
			LOG.info("===================第一次进件===========================");
			String url = requestURL + "610001";
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> head = new HashMap<String, Object>();
			Map<String, Object> rateList = new HashMap<String, Object>();
			rateList.put("QUICKPAY_OF_NP", rate);
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
			// 业务参数
			map.put("merchantCode", userId);
			map.put("merName", "上海莘丽");
			map.put("merShortName", "富贵商城");
			map.put("bankAccountName", userName);
			map.put("idCardNo", idCard);
			map.put("phoneno", phoneD);
			map.put("merAddress", "上海宝山区长江南路华滋奔腾控股集团1号楼309");
			map.put("bankAccountNo", bankNo);
			map.put("bankUnitNo", bankUnitNo);// 联行号
			map.put("bankName", bankName);
			map.put("productList", rateList);
			map.put("province", "530000");
			map.put("city", "530400");
			map.put("withdrawDepositSingleFee", getNumber(ExtraFee));

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
				String platMerchantCode = jsonobj.getString("platMerchantCode");
				String headJson = jsonobj.getString("head");
				JSONObject MessageJson = JSONObject.parseObject(headJson);
				String message = MessageJson.getString("8");
				LOG.info("返回平台商户号：" + platMerchantCode);
				LOG.info("返回描述：" + message);
				if (!"".equals(platMerchantCode) && platMerchantCode != null) {
					JFRegister jfRegister = new JFRegister();
					jfRegister.setBankCard(bankNo);
					jfRegister.setExtraFee(ExtraFee);
					jfRegister.setRate(rate);
					jfRegister.setMerchantNo(platMerchantCode);
					jfRegister.setIdCard(idCard);
					jfRegister.setPhone(phoneD);
					topupPayChannelBusiness.createJFRegister(jfRegister);
					maps = (Map<String, Object>) this.OpenSMS(ordercode, expiredTime, securityCode);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					LOG.info("即富进件---异常：" + message);
					this.addOrderCauseOfFailure(orderCode, message, rip);
					return maps;

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (jfB == null) {
			LOG.info("===================进入开通支付卡===========================");
			if (!ExtraFee.equals(jfR.getExtraFee()) | !bankNo.equals(jfR.getBankCard()) | !rate.equals(jfR.getRate())) {
				LOG.info("=====修改手续费,结算卡,费率,开通卡,去支付======");
				maps = (Map<String, Object>) modifyCard(orderCode, bankNo, phoneD, rate, extraFee, idCard);
				if ("000000".equals(maps.get("resp_code"))) {
					maps = (Map<String, Object>) this.OpenSMS(ordercode, expiredTime, securityCode);
					return maps;
				} else {
					return maps;
				}

			} else {
				LOG.info("===================进件未开支付卡,发起支付===========================");
				maps = (Map<String, Object>) this.OpenSMS(ordercode, expiredTime, securityCode);
				return maps;

			}
		} else if (jfB != null) {
			if (!ExtraFee.equals(jfR.getExtraFee()) | !bankNo.equals(jfR.getBankCard()) | !rate.equals(jfR.getRate())) {
				LOG.info("===================支付卡存在,修改手续费,结算卡,费率，后支付===========================");
				maps = (Map<String, Object>) modifyCard(orderCode, bankNo, phoneD, rate, extraFee, idCard);
				if ("000000".equals(maps.get("resp_code"))) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "成功");
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
							+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&ipAddress=" + ip);
				}
				return maps;

			} else {
				LOG.info("===================已开通卡,直接快捷支付===========================");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "成功");
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
								+ "&ordercode=" + orderCode + "&ipAddress=" + ip);
				return maps;
			}

		}*/
		/*return maps;*/


	}

	/**
	 * 跳转结算卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jf/jump-Receivablescard-view")
	public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/jf/jump-Receivablescard-view=========tojfbankinfo");
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

		return "jfbankinfo";
	}

	/**
	 * 绑卡转接
	 * 
	 * @param orderCode
	 * @param expiredTime
	 * @param securityCode
	 * @param ipAddress
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/transfer")
	public @ResponseBody Object Transfer(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "ipAddress") String ipAddress) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/jf/jump-bindcard-view?ordercode=" + orderCode
				+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
		return maps;

	}

	/**
	 * 跳转到绑卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jf/jump-bindcard-view")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/jf/jump-bindcard-view=========tojfbindcard");

		String ordercode = request.getParameter("ordercode");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardType = prp.getCreditCardCardType();

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ip);
		return "jfbindcard";
	}

	/**
	 * 开通支付卡短信6002
	 * 
	 * @return
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/open-sms")
	public @ResponseBody Object OpenSMS(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String idCard = prp.getIdCard();
		String userName = prp.getUserName();
		String phone = prp.getCreditCardPhone();
		String rip = prp.getIpAddress();
		// 获取平台商户号
		JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
		String MerchantNo = jf.getMerchantNo();

		String url = requestURL + "610002";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
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
		// 业务参数
		String openOrderId = getRandom();
		LOG.info("开卡流水号：" + openOrderId);
		map.put("openOrderId", openOrderId);
		map.put("platMerchantCode", MerchantNo);
		map.put("accountName", userName);
		map.put("cardNo", bankCard);
		map.put("certNo", idCard);
		map.put("phoneno", phone);
		map.put("cvn2", securityCode);
		map.put("expired", expiredTime);
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

		LOG.info("============ 即富开卡短信请求地址:" + url);

		Object resultJson = null;

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
			resultJson = JSON.toJSONString(result);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String headJson = jsonobj.getString("head");
			JSONObject HJson = JSONObject.parseObject(headJson);
			String message = HJson.getString("respMsg");
			String respCode = HJson.getString("respCode");
			LOG.info("返回描述：" + message);
			LOG.info("返回状态码：" + respCode);
			if ("000000".equals(respCode)) {
				LOG.info("状态码：1");
				try {
					String openCardId = jsonobj.getString("openCardId");
					JFBindCard jfBindCard = new JFBindCard();
					jfBindCard.setBankCard(bankCard);
					jfBindCard.setBindingNum(openCardId);
					jfBindCard.setIdCard(idCard);
					jfBindCard.setPhone(phone);
					jfBindCard.setStatus("0");
					topupPayChannelBusiness.createJFBindCard(jfBindCard);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
							+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&ipAddress=" + ip);
					return maps;

				} catch (Exception e) {
					LOG.info("即富开通支付卡短信=================未返回openCardId");
				}
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("contractIds", openOrderId);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("即富开通支付卡短信---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return maps;

	}

	/**
	 * 开通支付卡
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/open-card")
	public @ResponseBody Object OpenPaymentCard(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "contractIds") String openOrderId) {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();
		String phone = prp.getCreditCardPhone();
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String rip = prp.getIpAddress();

		String url = requestURL + "610003";
		Map<String, Object> maps = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		// 公共参数
		String orderId = getRandom();

		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		head.put("txnCode", "610003");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);
		// 业务参数
		map.put("openOrderId", openOrderId);
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

		LOG.info("============ 即富确认开卡请求地址:" + url);

		Object resultJson = null;

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
			resultJson = JSON.toJSONString(result);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String openCardId = jsonobj.getString("openCardId");
			String rsHead = jsonobj.getString("head");

			JSONObject headJson = JSONObject.parseObject(rsHead);
			String message = headJson.getString("respMsg");
			String respCode = headJson.getString("respCode");
			LOG.info("机构绑卡序号：" + openCardId);
			if ("000000".equals(respCode)) {
				JFBindCard jfBindCard = new JFBindCard();
				jfBindCard.setBankCard(bankCard);
				jfBindCard.setBindingNum(openCardId);
				jfBindCard.setIdCard(idCard);
				jfBindCard.setPhone(phone);
				jfBindCard.setStatus("0");
				topupPayChannelBusiness.createJFBindCard(jfBindCard);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("redirect_url",
						ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
								+ "&ordercode=" + orderCode + "&ipAddress=" + ip);

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("即富开通支付卡---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return maps;

	}

	/**
	 * 支付页面转接
	 * 
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/pay-transfer")
	public @ResponseBody Object PayTransfer(@RequestParam(value = "ordercode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String phone = prp.getPhone();
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		LOG.info(ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
				+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
				+ orderCode + "&ipAddress=" + ip+ "&phone=" + phone);
		maps.put("redirect_url",
				ip + "/v1.0/paymentgateway/quick/jf/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&ipAddress=" + ip+ "&phone=" + phone);
		return maps;

	}

	/**
	 * 跳转支付页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jf/pay-view")
	public String returnHLJCQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("ordercode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String amount = request.getParameter("amount");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("amount", amount);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);

		return "jfnewquickpay";
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/pay-sms")
	public @ResponseBody Object paySMS(@RequestParam(value = "ordercode") String orderCode) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<String, Object>();
		// 获取平台商户号
		JFRegister jf = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
		String MerchantNo = jf.getMerchantNo();
		String rate = jf.getRate();
		// 获取开卡机构号
		JFBindCard jfBindCard = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
		String openCardId = jfBindCard.getBindingNum();

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
		map.put("productCode", "QUICKPAY_OF_NP");
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/jf/pay/call-back?consumeOrderId=" + orderCode);

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

		LOG.info("============ 即富支付短信请求地址:" + url);

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

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/tohtml")
	public @ResponseBody Object toHtml(@RequestParam(value = "ipAddress") String ipAddress,
									   @RequestParam(value = "orderCode") String orderCode,
									   @RequestParam(value = "expiredTime") String expiredTime,
									   @RequestParam(value = "securityCode") String securityCode,
									   @RequestParam(value = "phone") String phone) throws UnsupportedEncodingException {
		Map<String, Object> maps = new HashMap<>();
		LOG.info("进来了么？？？===========ipAddress："+ipAddress +"orderCode:" + orderCode +"expiredTime:" +expiredTime +"securityCode:" + securityCode + "phone:" + phone);
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "直接跳转三级联动页面");
		LOG.info( ip + "/v1.0/paymentgateway/quick/jf/go?ipAddress=" + ipAddress
				+ "&orderCode=" + orderCode
				+ "&expiredTime=" + expiredTime
				+ "&securityCode=" + securityCode
				+ "&phone=" + phone);
		maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/jf/go?ipAddress=" + ipAddress
				+ "&orderCode=" + orderCode
				+ "&expiredTime=" + expiredTime
				+ "&securityCode=" + securityCode
				+ "&phone=" + phone);
		return maps;
	}


	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jf/go")
	public String go(HttpServletRequest request, HttpServletResponse response, Model model) throws UnsupportedEncodingException {
		LOG.info("/v1.0/paymentgateway/quick/jf/go   进来了");
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String ipAddress = request.getParameter("ipAddress");
		String ordercode = request.getParameter("orderCode");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");

		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("orderCode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);

		LOG.info("==========ipAddress:" + ipAddress + "orderCode:" + ordercode + "expiredTime" + expiredTime + "securityCode:" + securityCode + "phone:" + phone);

		return "jflinkage";
	}

	/**
	 * 快捷支付
	 * 
	 * @param orderCode
	 * @param workId
	 * @param smsCode
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/fast-pay")
	public @ResponseBody Object fastPay(@RequestParam(value = "orderCode") String orderCode,
										@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime,
										@RequestParam(value = "provinceCode") String province,
										@RequestParam(value = "cityCode") String city,
										@RequestParam(value = "areaCode") String address,
										@RequestParam(value = "phone") String phone) {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		String bankCard = prp.getBankCard();
		//String phone = prp.getPhone();
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String debitCardNo = prp.getDebitCardNo();
		String debitPhone = prp.getDebitPhone();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String orderType = prp.getOrderType();

		/*String url = requestURL + "order";
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> head = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String orderId = getRandom();
		// 公共参数
		head.put("version", "1.0.0");
		head.put("charset", UTF_8);
		head.put("partnerNo", partnerNo);
		//head.put("txnCode", "610005");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);
		// 业务参数
		//map.put("workId", workId);
		//map.put("smsCode", smsCode);
		map.put("payAmount", Double.valueOf(amount) * 100);
		map.put("payCardNo", bankCard);
		map.put("mobile", phone);
		map.put("cvv", securityCode);
		LOG.info("expire:==============" + expiredTime);
		map.put("expire", expiredTime);
		map.put("name", userName);
		map.put("idCard", idCard);
		map.put("inBankCardNo", debitCardNo);
		map.put("inMobile", debitPhone);

		LOG.info("province:==========" + province);
		LOG.info("city:==========" + city);
		LOG.info("address:==========" + address);
		map.put("province", province);
		map.put("city", city);
		map.put("address", address);


		map.put("txnRate", rate);



		LOG.info("drawFee:==========" + drawFee);

		map.put("drawFee", drawFee);

		map.put("frontUrl",ip + "/v1.0/paymentgateway/topup/jf/pay/success");

		LOG.info("notifyUrl:=========" + ip + "/v1.0/paymentgateway/topup/jf/pay/call-back");
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/jf/pay/call-back");
		map.put("remark", "提现");
		map.put("ip", ip);*/
		//String orderId = getRandom();
		Double drawFee = Double.valueOf(extraFee) * 100;

		JSONObject dto = new JSONObject();
		dto.put("payAmount",  Double.valueOf(amount) * 100);
		dto.put("payCardNo", bankCard);
		dto.put("mobile", phone);
		dto.put("name", userName);
		dto.put("idCard", idCard);
		dto.put("inBankCardNo", debitCardNo);
		dto.put("inMobile", debitPhone);
		dto.put("txnRate", rate);
		dto.put("drawFee", drawFee);
		dto.put("frontUrl", ip + "/v1.0/paymentgateway/topup/jf/pay/success");
		dto.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/jf/pay/call-back");
		dto.put("remark", "付款");
		dto.put("province", province);
		dto.put("city", city);
		dto.put("address", address);
		dto.put("cvv", securityCode);
		dto.put("expire", expiredTime);
		//处理ip传参格式问题
		String[] ips=ip.split("//");
		String ipDto=ips[1];
		dto.put("ip", ipDto);
		LOG.info("即富快捷确认支付参数========="+dto);
		String path = getPath("order", orderCode, dto);
		LOG.info("============ 即富快捷确认支付请求地址:" + path);

		/*map.put("subMerchantId", amount);
		map.put("mcc", amount);
		map.put("area", amount);*/

		// 发送
		/*String jsonStr = JSON.toJSONString(map);
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

		LOG.info("============ 即富快捷确认支付请求地址:" + url);*/
		Map<String,Object> resultMap = new HashMap<>();

		resultMap.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		resultMap.put(CommonConstants.RESP_MESSAGE,"请求成功");
		/*String path = url
				+ "?encryptData=" + encryptData
				+ "&signData=" + signData
				+ "&orderId=" + orderId
				+ "&partnerNo=" + partnerNo
				+ "&ext=" + "";
		LOG.info("============ 即富快捷确认支付请求地址:" + path);*/
		resultMap.put("redirect_url",path);
		//LOG.info("maps======"+resultMap.toString());
		return resultMap;

		/*try {
			byte[] resByte = HttpClient4Util.getInstance().doPost(url, null, params);
			if (resByte == null) {
				resultMap.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
				resultMap.put(CommonConstants.RESP_MESSAGE,"请求超时");
			}
			String resStr = new String(resByte, UTF_8);
			System.out.println("============ 返回报文原文:" + resStr);
			resultMap.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			resultMap.put(CommonConstants.RESP_MESSAGE,"返回支付页面");
			resultMap.put(CommonConstants.RESULT,resStr);

			*//*String resStr = new String(resByte, UTF_8);
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
				maps.put(CommonConstants.RESP_MESSAGE, message);
				maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/topaysuccess?orderCode=" + orderCode
						+ "&bankName=" + URLEncoder.encode(prp.getCreditCardBankName(), "UTF-8") + "&bankCard="
						+ prp.getBankCard() + "&amount=" + prp.getAmount() + "&realAmount=" + prp.getRealAmount());

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				LOG.info("快捷支付---异常：" + message);
				this.addOrderCauseOfFailure(orderCode, message, rip);
			}*//*

		} catch (Exception e) {
			resultMap.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			resultMap.put(CommonConstants.RESP_MESSAGE,"网络异常，请稍后再试！");
		}*/


	}

	/**
	 * 修改结算卡，费率，手续费
	 * 
	 * @param orderCode
	 * @param workId
	 * @param smsCode
	 * @return
	 */
	public Object modifyCard(String orderCode, String bankCard, String phone, String rate, String ex, String idCard) {

		JFRegister jfRegister = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);
		String MerchantNo = jfRegister.getMerchantNo();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String url = requestURL + "610006";
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
		head.put("txnCode", "610006");
		head.put("orderId", orderId);// 18-32位纯数字
		head.put("reqDate", TimeFormat("yyyyMMdd"));// yyyyMMdd
		head.put("reqTime", TimeFormat("yyyyMMddHHmmss"));// yyyyMMddHHmmss
		map.put("head", head);
		// 业务参数
		map.put("platMerchantCode", MerchantNo);
		map.put("bankAccountNo", bankCard);
		map.put("phoneno", phone);
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

		LOG.info("============ 即富结算卡修改请求地址:" + url);

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
				jfRegister.setBankCard(bankCard);
				jfRegister.setExtraFee(ex);
				jfRegister.setRate(rate);
				jfRegister.setCreateTime(new Date());
				topupPayChannelBusiness.createJFRegister(jfRegister);
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
	 * 异步通知
	 * 
	 * @param encryptData
	 * @param signature
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/jf/pay/call-back")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String encryptData = request.getParameter("encryptData");
		String signature = request.getParameter("signature");


		LOG.info("消费回调：data: {} ", encryptData, signature);
		String dataPlain = AES.decode(org.apache.commons.codec.binary.Base64.decodeBase64(encryptData),
				key.substring(0, 16));
		String checkSign = DigestUtils.sha1Hex(dataPlain + key.substring(16));
		LOG.info("消费回调：dataPlain: {} ", dataPlain);
		LOG.info("消费回调：checkSign: {} ", checkSign);
		/*String dataPlain = AES.decode(org.apache.commons.codec.binary.Base64.decodeBase64(encryptData),
				key.substring(0, 16));
		String checkSign = DigestUtils.sha1Hex(encryptData + key.substring(16));*/
		try {
			LOG.info("消费回调：signature: {} ", signature);
			signature = URLDecoder.decode(signature, UTF_8.name());
			LOG.info("消费回调：signature urldecode: {} ", signature);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		PaymentRequestParameter prp = null;
		String orderId = null;
		if (com.google.common.base.Objects.equal(signature, checkSign)) {
			LOG.error("签名验证成功");
			JSONObject jsonobj = JSONObject.parseObject(dataPlain);
			String head = jsonobj.getString("head");
			JSONObject headJson = JSONObject.parseObject(head);
			orderId = headJson.getString("orderId");
			prp = redisUtil.getPaymentRequestParameter(orderId);
			LOG.info("第三方查询流水号：" + orderId);
			String orderStatus = jsonobj.getString("orderStatus");
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String URL = null;
			String result = null;
			if ("01".equals(orderStatus)) {
				LOG.info("*********************交易成功***********************");
				
				URL = prp.getIpAddress()+ ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				LOG.info("URL:============="+URL  + "restTemplate:========" + restTemplate);
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderId);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(URL, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + orderId + "====================" + result);

				LOG.info("订单已交易成功!");

				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();

			} else if ("02".equals(orderStatus)) {
				LOG.error("支付失败");
				this.addOrderCauseOfFailure(orderId, "支付失败", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			} else if ("03".equals(orderStatus)) {
				LOG.error("初始未支付");
				this.addOrderCauseOfFailure(orderId, "初始未支付", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			} else if ("99".equals(orderStatus)) {
				LOG.error("支付超时");
				this.addOrderCauseOfFailure(orderId, "支付超时", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			} else if ("04".equals(orderStatus)) {
				LOG.error("支付处理中");
				this.addOrderCauseOfFailure(orderId, "支付处理中，请稍后查询", prp.getIpAddress());
				PrintWriter pw = response.getWriter();
				pw.print("000000");
				pw.close();
			}

		} else {
			LOG.error("签名验证失败");
			this.addOrderCauseOfFailure(orderId, "签名验证失败", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("000000");
			pw.close();
		}
	}

	@RequestMapping(value = "/v1.0/paymentgateway/topup/jf/pay/success")
	public String toSuccess(){

		return "jfpaysuccess";
	}

	/**
	 * 查询交易
	 * 
	 * @param thirdOrderId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jf/query-transactions")
	public @ResponseBody Object QueryTransactions(@RequestParam(value = "third_order_code") String thirdOrderId) {

		String url = requestURL + "orderQuery";
		Map<String, Object> maps = new HashMap<String, Object>();

		JSONObject head1 = getHead(thirdOrderId, partnerNo);
		JSONObject dto = new JSONObject();
		dto.put("orderId", thirdOrderId);
		dto.put("head", getHead(thirdOrderId, partnerNo));

		String encryptData = org.apache.commons.codec.binary.Base64.encodeBase64String(AES.encode(dto.toJSONString(), key.substring(0, 16)));
		String signData = DigestUtils.sha1Hex(dto.toJSONString() + key.substring(16));


		Map<String, String> params = Maps.newHashMap();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", thirdOrderId);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");

		LOG.info("params : " + JSON.toJSONString(params));

		LOG.info("============ 即富查询请求地址:" + url);

		Object resultJson = null;

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
			resultJson = JSON.toJSONString(result);
			LOG.info("返回明文：" + res);
			LOG.info("返回验签结果：" + signChecked);
			LOG.info("返回源报文：" + resStr);
			JSONObject jsonobj = JSONObject.parseObject(res);
			String statusDesc = jsonobj.getString("statusDesc");
			String headJson = jsonobj.getString("head");
			JSONObject HJson = JSONObject.parseObject(headJson);
			String message = HJson.getString("respMsg");
			String respCode = HJson.getString("respCode");
			String status = jsonobj.getString("status");
			LOG.info("返回描述：" + statusDesc);
			if ("000000".equals(respCode)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, statusDesc);
				maps.put("payStatus", status);// 01成功:04处理中
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

	@RequestMapping(value = "/v1.0/paymentgateway/quick/jf/area",method = RequestMethod.GET)
	@ResponseBody
	public Object listArea(@RequestParam(value = "id")int id){
		LOG.info("三级联动======================" + id);
		Map<String, Object> map = new HashMap<>();
		List<Area> list = null;
		try {
			list = topupPayChannelBusiness.listAreaInfo(id);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE,"查询成功");
			map.put(CommonConstants.RESULT,list);
			return map;
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE,e);
			return map;
		}
	}


	protected static JSONObject getHead(String id,
										String partnerNo) {
		JSONObject head = new JSONObject();
		head.put("version", "1.0.0");
		head.put("charset", "UTF-8");
		head.put("partnerNo", partnerNo);
		head.put("orderId", id);
		head.put("reqDate", DateTime.now().toString("yyyyMMdd"));
		head.put("reqTime", DateTime.now().toString("yyyyMMddHHmmss"));
		return head;
	}

	public String getPath (String txnCode,String orderId,JSONObject dto){
		dto.put("head", getHead(orderId, partnerNo));
		String path = execute(dto.toJSONString(), orderId, txnCode, partnerNo, key);
		return path;
	}

	private static String execute(String jsonStr,
								String id,
								String txnCode,
								String partnerNo,
								String key) {
		System.out.println("请求参数：" + jsonStr);
		if (Strings.isNullOrEmpty(key)) {
			DATA_KEY = "0000000000000000";
			SIGN_KEY = "0000000000000000";
		} else {
			DATA_KEY = key.substring(0, 16);
			SIGN_KEY = key.substring(16);
		}
		String encryptData = org.apache.commons.codec.binary.Base64.encodeBase64String(AES.encode(jsonStr, DATA_KEY));
		String signData = DigestUtils.sha1Hex(jsonStr + SIGN_KEY);
		String url = requestURL + txnCode;
//        String url = "http://localhost:19085/api/v1/" + txnCode;

		Map<String, Object> params = new HashMap<>();
		params.put("encryptData", encryptData);
		params.put("signData", signData);
		params.put("orderId", id);
		params.put("partnerNo", partnerNo);
		params.put("ext", "");
		String urlParams = "";
		try {
			urlParams = "?partnerNo=" + partnerNo
					+ "&orderId=" + id
					+ "&signData=" + signData
					+ "&encryptData=" + URLEncoder.encode(encryptData, "UTF-8")
					+ "&ext=" + "o";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String urlFull = url + urlParams;
		return urlFull;
	}
}
