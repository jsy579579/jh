package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.HZLRBindCard;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import hzchannel.utils.util;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年6月6日 下午4:26:43 类说明 和众自选落地商户快捷
 */
@Controller
public class HZLRTopupRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(HZLRTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final String merchant_no = "FGWL_8D39EE82";

	@SuppressWarnings("unchecked")
	public Object hzTopupRequest(String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, Object> maps = new HashMap<String, Object>();
		String creditCard = prp.getBankCard();
		String creditCardName = prp.getCreditCardBankName();
		String userName = prp.getUserName();
		String creditCardPhone = prp.getCreditCardPhone();
		String debitPhone = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String debitCard = prp.getDebitCardNo();
		String debitBankName = prp.getDebitBankName();
		String amount = prp.getAmount();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String ExtraFee = prp.getExtraFee();
		HZLRBindCard creditCardbind = topupPayChannelBusiness.getBindCardByBankCard(creditCard);
		HZLRBindCard debitCardbind = topupPayChannelBusiness.getBindCardByBankCard(debitCard);
		String creditAuthentNo = null;
		String debitAuthentNo = null;
		if (creditCardbind == null) {
			LOG.info("========================================01贷记卡未鉴权入网！");
			maps = (Map<String, Object>) this.autuority(idCard, creditCard, creditCardPhone, userName);
			if (!"000000".equals(maps.get("resp_code"))) {
				return maps;
			} else {
				creditAuthentNo = (String) maps.get("result");
			}

			maps = (Map<String, Object>) this.joinInternet(creditAuthentNo, idCard, creditCard, creditCardPhone,
					userName, creditCardName, expiredTime, securityCode, "贷记卡");
			if (!"000000".equals(maps.get("resp_code"))) {
				return maps;
			}

		}
		if (debitCardbind == null) {
			LOG.info("========================================02借记卡未鉴权入网！");
			maps = (Map<String, Object>) this.autuority(idCard, debitCard, debitPhone, userName);
			if (!"000000".equals(maps.get("resp_code"))) {
				return maps;
			} else {
				debitAuthentNo = (String) maps.get("result");
			}
			maps = (Map<String, Object>) this.joinInternet(debitAuthentNo, idCard, debitCard, debitPhone, userName,
					debitBankName, expiredTime, securityCode, "借记卡");
			if (!"000000".equals(maps.get("resp_code"))) {
				return maps;
			}
		}
		maps = (Map<String, Object>) this.fastPay(orderCode, creditCard, debitCard, amount, rate, ExtraFee);
		return maps;
	}

	// 鉴权卡信息
	public Object autuority(String idCard, String bankCard, String phone, String userName) throws Exception {
		String url = "http://xapi.ypt5566.com/api/Authent/authent";
		TreeMap<String, String> params = new TreeMap<String, String>();
		// 参数名 必选 类型(最大长度) 说明
		params.put("merchant_no", merchant_no);// 渠道商商户号YPT分配的渠道商户号
		params.put("channel_no", "S7SJBZP"); // 固定值 ‘S7SJBZP’
		params.put("business_no", "authent_channel");// ‘authent_channel’
		params.put("paymer_name", userName);// 持卡人姓名
		params.put("paymer_idcard", idCard);// 持卡人身份证
		params.put("paymer_bank_no", bankCard);// 持卡人银行卡号
		params.put("paymer_phone", phone); // 持卡人预留手机号
		String sign = util.sign(params);
		params.put("sign", sign);// sign签名值
		LOG.info("HZLR_QUICK===鉴权requestSign: " + params.toString());
		try {
			String result = util.http(url, params);
			LOG.info("HZLR_QUICK===鉴权responseSign" + result);
			// authent_no":"IX20190605153135500F8727" 储蓄卡的 鉴权标识用于商户入网
			// authent_no":"PS201906051532189FCE5484" 信用卡的 鉴权标识用于商户入网
			JSONObject json = JSONObject.parseObject(result);
			String Code = json.getString("Code");
			String Msg = json.getString("Msg");
			if ("10000".equals(Code)) {
				String Rcode = json.getString("Resp_code");
				String Rmsg = json.getString("Resp_msg");
				if ("40000".equals(Rcode)) {
					String authentNo = json.getString("authent_no");
					return ResultWrap.init(CommonConstants.SUCCESS, Rmsg, authentNo);
				} else {
					LOG.info("HZLR_QUICK========================================鉴权失败null");
					return ResultWrap.init(CommonConstants.FALIED, decodeUnicode(gbEncoding(Rmsg)));
				}
			} else {
				return ResultWrap.init(CommonConstants.FALIED, decodeUnicode(gbEncoding(Msg)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("HZLR_QUICK========================================鉴权异常");
			return ResultWrap.init(CommonConstants.FALIED, "鉴权异常");
		}
	}

	// 入网
	public Object joinInternet(String authentNo, String idCard, String bankCard, String phone, String userName,
			String bankName, String expiredTime, String securityCode, String cardType) throws Exception {
		String bankType;
		if ("贷记卡".equals(cardType)) {
			bankType = "6";
		} else {
			bankType = "1";
		}
		String url = "http://xapi.ypt5566.com/api/User/enterNet";
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("merchant_no", merchant_no);// 渠道商商户号YPT分配的渠道商户号
		params.put("auth_order_no", authentNo);// 鉴权订单号(3.1接口返回)
		params.put("user_name", userName);// 持卡人姓名
		params.put("id_no", idCard);// 身份证号
		params.put("card_no", bankCard);// 银行卡号
		params.put("phone", phone);// 银行预留手机号
		params.put("bank_name", bankName);// 银行卡开户行名称
		params.put("bank_branch", bankName);// 储蓄卡:银行卡开户支行名称,信用卡:银行卡开户行名称
		params.put("bank_code", "102431008056");// 联行号
		params.put("bank_coding", bankName);// 银行缩写
		params.put("province", "530000");// 省
		params.put("city", "530100");// 城市
		params.put("county", "530100");// 区、县
		params.put("address", "上海市宝山区");// 详细地址
		params.put("validity", expiredTime);// 有效期、信用卡必传(mmyy格式)
		params.put("cvv2", securityCode); // 安全码、信用卡必传
		params.put("bank_type", bankType); // 银行卡类型 1表示储蓄卡6表示信用卡
		String sign = util.sign(params);
		params.put("sign", sign);// MD5 签名
		LOG.info("HZLR_QUICK===入网requestSign: " + params.toString());
		String Rmsg = null;
		try {
			String result = util.http(url, params);
			LOG.info("HZLR_QUICK===入网responseSign" + result);
			JSONObject json = JSONObject.parseObject(result);
			String Code = json.getString("Code");
			String Msg = json.getString("Msg");
			if ("10000".equals(Code)) {
				String Rcode = json.getString("Resp_code");
				Rmsg = json.getString("Resp_msg");
				String userNo = null;
				HZLRBindCard hz = new HZLRBindCard();
				if ("40000".equals(Rcode)) {
					userNo = json.getString("user_no");
					hz.setBankCard(bankCard);
					hz.setCardType(cardType);
					hz.setIdCard(idCard);
					hz.setUserNo(userNo);
					hz.setUserName(userName);
					hz.setPhone(phone);
					topupPayChannelBusiness.createBindCard(hz);
					return ResultWrap.init(CommonConstants.SUCCESS, Rmsg);
				} else if ("40007".equals(Rcode)) {
					userNo = json.getString("user_no");
					hz.setBankCard(bankCard);
					hz.setCardType(cardType);
					hz.setIdCard(idCard);
					hz.setUserNo(userNo);
					hz.setUserName(userName);
					hz.setPhone(phone);
					topupPayChannelBusiness.createBindCard(hz);
					return ResultWrap.init(CommonConstants.SUCCESS, Rmsg);
				} else {
					LOG.info("HZLR_QUICK========================================入网失败null");
					return ResultWrap.init(CommonConstants.FALIED, decodeUnicode(gbEncoding(Rmsg)));
				}
			} else {
				return ResultWrap.init(CommonConstants.FALIED,  decodeUnicode(gbEncoding(Msg)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("HZLR_QUICK========================================入网异常");
			return ResultWrap.init(CommonConstants.FALIED, "入网异常");
		}
	}

	// 提现
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzquick/fastPay")
	public @ResponseBody Object fastPay(@RequestParam("oderCode") String orderCode,
			@RequestParam("creditCard") String creditCard, @RequestParam("debitCard") String debitCard,
			@RequestParam("amount") String amount, @RequestParam("rate") String rate,
			@RequestParam("ExtraFee") String ExtraFee) throws Exception {

		HZLRBindCard creditCardbind = topupPayChannelBusiness.getBindCardByBankCard(creditCard);
		HZLRBindCard debitCardbind = topupPayChannelBusiness.getBindCardByBankCard(debitCard);
		String creditUserNo = creditCardbind.getUserNo();
		String debitUserNo = debitCardbind.getUserNo();
		LOG.info("=======================================贷记卡入网号：" + creditUserNo);
		LOG.info("=======================================借记卡入网号：" + debitUserNo);
		String rate2 = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
		LOG.info("HZLR_QUICK====================提现费率:" + rate2);
		Map<String, Object> maps = new HashMap<String, Object>();
		String url = "http://xapi.ypt5566.com//api/Fall/fallPay";
		TreeMap<String, String> params = new TreeMap<>();
		params.put("merchant_no", "FGWL_8D39EE82");// 渠道商商户号YPT分配的渠道商户号
		params.put("business_no", "pay_channel");// 业务标识 （固定值：pay_channel）
		params.put("channel_no", "S23HHZTP");// 通道标识(开通时运营提供) S23HHZTP
		params.put("bank_card_no", debitUserNo);// 储蓄卡银行卡入网返回的商户号(文档2.1)
		params.put("credit_card_no", creditUserNo);// 信用卡入网返回的商户号(文档2.1)
		params.put("price", amount);// 支付金额 单位元(1 = 人民币 1元)
		params.put("rate", rate2);// 费率(0.6 =0.6%)
		params.put("single_payment", ExtraFee);// 单笔固定费率(1=人民币 1元)
		params.put("order_no", orderCode);// 商户订单号 （每笔订单号必须唯一）
		params.put("web_notify_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");// 支付成功跳转的链接(前端跳转)
		params.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hz/fastpay/notify_call");// 支付回调地址
																							// //
																							// 回调推送信息请求查看文档下方
		params.put("sign", util.sign(params));// MD5签名
		LOG.info("HZLR_QUICK===提现requestSign: " + params.toString());
		String Rmsg = null;
		try {
			String result = util.http(url, params);
			LOG.info("HZLR_QUICK===提现responseSign" + result);
			JSONObject json = JSONObject.parseObject(result);
			String Code = json.getString("Code");
			String Msg = json.getString("Msg");
			if ("10000".equals(Code)) {
				String Rcode = json.getString("Resp_code");
				Rmsg = json.getString("Resp_msg");
				if ("40000".equals(Rcode)) {
					String resp_url = json.getString("url");
					maps.put(CommonConstants.RESULT, resp_url);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, Rmsg);
					return maps;
				} else {
					LOG.info("HZLR_QUICK========================================提现失败null");
					return ResultWrap.init(CommonConstants.FALIED, decodeUnicode(gbEncoding(Rmsg)));
				}
			} else {
				return ResultWrap.init(CommonConstants.FALIED, decodeUnicode(gbEncoding(Msg)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("HZLR_QUICK========================================提现异常");
			return ResultWrap.init(CommonConstants.FALIED, "提现异常");
		}
	}

	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hz/fastpay/notify_call")
	public void bindcardNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

		LOG.info("交易回调回来了！！！！！！！！！！！！！！");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}

		}
		String code = request.getParameter("Resp_code");// 40000成功
		String third_code = request.getParameter("ypt_order_no");// 三方订单号
		String orderCode = request.getParameter("order_no");// 本系统订单号

		LOG.info("交易订单号orderCode-----------------------" + orderCode);
		LOG.info("交易三方号third_code---------------------" + third_code);
		LOG.info("交易状态code---------------------------------" + code);

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if ("40000".equals(code)) {
			LOG.info("*********************交易成功***********************");

			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
			}

			LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);
			LOG.info("=================订单已交易成功!");
			PrintWriter pw = response.getWriter();
			pw.print("10000");
			pw.close();
		} else {
			LOG.info("========================交易异常!");
			PrintWriter pw = response.getWriter();
			pw.print("10001");
			pw.close();
		}

	}

	/*
	 * 中文转unicode编码
	 */
	public static String gbEncoding(final String gbString) {
		char[] utfBytes = gbString.toCharArray();
		String unicodeBytes = "";
		for (int i = 0; i < utfBytes.length; i++) {
			String hexB = Integer.toHexString(utfBytes[i]);
			if (hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		return unicodeBytes;
	}

	/*
	 * unicode编码转中文
	 */
	public static String decodeUnicode(final String dataStr) {
		int start = 0;
		int end = 0;
		final StringBuffer buffer = new StringBuffer();
		while (start > -1) {
			end = dataStr.indexOf("\\u", start + 2);
			String charStr = "";
			if (end == -1) {
				charStr = dataStr.substring(start + 2, dataStr.length());
			} else {
				charStr = dataStr.substring(start + 2, end);
			}
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			start = end;
		}
		return buffer.toString();
	}

}
