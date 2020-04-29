package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YFJRBinkCard;
import com.jh.paymentgateway.pojo.YFJRRegister;
import com.jh.paymentgateway.util.yf.Common;
import com.jh.paymentgateway.util.yf.HttpClientUtils;
import com.jh.paymentgateway.util.yf.JSONUtil;
import com.jh.paymentgateway.util.yf.LocalUtil;
import com.jh.paymentgateway.util.yf.MessageResponse;
import com.jh.paymentgateway.util.yf.RSAUtils2;
import com.jh.paymentgateway.util.yf.RequestJson;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YFJRpageRequest extends BaseChannel {

	private static final Logger log = LoggerFactory.getLogger(YFJRpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	private String account = "850290097310003";// 商户编号  落地代还
//	private String account1 = "850290029810003";// 商户编号  同名快捷
//	private String account2 = "850290020780003";// 商户编号   同名快捷
	private String agentnum = "159";// 机构编号

	/**
	 * 入网
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/register")
	public @ResponseBody Object register(@RequestParam(value = "name") String name,
			@RequestParam(value = "userId") String userId, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "headBank") String headBank, @RequestParam(value = "cardType") String cardType)
					throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		YFJRRegister yfjrRegister = topupPayChannelBusiness.getYFJRRegisterByIdNum(idCard);
		YFJRRegister yFJRRegister = new YFJRRegister();

		if (yfjrRegister == null) {
			if (!"借记卡".equals(cardType)) {
				cardType = "2";// 2 信用卡
			}else {
				cardType = "1";// 1 储蓄卡
			}
			log.info("入网的卡类型：" + cardType + "-------------------------------");

			// 固定参数
			String serviceCode = "N08g0";// 业务请求码 必填 固定值

			Map<String, Object> dataMap = new HashMap<String, Object>();

			dataMap.put("agentnum", agentnum);// 机构编号
			dataMap.put("channelNum", "100008");// 渠道编号
			dataMap.put("payCode", "100704");// 业务类型 费率编号 必填固定值100704

			dataMap.put("appOrderId", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 全文要求日期格式为
																											// yyyyMMddHHmmss
			dataMap.put("settlementName", name);// 姓名
			dataMap.put("cardType", cardType);// 卡类型 必填 1储蓄卡2信用卡
			dataMap.put("crpIdNo", idCard);// 身份证号
			dataMap.put("cardNo", bankCard);// 卡号
			dataMap.put("phone", phone);// 手机号
			dataMap.put("headBank", headBank);// 总行名称

			dataMap.put("callBackUrl", "");// 异步通知地址(也就是回调地址)

			log.info("入网请求参数|" + dataMap + "|");
			String jsonMap = JSONUtil.mapToJson(dataMap);

			log.info("入网请求参数, mapToJson|" + jsonMap + "|");

			jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

			log.info("入网请求参数, base64|" + jsonMap + "|");

			// 通过私钥进行签名，得到签名
			byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
			String reqSign = new String(mySign);

			log.info("入网请求参数, reqSign|" + reqSign + "|");

			// 通过公钥进行数据加密
			byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

			String reqData = Base64.encodeBase64String(encodedData);

			log.info("入网请求参数, reqData|" + reqData + "|");

			Map<String, Object> request = new HashMap<String, Object>();
			request.put("sign", reqSign);
			request.put("data", reqData);
			request.put("account", account);// 商户号

			log.info("request|" + request + "|");

			JSONObject realParam = new JSONObject();
			realParam.put("serviceCode", serviceCode);
			realParam.put("request", request);

			log.info("request|" + realParam.toString() + "|");

			String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

			log.info("rspJson|" + rspJson + "|");

			MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
					MessageResponse.class);

			Map<String, Object> rspMap = msg.getResponse();

			log.info("入网请求返回参数, rspMap|" + rspMap + "|");

			String rspCode = rspMap.get("respCode").toString();// 返回码
			String respInfo = rspMap.get("respInfo").toString();// 返回信息
			if (!rspCode.equals("0000")) {
				log.info("入网失败：" + rspCode);
				log.info("入网返回信息："+respInfo);
				
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, respInfo);
				return map;
			} else {
				log.info("入网成功，开始验签。");

				// 保存用户信息
				yFJRRegister.setUserName(name);
				yFJRRegister.setPhone(phone);
				yFJRRegister.setIdCard(idCard);
				yFJRRegister.setBankCard(bankCard);

				topupPayChannelBusiness.createYFJRRegister(yFJRRegister);

				String rspSign = rspMap.get("sign").toString();

				Map<String, Object> signMap = new HashMap<String, Object>();
				signMap.put("respCode", rspMap.get("respCode").toString());
				signMap.put("respInfo", rspMap.get("respInfo").toString());

				log.info("signMap|" + signMap + "|");

				String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

				log.info("signData|" + signData + "|");

				boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
						signData.getBytes(Common.CHARSET));
				log.info("验证签名 " + vfy);
				String myResult = new String(Base64.decodeBase64(signData));
				log.info("myResult|" + myResult + "|");
				
				log.info("入网返回信息：",respInfo);

				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, respInfo);
				return map;
			}
		} else {
			log.info("用户已入网-------------------------------");

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "用户已入网");
			return map;
		}

	}

	/**
	 * 入网查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yfjr/registerQuery")
	public @ResponseBody Object registerQuery(@RequestParam(value = "cardNo") String cardNo,
			@RequestParam(value = "phone") String phone) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		// 固定参数
		String serviceCode = "N08l0";// 业务请求码 必填N08l0

		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("agentnum", agentnum);// 机构编号
		dataMap.put("payCode", "100704");// 业务类型 费率编号 必填固定值100704
		dataMap.put("channelNum", "100008");// 渠道编号

		dataMap.put("appOrderId", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		dataMap.put("cardNo", cardNo);// 卡号 过阵可不填
		dataMap.put("phone", phone);// 手机号

		log.info("查询用户入网请求参数|" + dataMap + "|");
		String jsonMap = JSONUtil.mapToJson(dataMap);

		log.info("jsonMap|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		log.info("dataMap base64|" + jsonMap + "|");

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
		String reqSign = new String(mySign);

		log.info("reqSign|" + reqSign + "|");

		// 通过公钥进行数据加密
		byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

		String reqData = Base64.encodeBase64String(encodedData);

		log.info("reqData|" + reqData + "|");

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("account", account);// 商户号

		log.info("request|" + request + "|");

		JSONObject realParam = new JSONObject();
		realParam.put("serviceCode", serviceCode);
		realParam.put("request", request);

		log.info("request|" + realParam.toString() + "|");

		String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

		log.info("rspJson|" + rspJson + "|");

		MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
				MessageResponse.class);

		Map<String, Object> rspMap = msg.getResponse();

		log.info("查询用户入网返回参数|" + rspMap + "|");

		String rspCode = rspMap.get("respCode").toString();
		String respInfo = rspMap.get("respInfo").toString();
		if (!rspCode.equals("0000")) {
			log.info("查询失败：" + respInfo);

			map.put(CommonConstants.RESULT, respInfo);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return map;
		} else {
			log.info("查询成功，开始验签：" + respInfo);
			String rspSign = rspMap.get("sign").toString();

			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("respCode", rspMap.get("respCode").toString());
			signMap.put("respInfo", rspMap.get("respInfo").toString());

			log.info("signMap|" + signMap + "|");

			String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

			log.info("signData|" + signData + "|");

			boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
					signData.getBytes(Common.CHARSET));
			log.info("验证签名 " + vfy);
			String myResult = new String(Base64.decodeBase64(signData));
			log.info("myResult|" + myResult + "|");

			map.put(CommonConstants.RESULT, "查询成功" );
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}

	}

	/**
	 * 绑卡
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yfjr/bindCard")
	public @ResponseBody Object bindCard(@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "bankName") String bankName) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		YFJRBinkCard yfJRBinkCard = topupPayChannelBusiness.getYFJRBinkCardByIdNum(idCard,bankCard,"0");//查询卡是否已预绑卡

		// 固定参数
		String serviceCode = "N0810";// 业务请求码 必填N0810

		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		// dataMap.put("agentnum", agentnum);// 机构编号
		dataMap.put("payCode", "100704");// 业务类型 费率编号 必填固定值100704
		dataMap.put("channelNum", "100008");// 渠道编号

		// dataMap.put("amount", "");// 交易金额，单位（分）
		dataMap.put("appOrderId", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		dataMap.put("settlementName", userName);// 姓名
		dataMap.put("crpIdNo", idCard);// 身份证号
		dataMap.put("outCardNo", bankCard);// 消费信用卡卡号
		dataMap.put("inCardNo", bankCard);// 结算信用卡号

		String outAcctValiddate = this.expiredTimeToMMYY(expiredTime);
		log.info("消费卡有效期：" + outAcctValiddate);
		dataMap.put("outCardExpire", outAcctValiddate);// 消费信用卡有效期 yymm

		String inAcctValiddate = this.expiredTimeToMMYY(expiredTime);
		log.info("结算卡有效期：" + inAcctValiddate);
		dataMap.put("inCardExpire", inAcctValiddate);// 结算信用卡有效期 yymm

		dataMap.put("outCardCvv2", securityCode);// 消费信用卡背面三位数
		dataMap.put("inCardCvv2", securityCode);// 结算信用卡背面三位数
		dataMap.put("inPhone", phone);// 结算信用卡手机号
		dataMap.put("outPhone", phone);// 消费信用卡手机号
		dataMap.put("headBank", bankName);// 结算卡总行名称

		System.out.println("下流订单号：" + agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

		dataMap.put("callBackUrl", ip + "/v1.0/paymentgateway/topup/yf/register/bindcard/callback");// 异步通知地址(也就是回调地址)
		dataMap.put("returnUrl", ip + "/v1.0/paymentgateway/topup/yf/register/bindcard/pagecallback");// 前台跳转地址

		log.info("绑卡请求参数|" + dataMap + "|");
		String appOrderId = (String) dataMap.get("appOrderId");// 请求绑卡流水号
		
		String jsonMap = JSONUtil.mapToJson(dataMap);

		log.info("jsonMap|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		log.info("dataMap base64|" + jsonMap + "|");

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
		String reqSign = new String(mySign);

		log.info("reqSign|" + reqSign + "|");

		// 通过公钥进行数据加密
		byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

		String reqData = Base64.encodeBase64String(encodedData);

		log.info("reqData|" + reqData + "|");

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("account", account);// 商户号

		log.info("request|" + request + "|");

		JSONObject realParam = new JSONObject();
		realParam.put("serviceCode", serviceCode);
		realParam.put("request", request);

		log.info("request|" + realParam.toString() + "|");

		String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

		log.info("rspJson|" + rspJson + "|");

		MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
				MessageResponse.class);

		Map<String, Object> rspMap = msg.getResponse();

		log.info("绑卡返回参数|" + rspMap + "|");

		String rspCode = rspMap.get("respCode").toString();
		String respInfo = rspMap.get("respInfo").toString();
		if (!rspCode.equals("0000")) {
			log.info("绑卡失败：" + respInfo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		} else {
			log.info("申请绑卡成功，开始验签。");
			String rspSign = rspMap.get("sign").toString();

			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("respCode", rspMap.get("respCode").toString());
			signMap.put("respInfo", rspMap.get("respInfo").toString());

			log.info("signMap|" + signMap + "|");

			String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

			log.info("signData|" + signData + "|");

			boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
					signData.getBytes(Common.CHARSET));
			log.info("验证签名 " + vfy);
			String myResult = new String(Base64.decodeBase64(signData));
			log.info("myResult|" + myResult + "|");
			
			if (yfJRBinkCard==null) {//未进行预绑卡
				//保存用户绑卡信息
				YFJRBinkCard yFJRBinkCard = new YFJRBinkCard();
				yFJRBinkCard.setBankCard(bankCard);
				yFJRBinkCard.setIdCard(idCard);
				yFJRBinkCard.setPhone(phone);
				yFJRBinkCard.setUserName(userName);
				yFJRBinkCard.setCvv2(securityCode);
				yFJRBinkCard.setExpired(expiredTime);
				yFJRBinkCard.setStatus("0");// 预绑卡
				yFJRBinkCard.setAppOrderId(appOrderId);
				
				topupPayChannelBusiness.createYFJRBinkCard(yFJRBinkCard);
			}

			/* 找出指定的2个字符在 该字符串里面的 位置 */
			String strStart = "<body>";
			String strEnd = "</body>";
			int strStartIndex = respInfo.indexOf(strStart);
			int strEndIndex = respInfo.indexOf(strEnd);
			String result = respInfo.substring(strStartIndex + 6, strEndIndex);
			System.out.println("截取后的form开始--------------------------");
			System.out.println(result);
			System.out.println("截取后的form结束--------------------------");
			map.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/topup/yf/register/bindcard/bindcardpage?userName=" + userName
							+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&expiredTime=" + expiredTime
							+ "&securityCode=" + securityCode + "&phone=" + phone + "&bankName=" + bankName
							// + "&pageContent=" + result
							+ "&ipAddress=" + ip);
			map.put("pageContent", result);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "申请绑卡成功，跳转绑卡页面");
			return map;
		}
	}

	/**
	 * 绑卡查询
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/bindcard/query")
	public @ResponseBody Object bindcardQuery(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "phone") String phone) throws Exception {
		log.info("进入绑卡查询-------------------------------");
		// 固定参数
		String serviceCode = "N08m0";// 业务请求码

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("agentnum", agentnum);// 机构编号
		dataMap.put("channelNum", "100008");// 渠道编号
		dataMap.put("payCode", "100704");// 业务类型

		dataMap.put("appOrderId", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		dataMap.put("cardNo", bankCard);// 消费卡号
		dataMap.put("phone", phone);// 手机号

		log.info("查询绑卡请求参数|" + dataMap + "|");
		String jsonMap = JSONUtil.mapToJson(dataMap);

		log.info("jsonMap|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		log.info("dataMap base64|" + jsonMap + "|");

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
		String reqSign = new String(mySign);

		log.info("reqSign|" + reqSign + "|");

		// 通过公钥进行数据加密
		byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

		String reqData = Base64.encodeBase64String(encodedData);

		log.info("reqData|" + reqData + "|");

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("account", account);// 商户号

		log.info("request|" + request + "|");

		JSONObject realParam = new JSONObject();
		realParam.put("serviceCode", serviceCode);
		realParam.put("request", request);

		log.info("request|" + realParam.toString() + "|");

		String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

		log.info("rspJson|" + rspJson + "|");

		MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
				MessageResponse.class);

		Map<String, Object> rspMap = msg.getResponse();

		log.info("查询绑卡返回的参数|" + rspMap + "|");

		String rspCode = rspMap.get("respCode").toString();
		String respInfo = rspMap.get("respInfo").toString();
		if (!rspCode.equals("0000")) {
			log.info("查询绑卡失败：" + respInfo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		} else {
			log.info("查询绑卡成功，开始验签。");
			String rspSign = rspMap.get("sign").toString();

			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("respCode", rspMap.get("respCode").toString());
			signMap.put("respInfo", rspMap.get("respInfo").toString());

			log.info("signMap|" + signMap + "|");

			String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

			log.info("signData|" + signData + "|");

			boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
					signData.getBytes(Common.CHARSET));
			log.info("验证签名 " + vfy);
			String myResult = new String(Base64.decodeBase64(signData));
			log.info("myResult|" + myResult + "|");

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}

	}

	/**
	 * 交易申请
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/pay")
	public @ResponseBody Object pay(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		log.info("进入交易申请-------------------------------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String name = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String phone = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String rateFee = prp.getRate();

		// 固定参数
		String serviceCode = "N08o0";

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("agentnum", agentnum);// 机构编号
		dataMap.put("channelNum", "100008");// 渠道编号
		dataMap.put("payCode", "100704");// 业务类型

		dataMap.put("appOrderId", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		dataMap.put("settlementName", name);// 姓名
		dataMap.put("crpIdNo", idCard);// 身份证号
		dataMap.put("inCardNo", bankCard);// 结算信用卡号
		dataMap.put("outCardNo", bankCard);// 消费信用卡卡号
		dataMap.put("phone", phone);// 手机号
		dataMap.put("amount", amount);// 金额单位分
		dataMap.put("rateFee", rateFee);// 费率

		dataMap.put("callBackUrl", ip+"/v1.0/paymentgateway/topup/yf/payCallback");// 异步通知地址(也就是回调地址)

		log.info("交易请求参数|" + dataMap + "|");
		String jsonMap = JSONUtil.mapToJson(dataMap);

		log.info("jsonMap|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		log.info("dataMap base64|" + jsonMap + "|");

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
		String reqSign = new String(mySign);

		log.info("reqSign|" + reqSign + "|");

		// 通过公钥进行数据加密
		byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

		String reqData = Base64.encodeBase64String(encodedData);

		log.info("reqData|" + reqData + "|");

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("account", account);// 商户号

		log.info("request|" + request + "|");

		JSONObject realParam = new JSONObject();
		realParam.put("serviceCode", serviceCode);
		realParam.put("request", request);

		log.info("request|" + realParam.toString() + "|");

		String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

		log.info("rspJson|" + rspJson + "|");

		MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
				MessageResponse.class);

		Map<String, Object> rspMap = msg.getResponse();

		log.info("交易返回参数|" + rspMap + "|");

		String rspCode = rspMap.get("respCode").toString();
		String respInfo = rspMap.get("respInfo").toString();
		if (!rspCode.equals("0000")) {
			log.info("交易失败：" + respInfo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		} else {
			log.info("交易成功，开始验签。");
			String rspSign = rspMap.get("sign").toString();

			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("respCode", rspMap.get("respCode").toString());
			signMap.put("respInfo", rspMap.get("respInfo").toString());

			log.info("signMap|" + signMap + "|");

			String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

			log.info("signData|" + signData + "|");

			boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
					signData.getBytes(Common.CHARSET));
			log.info("验证签名 " + vfy);
			String myResult = new String(Base64.decodeBase64(signData));
			log.info("myResult|" + myResult + "|");

			// 请求订单号
			String appOrderId = (String) dataMap.get("appOrderId");
			// 截取返回订单号
			String strStart = "订单号:";
			int strStartIndex = respInfo.indexOf(strStart);
			String thirdCode = respInfo.substring(strStartIndex + 4);

			System.out.println("截取后的返回订单号:" + thirdCode);
			log.info("记录订单号-----------------------------------------");
			log.info("截取返回的第三方订单号:" + thirdCode + "，请求的订单号：" + appOrderId);
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String results = null;
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "0");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", thirdCode);
			try {
				results = restTemplate.postForObject(url, requestEntity, String.class);
				log.info("*********************交易成功，添加第三方流水号***********************");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("",e);
			}

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}

	}

	/**
	 * 代付申请
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/payment")
	public @ResponseBody Object payment(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		log.info("进入代付申请-------------------------------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String name = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String phone = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String rateFee = prp.getRate();

		// 固定参数
		String serviceCode = "N08h0"; // 业务编码
		String channelNum = "100008";
		
		Map<String, Object> maps = new HashMap<String, Object>();
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();

			map.put("agentNum", agentnum);
			map.put("mercNum", account); // 商户编号
			map.put("channelNum", channelNum); // 渠道编号
			map.put("amount", amount); // 实际金额（分）
			map.put("bankCode", "302100011000"); // 联行号验证 不严谨
			map.put("headBank", "中信银行"); // 总行名称
			map.put("branchBank", "中信银行宝山支行");// 开户支行 不严谨
			map.put("settlementName", name); // 开户姓名//2
			map.put("cardNo", bankCard); // 到账卡号
			map.put("crpIdNo", idCard); // 身份证
			map.put("phone", phone); // 开户手机号
			map.put("feeAmt", "200"); // 固定额度最低200 单位分
			map.put("orderNo", agentnum + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "987987"); // 订单号1
			map.put("callBackUrl", ip + "/v1.0/paymentgateway/topup/yf/paymentCallback"); // 回调地址

			log.info("代付申请请求参数|" + map + "|");

			String result = YFJRpageRequest.reqSendPost(serviceCode, map, Common.PRIVATEKEY, Common.PUBLICKKEY,
					agentnum);
			log.info("代付申请返回参数|" + result + "|");
			
			// 截取返回订单号
		    String strStart = "订单号:";
			int strStartIndex = result.indexOf(strStart);
			String thirdCode = result.substring(strStartIndex + 4);

			log.info("记录返回订单号-----------------------------------------");
			log.info("截取返回的第三方订单号:" + thirdCode);
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String results = null;
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "0");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", thirdCode);
			try {
				results = restTemplate.postForObject(url, requestEntity, String.class);
				log.info("*********************交易成功，添加第三方流水号***********************");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("",e);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, result);
			}
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, result);	
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "代付申请异常");
			return maps;
		}
		return maps;

	}

	/**
	 * 申请交易查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/payQuery")
	public @ResponseBody Object payQuery(@RequestParam(value = "orderCode") String orderCode, // 交易请求的订单号
			@RequestParam(value = "orderNo") String orderNo // 交易返回的订单号
	) throws Exception {
		log.info("进入申请交易查询-------------------------------");
		
		// 固定参数
		String serviceCode = "N08i0";

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("agentnum", agentnum);// 机构编号
		dataMap.put("channelNum", "100008");// 渠道编号
		dataMap.put("payCode", "100704");// 业务类型

		dataMap.put("appOrderId", orderCode); // 交易请求的订单号
		dataMap.put("queryOrderNo", orderNo);// 交易返回的订单号

		log.info("申请交易查询请求参数|" + dataMap + "|");
		String jsonMap = JSONUtil.mapToJson(dataMap);

		log.info("jsonMap|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		log.info("dataMap base64|" + jsonMap + "|");

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(Common.PRIVATEKEY.getBytes()), jsonMap);
		String reqSign = new String(mySign);

		log.info("reqSign|" + reqSign + "|");

		// 通过公钥进行数据加密
		byte[] encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), Common.PUBLICKKEY);

		String reqData = Base64.encodeBase64String(encodedData);

		log.info("reqData|" + reqData + "|");

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("account", account);// 商户号

		log.info("request|" + request + "|");

		JSONObject realParam = new JSONObject();
		realParam.put("serviceCode", serviceCode);
		realParam.put("request", request);

		log.info("request|" + realParam.toString() + "|");

		String rspJson = HttpClientUtils.postJson(Common.URL, Common.PARAM, realParam.toString());

		log.info("rspJson|" + rspJson + "|");

		MessageResponse msg = (MessageResponse) JSONObject.toBean(JSONObject.fromObject(rspJson),
				MessageResponse.class);

		Map<String, Object> rspMap = msg.getResponse();

		log.info("申请交易查询返回参数|" + rspMap + "|");

		String rspCode = rspMap.get("respCode").toString();
		String respInfo = rspMap.get("respInfo").toString();
		if (!rspCode.equals("0000")) {
			log.info("交易查询失败：" + respInfo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		} else {
			log.info("申请交易查询成功，开始验签。");
			String rspSign = rspMap.get("sign").toString();

			Map<String, Object> signMap = new HashMap<String, Object>();
			signMap.put("respCode", rspMap.get("respCode").toString());
			signMap.put("respInfo", rspMap.get("respInfo").toString());

			log.info("signMap|" + signMap + "|");

			String signData = Base64.encodeBase64String(getParString(signMap).getBytes());

			log.info("signData|" + signData + "|");

			boolean vfy = LocalUtil.verifySignature(Base64.decodeBase64(Common.PUBLICKKEY.getBytes()), rspSign,
					signData.getBytes(Common.CHARSET));
			log.info("验证签名 " + vfy);
			String myResult = new String(Base64.decodeBase64(signData));
			log.info("myResult|" + myResult + "|");

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}

	}

	/**
	 * 代付查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/paymentQuery")
	public @ResponseBody Object paymentQuery(
			@RequestParam(value = "orderNo") String orderNo // 交易返回的订单号
	) throws Exception {
		log.info("进入代付查询-------------------------------");
		
		Map<String, Object> maps = new HashMap<String, Object>();
		
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			
			//固定参数
			String channelNum = "100008";
			String serviceCode = "N08j0"; // 业务编码

			map.put("agentNum", agentnum);
			map.put("mercNum", account); // 商户编号
			map.put("channelNum", channelNum); // 渠道编号
			map.put("orderNo", orderNo); // 订单号1

			log.info("|" + map + "|");

			String result = YFJRpageRequest.reqSendPost(serviceCode, map, Common.PRIVATEKEY, Common.PUBLICKKEY,
					agentnum);
			
			log.info("|" + result + "|");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, result);
			return maps;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询异常");
			return maps;
		}

	}
	
	/**
	 * 余额查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yf/balanceQuery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "phone") String phone
	) throws Exception {
		
		log.info("进入余额查询-------------------------------");
		
		Map<String, Object> maps = new HashMap<String, Object>();
		
		//固定参数
		String serviceCode = "N08k0"; // 业务编码
		String channelNum="100008";
		
		try{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("agentNum", agentnum);
			map.put("mercNum", account); // 商户编号
			map.put("channelNum", channelNum); //渠道编号
			map.put("phone",phone); //手机号
		
			log.info("查询余额请求参数|"+map+"|");

			String result = YFJRpageRequest.reqSendPost(serviceCode, map, Common.PRIVATEKEY, Common.PUBLICKKEY,agentnum);
		
			log.info("查询余额返回参数|"+result+"|");
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, result);
			return maps;
			
		}catch(Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询异常");
			return maps;
		}

	}

	/**
	 * 注册回调
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/register/callback")
	public Map<String, Object> register(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("注册回调回来了-----------------");
		Map<String, Object> map = new HashMap<String, Object>();

		String respCode = request.getParameter("respCode");
		String respInfo = request.getParameter("respInfo");

		if ("0000".equals(respCode)) {
			log.info("注册成功-----------------" + respInfo);
			map.put(CommonConstants.RESULT, respInfo);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "入网成功");
			return map;
		} else {
			log.info("注册失败-----------------" + respInfo);
			map.put(CommonConstants.RESULT, respInfo);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "入网失败");
			return map;
		}

	}

	/**
	 * 绑卡回调
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/register/bindcard/callback")
	public Object bindcard(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		log.info("绑卡回调回来了-----------------");
		Map<String, Object> map = new HashMap<String, Object>();

		String respCode = request.getParameter("respCode");
		String respInfo = request.getParameter("respInfo");

		String appOrderId = request.getParameter("appOrderId");
		
		YFJRBinkCard yFJRBinkCard = topupPayChannelBusiness.getYFJRBinkCardByAppOrderId(appOrderId);
		
		if ("0000".equals(respCode)) {
			log.info("绑卡成功-----------------" + respInfo);
			
			yFJRBinkCard.setStatus("1");// 确认绑卡
			
			return "yfjrbindcardsuccess";
		} else {
			log.info("绑卡失败-----------------" + respInfo);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}

	}

	/**
	 * 跳转前端绑卡页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/register/bindcard/pagecallback")
	public Object pagecallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("绑卡前端跳转页面-----------------");

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		Map<String, Object> map = new HashMap<String, Object>();

		String respCode = request.getParameter("respCode");
		String respInfo = request.getParameter("respInfo");
		if ("0000".equals(respCode)) {
			log.info("绑卡成功，跳转交易-----------------");

			return "yfjrbindcardsuccess";
		} else {
			log.info("绑卡异常-----------------");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, respInfo);
			return map;
		}
	}

	/**
	 * 中转页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/register/bindcard/bindcardpage")
	public String bindcardpage(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("绑卡前端跳转页面-----------------");

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String userName = request.getParameter("userName");
		String idCard = request.getParameter("idCard");
		String bankCard = request.getParameter("bankCard");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");
		String bankName = request.getParameter("bankName");
		// String pageContent = request.getParameter("pageContent");

		model.addAttribute("userName", userName);
		model.addAttribute("idCard", idCard);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);
		model.addAttribute("bankName", bankName);
		// model.addAttribute("pageContent", pageContent);// form页面

		return "yfjrbindcard";
	}

	/**
	 * 交易回调
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/payCallback")
	public void payCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("交易回调回来了-----------------");
		Map<String, Object> map = new HashMap<String, Object>();

		String code = request.getParameter("code");
		String msg = request.getParameter("msg");
		String appOrderId = request.getParameter("appOrderId");//请求的订单号
		String orderId = request.getParameter("orderId");//返回的订单号
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(appOrderId);
		
		if ("0000".equals(code)) {
			log.info("交易成功-----------------" + msg);
			log.info("交易订单号：" + appOrderId);
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			log.info("*********************交易成功***********************");
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", appOrderId);
			requestEntity.add("third_code", orderId);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("",e);
			}

			log.info("订单状态修改成功===================" + appOrderId + "====================" + result);

			log.info("订单已交易成功!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			log.info("交易失败-----------------" + msg);
			log.info("交易订单号：" + appOrderId);
			
			this.addOrderCauseOfFailure(appOrderId, "交易失败", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}

	}
	
	/**
	 * 代付回调
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yf/paymentCallback")
	public Object paymentCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("代付回调回来了-----------------");
		Map<String, Object> map = new HashMap<String, Object>();

		String code = request.getParameter("code");
		String msg = request.getParameter("msg");
		String orderNum = request.getParameter("orderNum");

		if ("A000".equals(code)) {
			log.info("代付成功-----------------" + msg);
			log.info("代付订单号：" + orderNum);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, msg);
			return "yfjrsuccess";
		} else {
			log.info("代付失败-----------------" + msg);
			log.info("代付订单号：" + orderNum);

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, msg);
			return map;
		}

	}

	public static String getParString(Map<String, Object> signMap) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("respCode=" + signMap.get("respCode") + ", ");
		sb.append("respInfo=" + signMap.get("respInfo"));
		sb.append("}");
		return sb.toString();
	}

	public static String reqSendPost(String Code, Map<String, Object> map, String prikey, String pubkey,
			String agentnum) throws Exception {

		RequestJson rj = new RequestJson();
		rj.setCode(Code);
		String jsonMap = JSONUtil.mapToJson(map);
		log.info("原始数据|" + jsonMap + "|");

		jsonMap = Base64.encodeBase64String(jsonMap.getBytes());

		// 通过私钥进行签名，得到签名
		byte[] mySign = LocalUtil.sign(Base64.decodeBase64(prikey.getBytes()), jsonMap);
		String reqSign = new String(mySign);
		// 通过公钥进行数据加密
		byte[] encodedData = null;
		encodedData = RSAUtils2.encryptByPublicKey(jsonMap.getBytes(), pubkey.toString());
		String reqData = Base64.encodeBase64String(encodedData);
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("sign", reqSign);
		request.put("data", reqData);
		request.put("agentNum", agentnum);

		rj.setRequest(request);

		JSONObject json = JSONObject.fromObject(rj);
		String param = json + "&unitType=paid";
		log.info("开始请求" + Code + "接口，发送数据为:" + param);
		String result = "";
		HttpPost httpRequst = new HttpPost(Common.myPayUrl);// 创建HttpPost对象

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("requestData", json.toString()));
		params.add(new BasicNameValuePair("unitType", "paid"));
		httpRequst.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		log.info("parms===" + params);
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequst);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			HttpEntity httpEntity = httpResponse.getEntity();
			result = EntityUtils.toString(httpEntity);// 取出应答字符串
		}
		log.info(Code + "接口，返回数据为:" + result);
		return result;
	}

}
