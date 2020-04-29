package com.jh.paymentgateway.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.print.DocFlavor.STRING;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BQXBindCard;
import com.jh.paymentgateway.pojo.BQXRegister;
import com.jh.paymentgateway.pojo.BqxCode;
import com.jh.paymentgateway.pojo.BqxMerchant;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.bqx.CertificateUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class BQXpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(BQXpageRequest.class);
	@Value("${bqx.cerpath}")
	private String cerpath;

	@Value("${bqx.jkspath}")
	private String jkspath;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	RedisUtil redisUtil;

	@Autowired
	Util util;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	private static String requestUrl = "http://47.96.160.164:8080/gatewaysite/";

	private static String password = "1126@123";
	private static PrivateKey privateKey = null;
	private static String regOrgCode = "1126";

	/**
	 * 注册商户
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String bankCard = prp.getBankCard();
		String bankName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String Rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String idCard = prp.getIdCard();
		String cardtype = prp.getCreditCardCardType();
		String bankNo = prp.getDebitCardNo();
		String cardType = prp.getDebitCardCardType();
		String cardName = prp.getCreditCardBankName();
		String amount = prp.getAmount();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<String, Object>();
		if (cardName.contains("中国银行")) {

			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;

			}

		} else if (cardName.contains("邮政")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

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

		LOG.info("开始进入博淇无卡注册接口----------");

		// 百分制
		String rate = new BigDecimal(Rate).multiply(new BigDecimal("100")).setScale(2).toString();
		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);
		BQXBindCard bqxBindCard = topupPayChannelBusiness.getBQXBindCardByBankCard(bankCard);
		if (bqxRegister == null) {// 未注册
			LOG.info("***************首次进件*****************");
			Map<String, String> req = new TreeMap();
			String requestNo = getOrderId();
			req.put("orderId", requestNo);// 交易流水号 系统唯一
			req.put("regOrgCode", regOrgCode);// 注册机构号
			req.put("settleName", userName);// 商户姓名
			req.put("settleNum", bankNo);// 商户结算卡号
			req.put("settlePhone", phoneD);// 商户结算手机号
			req.put("settleIdNum", idCard);// 商户证件号
			req.put("areaCode", "310000");// 地区号 不严谨
			req.put("transChannel", "04");// 支付类型 固定
			req.put("transRate", rate);// 商户交易费率
			req.put("withDrawRate", extraFee);// 代付费用
			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("博淇无卡进件请求参数:" + req);

			String resp = sendPost(requestUrl + "p/regist", JSON.toJSONString(req));

			LOG.info("博淇无卡进件返回参数:" + resp);
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			LOG.info("进件返回参数:" + resp);
			JSONObject json_obj = JSON.parseObject(resp);
			String orgId = json_obj.getString("orgId");// 注册返回的机构号 后期交易用
			String mchtId = json_obj.getString("mchtId");// 注册返回的商户号 后期交易用
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息

			if ("0000".equals(respCode)) {
				BQXRegister bqx = new BQXRegister();
				bqx.setBankCard(bankNo);
				bqx.setExtraFee(extraFee);
				bqx.setIdCard(idCard);
				bqx.setMerchantNo(mchtId);
				bqx.setOrgId(orgId);
				bqx.setPhone(phoneD);
				bqx.setRate(Rate);
				bqx.setUserName(userName);
				topupPayChannelBusiness.createBQXRegister(bqx);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				//跳转到结算卡页面
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
				return maps;

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				this.addOrderCauseOfFailure(orderCode, respMsg + "[" + requestNo + "]", rip);
				return maps;
			}
		} else if (!Rate.equals(bqxRegister.getRate()) || !extraFee.equals(bqxRegister.getExtraFee())
				|| !bankNo.equals(bqxRegister.getBankCard())) {
			LOG.info("***************修改费率或手续费************");
			maps = (Map<String, Object>) this.mchtModify(orderCode);
			if ("000000".equals(maps.get("resp_code"))) {
				//费率修改成功跳转到结算卡页面
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
			}
			return maps;

		} else if (bqxBindCard == null) {
			LOG.info("*******************发起预签约绑卡****************");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "开始签约绑卡");
			//跳转到结算卡页面
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");
			return maps;

		} else if ("0".equals(bqxBindCard.getStatus())) {
			LOG.info("*******************发起预签约审核****************");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "开始签约绑卡审核");
			maps.put(CommonConstants.RESULT,
					//跳转到结算卡页面
					ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");
			return maps;
		} else {
			LOG.info("******************直接发起扣款请求****************");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "直接发起支付");
			//跳转到结算卡页面
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=2");
			return maps;
		}

	}


	/**
	 * 预签约
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/beSign")
	public @ResponseBody Object beSign(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime) throws Exception {

		LOG.info("开始进入博淇无卡预签约接口--------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, String> maps = new HashMap<String, String>();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String phoneC = prp.getCreditCardPhone();
		String userName = prp.getUserName();
		String rip = prp.getIpAddress();

		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);
		BQXBindCard bqxBindCard = topupPayChannelBusiness.getBQXBindCardByBankCard(bankCard);

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		Map<String, String> req = new TreeMap();
		String requestNo = getOrderId();
		req.put("orderId", requestNo);// 交易流水号 系统唯一
		req.put("orgId", bqxRegister.getOrgId());// 机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("transTime", date);// 交易时间
		req.put("transChannel", "04");// 支付类型 固定
		req.put("acct_no", bankCard);// 交易卡号
		req.put("acct_name", userName);// 交易人姓名
		req.put("acct_phone", phoneC);// 交易人电话
		req.put("idNum", idCard);// 交易人身份证号
		req.put("acct_cvv2", securityCode);// 安全码
		req.put("acct_validdate", expiredTime);// 有效期 MMYY

		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("签约请求参数:" + req);

		String resp = sendPost(requestUrl + "c/beSign", JSON.toJSONString(req));
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);
		LOG.info("签约请求返回参数:" + resp);
		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("respMsg");// 返回信息
		if ("0000".equals(respCode)) {
			// 保存用户信息
			if (bqxBindCard != null) {
				LOG.info("预签约未审核");
			} else {
				BQXBindCard bqx = new BQXBindCard();
				bqx.setBankCard(bankCard);
				bqx.setIdCard(idCard);
				bqx.setPhone(phoneC);
				bqx.setStatus("0");
				bqx.setUserName(userName);
				topupPayChannelBusiness.createBQXBindCard(bqx);
			}
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			this.addOrderCauseOfFailure(orderCode, respMsg + "[" + requestNo + "]", rip);

		}
		return maps;
	}

	/**
	 * 签约
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/sign")
	public @ResponseBody Object sign(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {
		LOG.info("开始进入博淇无卡签约接口--------");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String cardtype = prp.getCreditCardCardType();
		String bankName = prp.getCreditCardBankName();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String nature = prp.getCreditCardNature();

		Map<String, String> maps = new HashMap<String, String>();

		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);
		BQXBindCard bqxBindCard = topupPayChannelBusiness.getBQXBindCardByBankCard(bankCard);

		Map<String, String> req = new TreeMap();
		String requestNo = getOrderId();
		req.put("orderId", requestNo);// 交易流水号 系统唯一
		req.put("orgId", bqxRegister.getOrgId());// 机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("acct_no", bankCard);// 交易卡号
		req.put("verify_code", smsCode);// 短信验证码

		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("签约发送参数:" + req);

		String resp = sendPost(requestUrl + "c/sign", JSON.toJSONString(req));

		LOG.info("签约返回参数:" + resp);
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);

		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		if ("0000".equals(respCode)) {
			// 修改绑卡状态
			bqxBindCard.setStatus("1");// 绑卡状态 0：预签约 1：确认签约
			bqxBindCard.setChangeTime(new Date());
			topupPayChannelBusiness.createBQXBindCard(bqxBindCard);
			String url = ip + "/v1.0/paymentgateway/quick/bqx/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
					+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&nature="
					+ URLEncoder.encode(nature, "UTF-8") + "&bankCard=" + bankCard + "&ordercode=" + orderCode
					+ "&ipAddress=" + ip + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode;
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("redirect_url", url);
			LOG.info(url.toString());
			maps.put(CommonConstants.RESP_MESSAGE, "签约成功");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "签约失败");
			this.addOrderCauseOfFailure(orderCode, "签约失败" + "[" + requestNo + "]", rip);
		}
		return maps;
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view")
	public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/bqx/jump-Receivablescard-view=========tobqxbankinfo");
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

		return "bqxbankinfo";
	}

	/**
	 * 页面直跳绑卡界面
	 * 
	 * @param orderCode
	 * @param expiredTime
	 * @param securityCode
	 * @param ipAddress
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/bqx/transfer")
	public @ResponseBody
	Object Transfer(@RequestParam(value = "ordercode") String orderCode,
					@RequestParam(value = "expiredTime") String expiredTime,
			        @RequestParam(value = "securityCode") String securityCode,
			        @RequestParam(value = "ipAddress") String ipAddress) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/bqx/jump-bindcard-view?ordercode=" + orderCode
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ypl/jump-bindcard-view")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/bqx/jump-bindcard-view=========tobqxbindcard");

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
		return "bqxbindcard";
	}

	/**
	 * 页面直跳支付界面
	 * 
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/bqx/pay-transfer")
	public @ResponseBody Object PayTransfer(@RequestParam(value = "ordercode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String securityCode = prp.getSecurityCode();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String nature = prp.getCreditCardNature();
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url",
				ip + "/v1.0/paymentgateway/quick/bqx/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&nature="
						+ URLEncoder.encode(nature, "UTF-8") + "&bankCard=" + bankCard + "&ordercode=" + orderCode
						+ "&ipAddress=" + ip + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
		return maps;

	}


	/**
	 * 自选支付页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/bqx/pay-view")
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
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String nature = request.getParameter("nature");

		model.addAttribute("orderCode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("ip", "0");
		model.addAttribute("ips", "0");
		model.addAttribute("nature", "0");
		model.addAttribute("phone", "123456");
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("nature", nature);

		return "bqxkquickpay";
	}
































	/**
	 * 获取商户信息
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/topup/bqx/merchant/querybycode"))
	public @ResponseBody Object merchant() {
		LOG.info("开始进入获取商户信息接口=================================");

		Map map = new HashMap();
		List<BqxMerchant> list = topupPayChannelBusiness.findBqxMerchant();//
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 查询所有的省份、直辖市、自治区
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/topup/bqx/province/queryall"))
	public @ResponseBody Object findProvince() {
		LOG.info("开始进入查询所有的省份、直辖市、自治区接口=================================");

		Map map = new HashMap();
		List<BqxCode> list = topupPayChannelBusiness.findBqxCodeProvince();// 省份id:000000,等级:1
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	/**
	 * 查询指定区域的下级城市
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/topup/bqx/city/queryall"))
	public @ResponseBody Object findCity(@RequestParam(value = "provinceId") String provinceId) {
		LOG.info("开始进入查询指定区域的下级城市区接口=================================");

		Map map = new HashMap();
		List<BqxCode> list = topupPayChannelBusiness.findBqxCodeCity(provinceId);//
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}



	public String sign(Map<String, String> resp) {
		StringBuffer sb = new StringBuffer();
		for (String key : resp.keySet()) {
			if (resp.get(key) != null && !resp.get(key).equals(""))
				sb.append(key + "=" + resp.get(key) + "&");
		}
		String queryString = sb.substring(0, sb.length() - 1);// 构造待签名字符串
		FileInputStream fis = null;
		String sign = null;
		try {
			if (privateKey == null) {
				fis = new FileInputStream(jkspath);
				privateKey = CertificateUtils.getPrivateKey(fis, null, password);
			}
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privateKey);
			signature.update(queryString.getBytes("UTF-8"));
			sign = Base64.encodeBase64String(signature.sign());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sign;
	}

	public String sendPost(String url, String json) {
		System.out.println(json);
		System.out.println(url);
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(json.getBytes("UTF-8"));
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.print(result);
		return result;
	}

	/**
	 * 验证
	 */
	public boolean verify(TreeMap<String, String> map) throws Exception {
		InputStream in = new FileInputStream(cerpath);
		PublicKey publicKey = CertificateUtils.getPublicKey(in);
		String signature = map.remove("sign") + "";
		StringBuffer sb = new StringBuffer();
		for (Map.Entry key : map.entrySet()) {
			sb.append(key.getKey() + "=" + key.getValue() + "&");
		}
		String string = sb.substring(0, sb.length() - 1);
		Signature st = Signature.getInstance("SHA1withRSA");
		st.initVerify(publicKey);
		st.update(string.getBytes("UTF-8"));
		boolean result = st.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
		return result;
	}

	/**
	 * 14位时间戳
	 *
	 * @return
	 */
	public static String getOrderId() {
		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHH");
		String date = sd.format(new Date());
		int uuid = ((int) ((Math.random() * 9 + 1) * 100000));
		String orderId = date + String.valueOf(uuid);
		LOG.info("14位时间戳流水：" + orderId);

		return orderId;

	}

	/**
	 * 代扣异步通知
	 *
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/Withhold/notifyurl")
	public Object Withhold(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("代扣异步通知进来了-----");
		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream byteArray = null;
		byteArray = new ByteArrayOutputStream();
		byte[] dat = new byte[2048];
		int l = 0;
		while ((l = inputStream.read(dat, 0, 2048)) != -1) {
			byteArray.write(dat, 0, l);
		}
		byteArray.flush();
		LOG.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
		String info = new String(byteArray.toByteArray(), "UTF-8");
		JSONObject jsonInfo;
		try {
			jsonInfo = JSONObject.parseObject(info);
		} catch (Exception e1) {
			return null;
		}
		LOG.info("jsonInfo=============" + jsonInfo.toString());
		inputStream.close();
		byteArray.close();
		String respCode = jsonInfo.getString("origRespCode");
		String respMsg = jsonInfo.getString("origRespMsg");
		String orderId = jsonInfo.getString("origOrderId");
		LOG.info("respCode:" + respCode);
		LOG.info("respMsg:" + respMsg);
		LOG.info("orderId:" + orderId);

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderId);
		String rip = prp.getIpAddress();
		if ("0000".equals(respCode)) {
			this.addOrderCauseOfFailure(orderId, "请求成功,等待银行扣款", rip);
			LOG.info("=========发起博淇无卡代付请求=============");
			this.payfor(orderId);
		} else {
			LOG.info("=========博淇无卡扣款失败=============");
			this.addOrderCauseOfFailure(orderId, respMsg + "[扣款：" + orderId + "]", rip);
		}

		PrintWriter pw = response.getWriter();
		pw.print("0000");
		pw.close();
		return null;

	}


	/**
	 * 快捷短信申请
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/payment/ssm")
	public @ResponseBody Object createNoCardOrder(@RequestParam(value = "ordercode") String orderCode,
												  @RequestParam(value = "expiredTime") String expiredTime,
												  @RequestParam(value = "securityCode") String securityCode,
												  @RequestParam(value = "cityCode") String cityCode,
												  @RequestParam(value = "merchantCode") String merchantCode) throws Exception {
		LOG.info("开始进入博淇无卡扣款申请接口--------");
		LOG.info("==========地区码：" + cityCode);
		LOG.info("==========商户码：" + merchantCode);
		Map<String, String> maps = new HashMap<String, String>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String username = prp.getUserName();
		String phone = prp.getDebitPhone();
		String rip = prp.getIpAddress();

		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);

		// 金额 单位分
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		LOG.info("金额，单位分:" + Amount);

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		Map<String, String> req = new TreeMap();
		req.put("orderId", orderCode);// 订单号
		req.put("orgId", bqxRegister.getOrgId());// 机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("transTime", date);// 交易时间
		req.put("transChannel", "04");// 支付类型
		req.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/bqx/Withhold/notifyurl");
		req.put("tranAmt", Amount);// 交易金额
		req.put("acct_no", bankCard);// 交易卡号
		req.put("acct_name", username);// 交易人姓名
		req.put("acct_phone", phone);// 交易人电话
		req.put("idNum", idCard);// 交易人身份证号
		req.put("acct_cvv2", securityCode);// 安全码
		req.put("acct_validdate", expiredTime);// 有效期 YYMM
		if (!"".equals(cityCode) && cityCode != null) {
			req.put("transCity", cityCode);
		}
		if (!"".equals(merchantCode) && merchantCode != null) {
			req.put("mccid", merchantCode);
		}
		String sign = sign(req);
		req.put("sign", sign);
		LOG.info("博淇无卡预扣款请求参数:" + req);
		String resp = sendPost(requestUrl + "c/createNoCardOrder", JSON.toJSONString(req));
		LOG.info("博淇无卡预扣款返回参数:" + resp);
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);

		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("respMsg");// 返回信息
		if ("1001".equals(respCode)) {
			LOG.info("===================博淇无卡代扣请求成功，等待抠扣款" + orderCode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			maps.put("orderId", orderCode);

		} else {
			this.addOrderCauseOfFailure(orderCode, respMsg + "[" + orderCode + "]", rip);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);

		}
		return maps;
	}

	/**
	 * 短信上送
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/sendpayment")
	public @ResponseBody Object sendNoCardOrder(@RequestParam(value = "ordercode") String orderCode,
												@RequestParam(value = "orderId") String orderId, @RequestParam(value = "smsCode") String smsCode,
												@RequestParam(value = "expiredTime") String expiredTime) throws Exception {
		LOG.info("开始进入博淇无卡扣款上送接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();

		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		Map<String, String> req = new TreeMap();
		String requestNo = getOrderId();
		req.put("orderId", requestNo);// 请求流水号
		req.put("orgId", bqxRegister.getOrgId());// 机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("transTime", date);// 交易时间
		req.put("origOrderId", orderId);// 原交易订单号
		req.put("verify_code", smsCode);// 短信验证码
		req.put("acct_validdate", expiredTime);// 有效期 YYMM

		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("博淇无卡扣款请求参数:" + req);

		String resp = sendPost(requestUrl + "c/sendNoCardPay", JSON.toJSONString(req));
		LOG.info("博淇无卡扣款返回参数:" + resp);
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);

		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("respMsg");// 返回信息
		if ("0000".equals(respCode)) {
			maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
		} else {
			this.addOrderCauseOfFailure(orderCode, respMsg + "[" + requestNo + "]", rip);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
		}
		return maps;
	}


	/**
	 * 修改费率，手续费
	 *
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/mchtModify")
	public @ResponseBody Object mchtModify(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String Rate = prp.getRate();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String bankNo = prp.getDebitCardNo();
		String phoneD = prp.getDebitPhone();

		String userName = prp.getUserName();
		String extraFee = prp.getExtraFee();
		LOG.info("开始进入商户信息修改接口----------");
		Map<String, Object> maps = new HashMap<String, Object>();
		// 百分制
		String rate = new BigDecimal(Rate).multiply(new BigDecimal("100")).setScale(2).toString();
		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);

		Map<String, String> req = new TreeMap();
		String requestNo = getOrderId();
		req.put("orderId", requestNo);// 交易流水号 系统唯一
		req.put("regOrgCode", regOrgCode);// 注册机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("settleName", userName);// 商户姓名
		req.put("settleNum", bankNo);// 商户结算卡号
		req.put("settlePhone", phoneD);// 商户结算手机号
		req.put("settleIdNum", idCard);// 商户证件号
		req.put("areaCode", "310000");// 地区号 不严谨
		req.put("transChannel", "04");// 支付类型 固定
		req.put("transRate", rate);// 商户交易费率
		req.put("withDrawRate", extraFee);// 代付费用
		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("博淇无卡修改商户请求参数:" + req);

		String resp = sendPost(requestUrl + "p/mchtModify", JSON.toJSONString(req));
		LOG.info("博淇无卡修改商户返回参数:" + resp);
		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("respMsg");
		if ("0000".equals(respCode)) {
			bqxRegister.setRate(Rate);
			bqxRegister.setExtraFee(extraFee);
			bqxRegister.setBankCard(bankNo);
			bqxRegister.setPhone(phoneD);
			bqxRegister.setChangeTime(new Date());
			topupPayChannelBusiness.createBQXRegister(bqxRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			this.addOrderCauseOfFailure(orderCode, respMsg + "[" + requestNo + "]", rip);
			return maps;
		}
	}


	/**
	 * 查询扣款
	 *
	 * @param orderId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/querybepay")
	public @ResponseBody Object querybepay(@RequestParam(value = "orderId") String orderId,
										   @RequestParam(value = "orgId") String orgId, @RequestParam(value = "mchtId") String mchtId,
										   @RequestParam(value = "date") String date) throws Exception {
		LOG.info("开始进入博淇无卡查询扣款接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		Map<String, String> req = new TreeMap();
		req.put("orderId", getOrderId());// 请求流水号
		req.put("orgId", orgId);// 机构号
		req.put("mchtId", mchtId);// 商户号
		req.put("transTime", date);// 交易时间
		req.put("origOrderId", orderId);// 原交易订单号

		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("博淇无卡扣款查询请求参数:" + req);

		String resp = sendPost("http://47.96.160.164:8080/gatewaysite/p/transquery", JSON.toJSONString(req));
		LOG.info("博淇无卡扣款查询返回参数:" + resp);
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);

		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("origRespMsg");// 返回信息
		if ("0000".equals(respCode)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT, resp);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESULT, resp);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
		}
		return maps;
	}

	/**
	 * 代付
	 *
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bqx/payfor")
	public @ResponseBody Object payfor(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入博淇无卡代付接口=================================");

		Map<String, String> maps = new HashMap<String, String>();

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String realAmount = prp.getRealAmount();
		String bankName = prp.getDebitBankName();
		String bankNo = prp.getDebitCardNo();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();

		BQXRegister bqxRegister = topupPayChannelBusiness.getBQXRegisterByIdCard(idCard);

		String real_amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		LOG.info("金额，单位分:" + real_amount);

		Map<String, String> req = new TreeMap();
		String requestNo = getOrderId();
		req.put("mchtOrderId", requestNo);// 订单号
		req.put("orgId", bqxRegister.getOrgId());// 机构号
		req.put("mchtId", bqxRegister.getMerchantNo());// 商户号
		req.put("transTime", date);// 交易时间
		req.put("currency", "CNY");// 交易币种 固定值
		req.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/bqx/payfor/notifyurl");// 异步回调地址
		req.put("idnum", idCard);
		req.put("tranAmt", real_amount);
		req.put("idtype", "1");
		req.put("settleBank", bankName);
		req.put("settlePan", bankNo);
		req.put("name", userName);
		req.put("mobile", phoneD);
		req.put("bankProvince", "上海市");
		req.put("bankCity", "上海市");
		req.put("bankCounty", "宝山区");

		String sign = sign(req);
		req.put("sign", sign);

		LOG.info("博淇无卡余额代付请求参数:" + req);

		String resp = sendPost("http://47.96.160.164:8180/withdrawsite/w/pay", JSON.toJSONString(req));

		LOG.info("博淇无卡余额代付返回参数:" + resp);
		TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
		boolean verify = verify(treeMap);

		JSONObject json_obj = JSON.parseObject(resp);
		String respCode = json_obj.getString("respCode");// 返回码
		String respMsg = json_obj.getString("respMsg");// 返回信息
		if ("0000".equals(respCode)) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String URL = null;
			String result = null;
			LOG.info("*********************交易成功***********************");

			URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", requestNo);
			try {
				result = restTemplate.postForObject(URL, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

			LOG.info("订单已交易成功!");

		} else {
			this.addOrderCauseOfFailure(orderCode, respMsg + "[" + requestNo + "]", rip);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);

		}
		return maps;

	}


}
