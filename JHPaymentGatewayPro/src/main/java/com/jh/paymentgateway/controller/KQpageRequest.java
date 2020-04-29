package com.jh.paymentgateway.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
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
import com.alibaba.fastjson.JSONArray;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.KQBindCard;
import com.jh.paymentgateway.pojo.KQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.kq.Base64Binrary;
import com.jh.paymentgateway.util.kq.EncryptDate;
import com.jh.paymentgateway.util.kq.MyX509TrustManager;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;
import sun.misc.BASE64Decoder;

@Controller
@EnableAutoConfiguration
public class KQpageRequest extends BaseChannel {

	@Value("${kq.jkspath}")
	private String jkspath;

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	private static final Logger LOG = LoggerFactory.getLogger(KQpageRequest.class);
	protected static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static String version = "1.0";
	private static String merchantid = "812310048161056";
	private static String terminalid = "13197082";
	private static String memberCode = "10210070750";

	/**
	 * 注册
	 * 
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/register")
	public @ResponseBody Object register(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "rip") String rip) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/personalSeller/register";
		EncryptDate encrypt = new EncryptDate();
		Map<String, Object> body = new HashMap<String, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("uId", "xinli" + userId);
		body.put("platformCode", memberCode);
		body.put("idCardType", "101");// 身份证
		body.put("idCardNumber", idCard);// 身份证号
		body.put("name", userName);// 姓名
		body.put("userFlag", "1");// 平台标识
		body.put("mobile", phone);// 手机号：否
		String data = JSON.toJSONString(body);
		LOG.info("kq-register-明文参数：" + data);
		// 加密后报文
		String encryptData = encrypt.signAndEncrypt(memberCode, data.getBytes());
		LOG.info("kq-register-加密后报文：" + encryptData);
		String respResult = sendJson(url, memberCode, encryptData);
		LOG.info("kq-register-返回密文：" + respResult);
		if (null != respResult) {
			// 解析响应
			Map<String, String> rspMap = JSON.parseObject(respResult, Map.class);
			String decryptData = encrypt.decryptMerchantMsgAndVerifySignature(memberCode, rspMap.get("encryptedData"),
					rspMap.get("envelope"), rspMap.get("signature"));
			LOG.info("kq-register-返回明文：" + decryptData);
			JSONObject json = JSONObject.fromObject(decryptData);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			if ("0000".equals(code)) {
				KQRegister kqr = new KQRegister();
				kqr.setMerchantNo("xinli" + userId);
				kqr.setIdCard(idCard);
				kqr.setUserName(userName);
				kqr.setSignStatus("0");
				topupPayChannelBusiness.createKQRegister(kqr);
				// 限制注册2分钟后签约
				redisUtil.set(idCard + "-0", kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "注册成功，正在下载合同,请耐心等待2分钟");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, "注册" + errorMsg + "[" + requestNo + "]", rip);
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "注册系统异常!");
			this.addOrderCauseOfFailure(orderCode, "注册系统异常[" + requestNo + "]", rip);
		}
		return maps;
	}

	/**
	 * 合同签约
	 * 
	 * @param orderCode
	 * @param userId
	 * @param rip
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/signContract")
	public @ResponseBody Object signContract(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "rip") String rip,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		if (redisUtil.getKq(idCard + "-0") != null) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "正在下载签约合同,请2分钟后重试!");
			return maps;
		}
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/signContract";
		EncryptDate encrypt = new EncryptDate();
		Map<String, Object> body = new HashMap<String, Object>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("applyId", kqr.getMerchantNo()); // 个人uid
		body.put("signType", "1");// 签约类型
		String data = JSON.toJSONString(body);
		LOG.info("kq-signContract-明文参数：" + data);
		// 加密后报文
		String encryptData = encrypt.signAndEncrypt(memberCode, data.getBytes());
		LOG.info("kq-signContract-加密后报文：" + encryptData);
		String respResult = sendJson(url, memberCode, encryptData);
		LOG.info("kq-signContract-返回密文：" + encryptData);
		if (null != respResult) {
			// 解析响应
			Map<String, String> rspMap = JSON.parseObject(respResult, Map.class);
			String decryptData = encrypt.decryptMerchantMsgAndVerifySignature(memberCode, rspMap.get("encryptedData"),
					rspMap.get("envelope"), rspMap.get("signature"));
			LOG.info("kq-signContract-返回明文：" + decryptData);
			JSONObject json = JSONObject.fromObject(decryptData);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			if ("0000".equals(code)) {
				kqr.setSignStatus("1");
				topupPayChannelBusiness.createKQRegister(kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, "合同签约" + errorMsg + "[" + requestNo + "]", rip);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "合同签约异常!");
			this.addOrderCauseOfFailure(orderCode, "合同签约系统异常[" + requestNo + "]", rip);
		}
		return maps;
	}

	/**
	 * 检查银行卡是否有受理能力
	 * 
	 * @param userId
	 * @param cardNo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/accept")
	public @ResponseBody Object accept(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "cardNo") String cardNo, @RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "rip") String rip) throws Exception {
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/person/bankcard/accep";
		EncryptDate encrypt = new EncryptDate();
		Map<String, Object> body = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("uId", userId); // 个人uid
		body.put("bankAcctId", cardNo);
		String data = JSON.toJSONString(body);
		LOG.info("检测受理明文参数：" + data);
		// 加密后报文
		String encryptData = encrypt.signAndEncrypt(memberCode, data.getBytes());
		LOG.info("加密后报文：" + encryptData);
		String respResult = sendJson(url, memberCode, encryptData);
		LOG.info("返回密文：" + encryptData);
		if (null != respResult) {
			// 解析响应
			Map<String, String> rspMap = JSON.parseObject(respResult, Map.class);
			String decryptData = encrypt.decryptMerchantMsgAndVerifySignature(memberCode, rspMap.get("encryptedData"),
					rspMap.get("envelope"), rspMap.get("signature"));

			LOG.info("检测受理返回明文：" + decryptData);
			JSONObject json = JSONObject.fromObject(decryptData);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			if ("0000".equals(code)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, "检测受理" + errorMsg + "[" + requestNo + "]", rip);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "检测受理异常!");
			this.addOrderCauseOfFailure(orderCode, "检测受理系统异常[" + requestNo + "]", rip);
		}
		return maps;
	}

	/**
	 * 鉴权结算卡
	 * 
	 * @param orderCode
	 * @param idCard
	 * @param userId
	 * @param cardNo
	 * @param phone
	 * @param userName
	 * @param rip
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/bindThreeElements")
	public @ResponseBody Object bindThreeElements(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "cardNo") String cardNo, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "rip") String rip)
					throws Exception {
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/person/bankcard/bindThreeElements";
		EncryptDate encrypt = new EncryptDate();
		Map<String, Object> body = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("uId", kqr.getMerchantNo()); // 个人uid
		body.put("bankAcctId", cardNo);
		body.put("mobile", phone);
		body.put("idCardNumber", idCard);
		body.put("idCardType", "101");
		body.put("name", userName);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> tree1 = new TreeMap<>();
		tree1.put("fileType", "2");
		tree1.put("fssId", kqr.getFssId1());
		tree1.put("fileExtName", ".jpg");
		Map<String, Object> tree2 = new TreeMap<>();
		tree2.put("fileType", "23");
		tree2.put("fssId", kqr.getFssId2());
		tree2.put("fileExtName", ".jpg");
		Map<String, Object> tree3 = new TreeMap<>();
		tree3.put("fileType", "109");
		tree3.put("fssId", kqr.getFssId3());
		tree3.put("fileExtName", ".jpg");
		list.add(0, tree1);
		list.add(1, tree2);
		list.add(2, tree3);
		body.put("fileList", list);
		String data = JSON.toJSONString(body);
		LOG.info("kq鉴权结算卡明文参数：" + data);
		// 加密后报文
		String encryptData = encrypt.signAndEncrypt(memberCode, data.getBytes());
		LOG.info("加密后报文：" + encryptData);
		String respResult = sendJson(url, memberCode, encryptData);
		LOG.info("返回密文：" + encryptData);
		if (null != respResult) {
			// 解析响应
			Map<String, String> rspMap = JSON.parseObject(respResult, Map.class);
			String decryptData = encrypt.decryptMerchantMsgAndVerifySignature(memberCode, rspMap.get("encryptedData"),
					rspMap.get("envelope"), rspMap.get("signature"));

			LOG.info("kq鉴权结算卡返回明文：" + decryptData);
			JSONObject json = JSONObject.fromObject(decryptData);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			if ("0000".equals(code)) {
				kqr.setBankCard(cardNo);
				kqr.setPhone(phone);
				topupPayChannelBusiness.createKQRegister(kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, "鉴权三要素:" + errorMsg + "[" + requestNo + "]", rip);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "鉴权三要素异常!");
			this.addOrderCauseOfFailure(orderCode, "鉴权三要素系统异常[" + requestNo + "]", rip);
		}
		return maps;
	}

	/**
	 * 绑卡申请
	 * 
	 * @param securityCode
	 * @param expiredTime
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */

	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/bindcardssm")
	public @ResponseBody Object bindCardssm(@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "ordercode", required = false) String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String phoneC = prp.getCreditCardPhone();
		String bankCard = prp.getBankCard();
		String userName = prp.getUserName();
		String rip = prp.getIpAddress();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		KQBindCard kqb = topupPayChannelBusiness.getKQBindCardByBankCard(bankCard);
		String requestNo = TimeFormat("yyyyMMddHHmmss");
		LOG.info("=============" + requestNo + "======bindCardssm=============================");
		Map<String, Object> maps = new HashMap<>();
		String url = "https://mas.99bill.com/cnp/ind_auth";
		StringBuilder tr1XML = new StringBuilder();
		tr1XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		tr1XML.append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
		tr1XML.append("<version>" + version + "</version>");
		tr1XML.append("<indAuthContent>");
		tr1XML.append("<merchantId>" + merchantid + "</merchantId>");
		tr1XML.append("<terminalId>" + terminalid + "</terminalId>");
		tr1XML.append("<customerId>" + kqr.getMerchantNo() + "</customerId>");
		tr1XML.append("<externalRefNumber>" + requestNo + "</externalRefNumber>");
		tr1XML.append("<pan>" + bankCard + "</pan>");
		tr1XML.append("<cardHolderName>" + userName + "</cardHolderName>");
		tr1XML.append("<idType>" + "0" + "</idType>");
		tr1XML.append("<cardHolderId>" + idCard + "</cardHolderId>");
		tr1XML.append("<phoneNO>" + phoneC + "</phoneNO>");
		tr1XML.append("<expiredDate>" + expiredTime + "</expiredDate>");
		tr1XML.append("<cvv2>" + securityCode + "</cvv2>");
		tr1XML.append("</indAuthContent>");
		tr1XML.append("</MasMessage>");
		String params = tr1XML.toString();
		LOG.info("========快钱绑卡短信申请参数:" + params);
		String result = sendPost(url, params);
		LOG.info("========快钱绑卡短信申请返回参数:" + result);
		String str = xml2JSON(result);
		LOG.info(str);
		String o = "[\"2212132321213\"]";
		JSONObject jsonb = JSONObject.fromObject(str);
		String Mas = jsonb.getString("MasMessage");
		JSONObject masJson = JSONObject.fromObject(Mas);
		String indAuthContent = masJson.getString("indAuthContent");
		LOG.info(indAuthContent);
		JSONArray inJson = JSONArray.parseArray(indAuthContent);
		String body = inJson.getString(0);
		LOG.info(body);
		JSONObject bodyJSON = JSONObject.fromObject(body);
		String respMessage = bodyJSON.getString("responseTextMessage");
		LOG.info(respMessage);
		String respCode = bodyJSON.getString("responseCode");
		LOG.info(respCode);
		if (respCode.contains("00")) {
			if (kqb != null) {
				LOG.info("===================绑卡未审核通过");
			} else {
				KQBindCard kqBindCard = new KQBindCard();
				kqBindCard.setBankCard(bankCard);
				kqBindCard.setIdCard(idCard);
				kqBindCard.setPhone(phoneC);
				kqBindCard.setStatus("0");
				kqBindCard.setUserName(userName);
				topupPayChannelBusiness.createKQBindCard(kqBindCard);
			}
			String tokenStr = bodyJSON.getString("token");
			LOG.info("======" + tokenStr);
			String start = "[";
			String end = "]";
			int strStartIndex = tokenStr.indexOf(start);
			int strEndIndex = tokenStr.indexOf(end);
			String token = tokenStr.substring(strStartIndex + 2, strEndIndex - 1);
			LOG.info("=======取出：" + token);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("token", token);
			maps.put("requestNo", requestNo);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			this.addOrderCauseOfFailure(orderCode, "kq绑卡：" + respMessage + "[" + requestNo + "]", rip);
		}
		return maps;
	}

	/**
	 * 绑卡确认
	 * 
	 * @param verifyCode
	 * @param token
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/bindcardssm-verify")
	public @ResponseBody Object SSMverify(@RequestParam(value = "smsCode") String verifyCode,
			@RequestParam(value = "token") String token, @RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "requestNo") String requestNo) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String phoneC = prp.getCreditCardPhone();
		String bankCard = prp.getBankCard();
		String cardName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		KQBindCard kqb = topupPayChannelBusiness.getKQBindCardByBankCard(bankCard);
		LOG.info("=========" + requestNo + "========SSMverify===============================");
		String url = "https://mas.99bill.com/cnp/ind_auth_verify";
		StringBuilder tr1XML = new StringBuilder();
		tr1XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		tr1XML.append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
		tr1XML.append("<version>" + version + "</version>");
		tr1XML.append("<indAuthDynVerifyContent>");
		tr1XML.append("<merchantId>" + merchantid + "</merchantId>");
		tr1XML.append("<terminalId>" + terminalid + "</terminalId>");
		tr1XML.append("<customerId>" + kqr.getMerchantNo() + "</customerId>");
		tr1XML.append("<externalRefNumber>" + requestNo + "</externalRefNumber>");
		tr1XML.append("<pan>" + bankCard + "</pan>");
		tr1XML.append("<validCode>" + verifyCode + "</validCode>");
		tr1XML.append("<token>" + token + "</token>");
		tr1XML.append("<phoneNO>" + phoneC + "</phoneNO>");
		tr1XML.append("</indAuthDynVerifyContent>");
		tr1XML.append("</MasMessage>");
		String params = tr1XML.toString();
		LOG.info("========快钱绑卡确认请求参数:" + params);
		String result = sendPost(url, params);
		LOG.info("========快钱绑卡去人返回参数:" + result);
		String str = xml2JSON(result);
		LOG.info(str);
		JSONObject jsonb = JSONObject.fromObject(str);
		String Mas = jsonb.getString("MasMessage");
		JSONObject masJson = JSONObject.fromObject(Mas);
		String indAuthDynVerifyContent = masJson.getString("indAuthDynVerifyContent");
		LOG.info(indAuthDynVerifyContent);
		JSONArray inJson = JSONArray.parseArray(indAuthDynVerifyContent);
		String body = inJson.getString(0);
		LOG.info(body);
		JSONObject bodyJSON = JSONObject.fromObject(body);
		String respMessage = bodyJSON.getString("responseTextMessage");
		LOG.info(respMessage);
		String respCode = bodyJSON.getString("responseCode");
		LOG.info("===============" + respCode);
		String paytokenStr = bodyJSON.getString("payToken");
		LOG.info("=========" + paytokenStr);
		String start = "[";
		String end = "]";
		int strStartIndex = paytokenStr.indexOf(start);
		int strEndIndex = paytokenStr.indexOf(end);
		String payToken = paytokenStr.substring(strStartIndex + 2, strEndIndex - 1);
		LOG.info("payToken取出：" + payToken);
		if (respCode.contains("00")) {
			kqb.setChangeTime(new Date());
			kqb.setStatus("1");
			kqb.setPayToken(payToken);
			topupPayChannelBusiness.createKQBindCard(kqb);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("redirect_url",
					ip + "/v1.0/paymentgateway/quick/kq/pay-view?bankName=" + URLEncoder.encode(cardName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
							+ "&ordercode=" + orderCode + "&ipAddress=" + ip);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			this.addOrderCauseOfFailure(orderCode, respMessage, rip);
		}

		return maps;
	}

	/**
	 * 快捷交易短信
	 * 
	 * @param orderCode
	 * @return
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/requestOrder-ssm")
	public @ResponseBody Object requestOrder(@RequestParam("orderCode") String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		String userName = prp.getUserName();
		Map<String, Object> maps = new HashMap<>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		KQBindCard kqb = topupPayChannelBusiness.getKQBindCardByBankCard(bankCard);
		LOG.info("=============" + orderCode + "======requestOrder=============================");
		String url = "https://mas.99bill.com/cnp/getDynNum";
		StringBuilder tr1XML = new StringBuilder();
		tr1XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		tr1XML.append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
		tr1XML.append("<version>" + version + "</version>");
		tr1XML.append("<GetDynNumContent>");
		tr1XML.append("<merchantId>" + merchantid + "</merchantId>");
		tr1XML.append("<customerId>" + kqr.getMerchantNo() + "</customerId>");
		tr1XML.append("<externalRefNumber>" + orderCode + "</externalRefNumber>");
		tr1XML.append("<cardHolderName>" + userName + "</cardHolderName>");
		tr1XML.append("<cardHolderId>" + idCard + "</cardHolderId>");
		tr1XML.append("<payToken>" + kqb.getPayToken() + "</payToken>");
		tr1XML.append("<amount>" + amount + "</amount>");
		tr1XML.append("</GetDynNumContent>");
		tr1XML.append("</MasMessage>");
		String params = tr1XML.toString();
		LOG.info("========快钱交易短信申请参数:" + params);
		String result = sendPost(url, params);
		LOG.info("========快钱交易短信申请返回参数:" + result);
		String str = xml2JSON(result);
		LOG.info(str);
		JSONObject jsonb = JSONObject.fromObject(str);
		String Mas = jsonb.getString("MasMessage");
		JSONObject masJson = JSONObject.fromObject(Mas);
		String GetDynNumContent = masJson.getString("GetDynNumContent");
		LOG.info(GetDynNumContent);
		JSONArray inJson = JSONArray.parseArray(GetDynNumContent);
		String body = inJson.getString(0);
		LOG.info(body);
		JSONObject bodyJSON = JSONObject.fromObject(body);
		String respCode = bodyJSON.getString("responseCode");
		LOG.info(respCode);
		if (respCode.contains("00")) {
			String tokenStr = bodyJSON.getString("token");
			LOG.info("======" + tokenStr);
			String start = "[";
			String end = "]";
			int strStartIndex = tokenStr.indexOf(start);
			int strEndIndex = tokenStr.indexOf(end);
			String token = tokenStr.substring(strStartIndex + 2, strEndIndex - 1);
			LOG.info("=====取出：" + token);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("token", token);
			maps.put("orderId", orderCode);
		} else {
			this.addOrderCauseOfFailure(orderCode, "请求交易短信失败", rip);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求交易短信失败");
		}
		return maps;
	}

	/**
	 * 确认交易
	 * 
	 * @param orderCode
	 * @param verifyCode
	 * @param requestNo
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/fastpay")
	public @ResponseBody Object fastpay(@RequestParam("orderCode") String orderCode,
			@RequestParam(value = "smsCode") String verifyCode, @RequestParam(value = "orderId") String requestNo,
			@RequestParam(value = "token") String token) throws Exception {
		LOG.info("=============" + requestNo + "======requestOrder=============================");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String realAmount = prp.getRealAmount();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<>();
		KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
		KQBindCard kqb = topupPayChannelBusiness.getKQBindCardByBankCard(bankCard);
		String url = "https://mas.99bill.com/cnp/purchase";
		StringBuilder tr1XML = new StringBuilder();
		tr1XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		tr1XML.append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
		tr1XML.append("<version>" + version + "</version>");
		tr1XML.append("<TxnMsgContent>");
		tr1XML.append("<interactiveStatus>" + "TR1" + "</interactiveStatus>");
		tr1XML.append("<spFlag>" + "QuickPay" + "</spFlag>");
		tr1XML.append("<txnType>" + "PUR" + "</txnType>");
		tr1XML.append("<merchantId>" + merchantid + "</merchantId>");
		tr1XML.append("<terminalId>" + terminalid + "</terminalId>");
		tr1XML.append("<externalRefNumber>" + requestNo + "</externalRefNumber>");
		tr1XML.append("<entryTime>" + TimeFormat("yyyyMMddHHmmss") + "</entryTime>");
		tr1XML.append("<amount>" + amount + "</amount>");
		tr1XML.append("<payToken>" + kqb.getPayToken() + "</payToken>");
		tr1XML.append("<customerId>" + kqr.getMerchantNo() + "</customerId>");
		tr1XML.append("<tr3Url>" + "http://101.227.69.165:8801/YJZF_DEMO/ReceiveTR3ToTR4.jsp" + "</tr3Url>");
		tr1XML.append("<extMap>");
		tr1XML.append("<extDate>");
		tr1XML.append("<key>" + "validCode" + "</key>");
		tr1XML.append("<value>" + verifyCode + "</value>");
		tr1XML.append("</extDate>");
		tr1XML.append("<extDate>");
		tr1XML.append("<key>" + "savePciFlag" + "</key>");
		tr1XML.append("<value>" + "0" + "</value>");
		tr1XML.append("</extDate>");
		tr1XML.append("<extDate>");
		tr1XML.append("<key>" + "token" + "</key>");
		tr1XML.append("<value>" + token + "</value>");
		tr1XML.append("</extDate>");
		tr1XML.append("<extDate>");
		tr1XML.append("<key>" + "payBatch" + "</key>");
		tr1XML.append("<value>" + "2" + "</value>");
		tr1XML.append("</extDate>");
		tr1XML.append("<extDate>");
		String uid01 = "756091451";
		String str = "{\"sharingFlag\":\"1\",\"sharingPayFlag\":\"0\",\"feeMode\":\"0\",\"feePayer\":\"" + uid01
				+ "\",\"sharingData\":\"2^" + uid01 + "^" + sub(amount, realAmount) + "^D+0^收款|2^" + kqr.getMerchantNo()
				+ "^" + realAmount + "^D+0^0\"}";
		tr1XML.append("<key>" + "sharingInfo" + "</key>");
		tr1XML.append("<value>" + str + "</value>");
		tr1XML.append("</extDate>");
		tr1XML.append("</extMap>");
		tr1XML.append("</TxnMsgContent>");
		tr1XML.append("</MasMessage>");
		String params = tr1XML.toString();
		LOG.info("========快钱交易确认参数:" + params);
		String result = sendPost(url, params);
		LOG.info("========快钱交易确认返回参数:" + result);
		String strJson = xml2JSON(result);
		LOG.info("JSON===" + strJson);
		JSONObject jsonb = JSONObject.fromObject(strJson);
		String Mas = jsonb.getString("MasMessage");
		JSONObject masJson = JSONObject.fromObject(Mas);
		String TxnMsgContent = masJson.getString("TxnMsgContent");
		LOG.info(TxnMsgContent);
		JSONArray inJson = JSONArray.parseArray(TxnMsgContent);
		String body = inJson.getString(0);
		LOG.info(body);
		JSONObject bodyJSON = JSONObject.fromObject(body);
		String respCode = bodyJSON.getString("responseCode");
		String responseMessage = bodyJSON.getString("responseTextMessage");
		if (respCode.contains("00")) {
			String Paystatus = this.queryPay(orderCode, kqr.getMerchantNo());
			if ("S".equals(Paystatus)) {
				LOG.info("=======================交易成功======================");
				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String URL = null;
				String results = null;
				LOG.info("*********************交易成功***********************");
				
				URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					results = restTemplate.postForObject(URL, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + orderCode + "====================" + results);

				LOG.info("订单已交易成功!");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, responseMessage);
				maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/yldzpaying");
			} else if ("F".equals(Paystatus)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易分账支付失败");
				this.addOrderCauseOfFailure(orderCode, "交易分账支付失败", rip);
			} else if ("P".equals(Paystatus)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易挂起");
				this.addOrderCauseOfFailure(orderCode, "交易挂起", rip);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, responseMessage);
			this.addOrderCauseOfFailure(orderCode, responseMessage, rip);
		}
		return maps;
	}

	// 跳转待扣款页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yldzpaying")
	public String returnpaying(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		return "yldzpaying";
	}

	/**
	 * xml解析
	 * 
	 * @param xml
	 * @return
	 */
	public static String xml2JSON(String xml) {
		JSONObject obj = new JSONObject();
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(is);
			Element root = doc.getRootElement();
			obj.put(root.getName(), iterateElement(root));
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Map iterateElement(Element element) {
		List jiedian = element.getChildren();
		Element et = null;
		Map obj = new HashMap();
		List list = null;
		for (int i = 0; i < jiedian.size(); i++) {
			list = new LinkedList();
			et = (Element) jiedian.get(i);
			if (et.getTextTrim().equals("")) {
				if (et.getChildren().size() == 0)
					continue;
				if (obj.containsKey(et.getName())) {
					list = (List) obj.get(et.getName());
				}
				list.add(iterateElement(et));
				obj.put(et.getName(), list);
			} else {
				if (obj.containsKey(et.getName())) {
					list = (List) obj.get(et.getName());
				}
				list.add(et.getTextTrim());
				obj.put(et.getName(), list);
			}
		}
		return obj;
	}

	/**
	 * 生成当前系统时间
	 * 
	 * @param timeType
	 * @return
	 */
	public static String TimeFormat(String timeType) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeType);
		String nowTime = simpleDateFormat.format(new Date());
		LOG.info("=========当前时间：" + nowTime);
		return nowTime;

	}

	/**
	 * 结算卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kq/jump-DebitCard-view")
	public String JumpReceivablesCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("快钱出账卡=========================/v1.0/paymentgateway/quick/kq/jump-DebitCard-view");
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

		return "kqbankinfo";
	}

	/**
	 * 转接绑卡页面
	 * 
	 * @param orderCode
	 * @param expiredTime
	 * @param securityCode
	 * @param ipAddress
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/transfer-bindcard")
	public @ResponseBody Object Transfer(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "ipAddress") String ipAddress) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/kq/jump-bindcard-view?ordercode=" + orderCode
				+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode);
		return maps;

	}

	/**
	 * 绑卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kq/jump-bindcard-view")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("快钱充值卡=========================/v1.0/paymentgateway/quick/kq/jump-bindcard-view");

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
		return "kqbindcard";
	}

	/**
	 * 支付页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/kq/pay-view")
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

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "kqpaymessage";
	}

	/**
	 * 页面直跳支付界面
	 * 
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/pay-transfer")
	public @ResponseBody Object PayTransfer(@RequestParam(value = "ordercode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String cardtype = prp.getCreditCardCardType();
		String nature = prp.getCreditCardNature();
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url",
				ip + "/v1.0/paymentgateway/quick/kq/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&nature="
						+ URLEncoder.encode(nature, "UTF-8") + "&bankCard=" + bankCard + "&ordercode=" + orderCode
						+ "&ipAddress=" + ip);
		return maps;

	}

	/**
	 * 上传资质1
	 * 
	 * @param phone
	 * @param brandId
	 * @param orderCode
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/fileUpload1")
	public @ResponseBody Object fileUpload1(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "rip") String rip,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<>();
		String bytes = null;
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		String URL = null;
		String results = null;
		URL = rip + "/v1.0/user/getPicture";
		requestEntity.add("phone", phone);
		try {
			results = restTemplate.postForObject(URL, requestEntity, String.class);
			LOG.info("*******************获取用户图片***********************");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		}
		JSONObject jsonfile = JSONObject.fromObject(results);
		if ("000000".equals(jsonfile.get("resp_code"))) {
			net.sf.json.JSONArray filelist = jsonfile.getJSONArray("result");
			if (filelist.size() == 1) {
				bytes = (String) filelist.get(0);
			} else if (filelist.size() == 2) {
				bytes = (String) filelist.get(0);
			} else if (filelist.size() >= 3) {
				bytes = (String) filelist.get(0);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, jsonfile.get("resp_message"));
			return maps;
		}
		/*
		 * byte[] buffer = new BASE64Decoder().decodeBuffer(bytes);
		 * saveFile("ning1", buffer);
		 */
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/fileUpload";
		Map<String, Object> body = new HashMap<String, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("billAccount", phone); // 快钱账户
		body.put("buffer", bytes);//
		body.put("fileName", requestNo + ".jpg");// 文件名
		String data = JSON.toJSONString(body);
		LOG.info("明文参数：" + data);

		String respResult = sendJson(url, memberCode, data);
		LOG.info("返回明文：" + respResult);
		if (null != respResult) {
			JSONObject json = JSONObject.fromObject(respResult);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			String fssId = json.getString("fssId");
			if ("0000".equals(code)) {
				KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
				kqr.setFssId1(fssId);
				kqr.setChangeTime(new Date());
				topupPayChannelBusiness.createKQRegister(kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, errorMsg + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		maps.put(CommonConstants.RESP_MESSAGE, "资质a上传系统异常！");
		this.addOrderCauseOfFailure(orderCode, "资质a上传系统异常[" + requestNo + "]", rip);
		return maps;
	}

	/**
	 * 上传资质2
	 * 
	 * @param phone
	 * @param brandId
	 * @param orderCode
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/fileUpload2")
	public @ResponseBody Object fileUpload2(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "rip") String rip,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<>();

		String bytes = null;
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		String URL = null;
		String results = null;
		URL = rip + "/v1.0/user/getPicture";
		requestEntity.add("phone", phone);
		try {
			results = restTemplate.postForObject(URL, requestEntity, String.class);
			LOG.info("*******************获取用户图片***********************");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		}
		JSONObject jsonfile = JSONObject.fromObject(results);
		if ("000000".equals(jsonfile.get("resp_code"))) {
			net.sf.json.JSONArray filelist = jsonfile.getJSONArray("result");
			if (filelist.size() == 1) {
				bytes = (String) filelist.get(0);
			} else if (filelist.size() == 2) {
				bytes = (String) filelist.get(1);
			} else if (filelist.size() >= 3) {
				bytes = (String) filelist.get(1);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, jsonfile.get("resp_message"));
			return maps;
		}
		/*
		 * byte[] buffer = new BASE64Decoder().decodeBuffer(bytes);
		 * saveFile("ning2", buffer);
		 */
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/fileUpload";
		Map<String, Object> body = new HashMap<String, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("billAccount", phone); // 快钱账户
		body.put("buffer", bytes);//
		body.put("fileName", requestNo + ".jpg");// 文件名
		String data = JSON.toJSONString(body);
		LOG.info("明文参数：" + data);

		String respResult = sendJson(url, memberCode, data);
		LOG.info("返回明文：" + respResult);
		if (null != respResult) {
			JSONObject json = JSONObject.fromObject(respResult);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			String fssId = json.getString("fssId");
			if ("0000".equals(code)) {
				KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
				kqr.setFssId2(fssId);
				kqr.setChangeTime(new Date());
				topupPayChannelBusiness.createKQRegister(kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, errorMsg + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		maps.put(CommonConstants.RESP_MESSAGE, "资质b上传系统异常！");
		this.addOrderCauseOfFailure(orderCode, "资质b上传系统异常[" + requestNo + "]", rip);
		return maps;
	}

	/**
	 * 上传资质3
	 * 
	 * @param phone
	 * @param brandId
	 * @param orderCode
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/fileUpload3")
	public @ResponseBody Object fileUpload3(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "rip") String rip,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<>();
		String bytes = null;
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		String URL = null;
		String results = null;
		URL = rip + "/v1.0/user/getPicture";
		requestEntity.add("phone", phone);
		try {
			results = restTemplate.postForObject(URL, requestEntity, String.class);
			LOG.info("*******************获取用户图片***********************");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		}
		JSONObject jsonfile = JSONObject.fromObject(results);
		if ("000000".equals(jsonfile.get("resp_code"))) {
			net.sf.json.JSONArray filelist = jsonfile.getJSONArray("result");
			if (filelist.size() == 1) {
				bytes = (String) filelist.get(0);
			} else if (filelist.size() == 2) {
				bytes = (String) filelist.get(0);
			} else if (filelist.size() >= 3) {
				bytes = (String) filelist.get(2);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, jsonfile.get("resp_message"));
			return maps;
		}
		/*
		 * byte[] buffer = new BASE64Decoder().decodeBuffer(bytes);
		 * saveFile("ning3", buffer);
		 */
		// 示例参数
		String url = "https://coe.99bill.com/coe-boss-dolphin/api/fileUpload";
		Map<String, Object> body = new HashMap<String, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		// 构建参数
		body.put("requestId", requestNo);
		body.put("platformCode", memberCode);
		body.put("billAccount", phone); // 快钱账户
		body.put("buffer", bytes);//
		body.put("fileName", requestNo + ".jpg");// 文件名
		String data = JSON.toJSONString(body);
		LOG.info("明文参数：" + data);

		String respResult = sendJson(url, memberCode, data);
		LOG.info("返回明文：" + respResult);
		if (null != respResult) {
			JSONObject json = JSONObject.fromObject(respResult);
			String code = json.getString("code");
			String errorMsg = json.getString("errorMsg");
			String fssId = json.getString("fssId");
			if ("0000".equals(code)) {
				KQRegister kqr = topupPayChannelBusiness.getKQRegisterByIdCard(idCard);
				kqr.setFssId3(fssId);
				kqr.setChangeTime(new Date());
				topupPayChannelBusiness.createKQRegister(kqr);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errorMsg);
				this.addOrderCauseOfFailure(orderCode, errorMsg + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		maps.put(CommonConstants.RESP_MESSAGE, "资质c上传系统异常！");
		this.addOrderCauseOfFailure(orderCode, "资质c上传系统异常[" + requestNo + "]", rip);
		return maps;
	}

	/**
	 * 交易查询
	 * 
	 * @param orderCode
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/kq/queryPay")
	public @ResponseBody String queryPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "MerchantNo") String MerchantNo) throws Exception {
		String url = "https://mas.99bill.com/cnp/query_txn";
		StringBuilder tr1XML = new StringBuilder();
		tr1XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		tr1XML.append("<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">");
		tr1XML.append("<version>" + version + "</version>");
		tr1XML.append("<QryTxnMsgContent>");
		tr1XML.append("<externalRefNumber>" + orderCode + "</externalRefNumber>");
		tr1XML.append("<txnType>" + "PUR" + "</txnType>");
		tr1XML.append("<merchantId>" + merchantid + "</merchantId>");
		tr1XML.append("<terminalId>" + terminalid + "</terminalId>");
		tr1XML.append("<customerId>" + MerchantNo + "</customerId>");
		tr1XML.append("</QryTxnMsgContent>");
		tr1XML.append("</MasMessage>");
		String params = tr1XML.toString();
		LOG.info("========快钱交易查询参数:" + params);
		String result = sendPost(url, params);
		LOG.info("========快钱交易查询返回参数:" + result);
		String strJson = xml2JSON(result);
		LOG.info("JSON===" + strJson);
		JSONObject jsonb = JSONObject.fromObject(strJson);
		String Mas = jsonb.getString("MasMessage");
		JSONObject masJson = JSONObject.fromObject(Mas);
		String TxnMsgContent = masJson.getString("TxnMsgContent");
		LOG.info(TxnMsgContent);
		JSONArray inJson = JSONArray.parseArray(TxnMsgContent);
		String body = inJson.getString(0);
		LOG.info(body);
		JSONObject bodyJSON = JSONObject.fromObject(body);
		String respCode = bodyJSON.getString("responseCode");
		String txnStatus = bodyJSON.getString("txnStatus");
		if (respCode.contains("00")) {
			LOG.info("======================支付查询请求成功");
			if (txnStatus.contains("S")) {
				return "S";
			} else if (txnStatus.contains("F")) {
				return "F";
			} else if (txnStatus.contains("P")) {
				return "P";
			}
		}
		return "F";

	}

	/**
	 * 分账
	 * 
	 * @param amount
	 * @param realAmount
	 * @return
	 */
	public String sub(String amount, String realAmount) {
		BigDecimal b1 = new BigDecimal(amount);

		BigDecimal b2 = new BigDecimal(realAmount);

		return b1.subtract(b2).toString();
	}

	/**
	 * 快钱入网请求sendPost
	 * 
	 * @param url
	 * @param memberCode
	 * @param body
	 * @return
	 */
	public String sendJson(String url, String memberCode, String body) {

		HttpClient client = new DefaultHttpClient();

		HttpPost post = new HttpPost(url);

		try {

			StringEntity s = new StringEntity(body, "UTF-8");

			s.setContentEncoding("UTF-8");

			s.setContentType("application/json;charset=UTF-8");

			post.addHeader("Content-Type", "application/json;charset=UTF-8");

			post.addHeader("X-99Bill-PlatformCode", memberCode);
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");

			post.setEntity(s);

			HttpResponse res = client.execute(post);

			HttpEntity entity = res.getEntity();
			return EntityUtils.toString(entity, "utf8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
		}

	}

	/**
	 * 交易发送url请求
	 * 
	 * @param url
	 * @param tr1XML
	 * @return
	 * @throws Exception
	 */
	public String sendPost(String url, String tr1XML) throws Exception {
		OutputStream out = null;

		String respXml = "";
		String respXmlCut = "";
		String respXmlCut2 = "";
		// 证书路径
		/* String certPath = "D:/kq/81231004816105690.jks"; */
		String certPath = jkspath;
		// 获取证书路径
		File certFile = new File(certPath);

		// 访问Java密钥库，JKS是keytool创建的Java密钥库，保存密钥。
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(certFile), "vpos123".toCharArray());
		// 创建用于管理JKS密钥库的密钥管理器
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		// 初始化证书
		kmf.init(ks, "vpos123".toCharArray());

		// 同位体验证信任决策源//同位体验证可信任的证书来源
		TrustManager[] tm = { new MyX509TrustManager() };

		// 初始化安全套接字
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		// 初始化SSL环境。第二个参数是告诉JSSE使用的可信任证书的来源，设置为null是从javax.net.ssl.trustStore中获得证书。
		// 第三个参数是JSSE生成的随机数，这个参数将影响系统的安全性，设置为null是个好选择，可以保证JSSE的安全性。
		sslContext.init(kmf.getKeyManagers(), tm, null);

		// 根据上面配置的SSL上下文来产生SSLSocketFactory,与通常的产生方法不同
		SSLSocketFactory factory = sslContext.getSocketFactory();

		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpsURLConnection conn = (HttpsURLConnection) realUrl.openConnection();
			// 创建安全的连接套接字
			conn.setSSLSocketFactory(factory);
			// 发送POST请求必须设置如下两行,使用 URL 连接进行输出、入
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 设置URL连接的超时时限
			conn.setReadTimeout(100000);

			// 设置通用的请求属性
			String authString = "812310048161056" + ":" + "vpos123";
			String auth = "Basic " + Base64Binrary.encodeBase64Binrary(authString.getBytes());
			conn.setRequestProperty("Authorization", auth);

			// 获取URLConnection对象对应的输出流
			out = conn.getOutputStream();
			// 发送请求参数
			out.write(tr1XML.getBytes());
			// flush 输出流的缓冲
			out.flush();

			// 得到服务端返回
			InputStream is = conn.getInputStream();
			String reqData = "";
			if (is != null && !"".equals(is)) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] receiveBuffer = new byte[2048];// 缓冲区长度
				int readBytesSize = is.read(receiveBuffer);// 读取数据长度，InputStream要读取的数据长度一定要小于等于缓冲区中的字节数
				System.out.println("readBytesSize：" + readBytesSize);

				while (readBytesSize != -1) {// 判断流是否位于文件末尾而没有可用的字节
					bos.write(receiveBuffer, 0, readBytesSize);// 从receiveBuffer内存处的0偏移开始写，写与readBytesSize长度相等的字节
					readBytesSize = is.read(receiveBuffer);

				}
				reqData = new String(bos.toByteArray(), "UTF-8");// 编码后的tr2报文
			}
			// System.out.println("tr2报文："+reqData);
			// respXml= ParseUtil.parseXML(reqData);//给解析XML的函数传递快钱返回的TR2的XML数据流
			respXml = reqData.replaceAll("\"", "\'");
			System.out.println("tr2报文转换之后：" + respXml);
			respXmlCut = respXml.replace(
					"<?xml version='1.0' encoding='UTF-8' standalone='yes'?><MasMessage xmlns='http://www.99bill.com/mas_cnp_merchant_interface'><version>1.0</version>",
					"");
			respXmlCut2 = respXmlCut.replace("</MasMessage>", "");
			// System.out.println("获取应答码："+respXml.substring(respXml.indexOf("<responseCode>")+14,respXml.indexOf("</responseCode>")));
			// System.out.println("tr2报文剪切之后："+respXmlCut2);
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			if (out != null) {
				out.close();
			}
			// if (in != null){in.close();}
		}
		return respXml;
	}

	/**
	 * 根据文件流生成文件
	 * 
	 * @author lirui
	 * 
	 * @param filename
	 * @param data
	 * @throws Exception
	 */
	public void saveFile(String filename, byte[] data) throws Exception {
		if (data != null) {
			String filepath = "D:\\" + filename;
			File file = new File(filepath);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data, 0, data.length);
			fos.flush();
			fos.close();
			System.out.println("生成文件：D:\\" + filename);
		}
	}

}
