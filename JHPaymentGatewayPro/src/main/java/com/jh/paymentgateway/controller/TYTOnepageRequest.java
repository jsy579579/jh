package com.jh.paymentgateway.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
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
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.FFZCBindCard;
import com.jh.paymentgateway.pojo.FFZCRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.TYTRegister;
import com.jh.paymentgateway.util.JsonUtil;
import com.jh.paymentgateway.util.ffzc.Des3Encryption;
import com.jh.paymentgateway.util.ffzc.HttpUtil;
import com.jh.paymentgateway.util.tyt.Md5Util;
import com.jh.paymentgateway.util.tyt.MyEncryptUtils;
import com.jh.paymentgateway.util.ffzc.QuickPayUtil;
import com.jh.paymentgateway.util.jf.AES;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class TYTOnepageRequest extends BaseChannel {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	private static final Logger LOG = LoggerFactory.getLogger(TYTOnepageRequest.class);

	private static String branchKey = "724C254F1E0A72317953E722A4F56E09"; // 加密秘钥
	private static String branchId = "300004"; // 机构号

	/**
	 * 商户入网
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/tytone/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入商户入网接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String cardNo = prp.getDebitCardNo();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String userId = prp.getUserId();

		Map<String, Object> maps = new HashMap<String, Object>();

		TYTRegister tytRegister = topupPayChannelBusiness.getTYTRegisterByIdCard(idCard);
		if (tytRegister == null) {
			Map<String, Object> map = new HashMap<String, Object>();

			map.put("branchId", branchId);
			map.put("lpName", userName);
			
			String lpCertNo = this.idCardLastToUppercase(idCard);// 身份证最后一位转大写
			map.put("lpCertNo", lpCertNo);
			
			map.put("merchName", branchId+userId);
			map.put("accNo", cardNo);
			map.put("telNo", phoneD);
			map.put("city", "310100");
			map.put("bizTypes", "5601");
			map.put("5601_fee", rate);
			map.put("5601_tzAddFee", ExtraFee);

			String signStr = Md5Util.getSignDataStr(map) + branchKey;
			String Sign = Md5Util.MD5(signStr);

			LOG.info("MD5加密密文：" + Sign);

			map.put("sign", Sign);

			String url = "https://sd-app1.tfb8.com/jk/BranchMerchAction_add";

			String params = new String(Base64.getEncoder().encode(JsonUtil.format(map).getBytes("UTF-8")), "UTF-8");

			LOG.info("商户入网请求报文：" + params);

			String respStr = HttpUtil.sendGet(url + "?params=" + URLEncoder.encode(params));

			LOG.info("商户入网响应报文：" + respStr);

			JSONObject respParmas = JSONObject.parseObject(respStr);
			String result = respParmas.getString("params");

			byte[] resultStr = Base64.getDecoder().decode(result);

			String respParams = new String(resultStr);
			LOG.info("解密明文：" + respParams);

			JSONObject realParam = JSONObject.parseObject(respParams);

			String resCode = realParam.getString("resCode");
			String resMsg = realParam.getString("resMsg");
			String merchId = realParam.getString("merchId");
			if ("00".equals(resCode)) {
				LOG.info("入网成功：" + resMsg);
				LOG.info("----------  tyt保存新用户数据  ----------");
				tytRegister = new TYTRegister();
				tytRegister.setIdCard(idCard);
				tytRegister.setPhone(phoneD);
				tytRegister.setRate(rate);
				tytRegister.setExtraFee(ExtraFee);
				tytRegister.setBankCard(cardNo);// 到账卡
				tytRegister.setUserName(userName);
				tytRegister.setMerchantId(merchId);

				topupPayChannelBusiness.createTYTRegister(tytRegister);

				maps = (Map<String, Object>) putOrder(orderCode);
				String respCode = (String) maps.get("resp_code");
				String respMsg = (String) maps.get("resp_message");
				String URL = (String) maps.get("to_url");
				if ("000000".equals(respCode)) {
					LOG.info("----------  进件成功，跳转交易   ----------");
					
					maps.put(CommonConstants.RESULT, URL);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				}else{
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				}	
			} else {
				LOG.info("入网失败：" + resMsg);

				this.addOrderCauseOfFailure(orderCode, resMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resMsg);
				return maps;
			}
		} else if(!cardNo.equals(tytRegister.getBankCard()) | !rate.equals(tytRegister.getRate())
				| !ExtraFee.equals(tytRegister.getExtraFee())){
			LOG.info("---------- 修改费率/到账卡信息 ----------");
			
			maps = (Map<String, Object>) changeInfo(orderCode);
			String respCode = (String) maps.get("resp_code");
			String respMsg = (String) maps.get("resp_message");
			if ("000000".equals(respCode)) {
				LOG.info("----------  修改费率/到账卡信息成功   ----------");
				
				maps = (Map<String, Object>) putOrder(orderCode);
				String putRespCode = (String) maps.get("resp_code");
				String putRespMsg = (String) maps.get("resp_message");
				String URL = (String) maps.get("to_url");
				if ("000000".equals(putRespCode)) {
					LOG.info("----------  修改费率/到账卡信息成功，跳转交易   ----------");
					
					maps.put(CommonConstants.RESULT, URL);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, putRespMsg);
					return maps;
				}else{
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, putRespMsg);
					return maps;
				}
			}else{
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}	
		} else {
			LOG.info("---------- 用户已存在,费率/到账卡信息一致,直接交易 ----------");

			maps = (Map<String, Object>) putOrder(orderCode);
			String respCode = (String) maps.get("resp_code");
			String respMsg = (String) maps.get("resp_message");
			String URL = (String) maps.get("to_url");
			if ("000000".equals(respCode)) {
				LOG.info("----------  跳转交易   ----------");
				
				maps.put(CommonConstants.RESULT, URL);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}else{
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}	
		}
	}

	/**
	 * 申请交易
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/tytone/putOrder")
	public @ResponseBody Object putOrder(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入天亿通申请交易接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String userName = prp.getUserName();
		String expiredTime = prp.getExpiredTime();
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String rip = prp.getIpAddress();
		String phoneC = prp.getCreditCardPhone();
		String cvn = prp.getSecurityCode();

		Map<String, Object> maps = new HashMap<String, Object>();

		TYTRegister tytRegister = topupPayChannelBusiness.getTYTRegisterByIdCard(idCard);
		String merchId = tytRegister.getMerchantId();
		// FFZCBindCard ffzcBindCard =
		// topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("version", "1.0");
		map.put("branchId", branchId);
		map.put("bizType", "5601");
		map.put("merchId", merchId);
		map.put("payamt", amount);
		map.put("orderId", orderCode);
		map.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/tytone/pay/call-back");// 后台异步通知地址
//		map.put("frontNotifyUrl", ip + "/v1.0/paymentgateway/topup/tyt/pay/return_call");// 支付完成后前台界面跳转地址
		
		String lpCertNo = this.idCardLastToUppercase(idCard);// 身份证最后一位转大写
		map.put("lpCertNo", lpCertNo);
		
		map.put("accNo", bankCard);
		map.put("phoneNo", phoneC);
		map.put("lpName", userName);
//		map.put("isEncrypt", "Y");
		map.put("CVN2", cvn);
		
		String expDate = this.expiredTimeToMMYY(expiredTime);
		map.put("expDate", expDate);// MMYY
		
		String signStr = Md5Util.getSignDataStr(map) + branchKey;
		String Sign = Md5Util.MD5(signStr);

		LOG.info("MD5加密密文：" + Sign);

		map.put("sign", Sign);

		String url = "https://sd-app1.tfb8.com/jk/pay/doOrder";

		String params = new String(Base64.getEncoder().encode(JsonUtil.format(map).getBytes("UTF-8")), "UTF-8");

		LOG.info("天亿通申请交易请求报文：" + params);

		String respStr = HttpUtil.sendGet(url + "?params=" + URLEncoder.encode(params));

		LOG.info("天亿通申请交易响应报文：" + respStr);

		JSONObject respParmas = JSONObject.parseObject(respStr);
		String result = respParmas.getString("params");

		byte[] resultStr = Base64.getDecoder().decode(result);

		String respParams = new String(resultStr);
		LOG.info("解密明文：" + respParams);

		JSONObject realParam = JSONObject.parseObject(respParams);

		String resCode = realParam.getString("resCode");
		String resMsg = realParam.getString("resMsg");
		String URL = realParam.getString("url");// 支付页面地址
		LOG.info("支付URL：" + URL);
		if ("00".equals(resCode)) {
			LOG.info("天亿通申请交易成功：" + resMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			maps.put("to_url", URL);
			return maps;

		} else {
			LOG.info("天亿通申请交易失败：" + resMsg);

			this.addOrderCauseOfFailure(orderCode, resMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}
	
	/**
	 * 商户信息变更
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/tytone/changeInfo")
	public @ResponseBody Object changeInfo(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入天亿通商户信息变更接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		String userName = prp.getUserName();
		String expiredTime = prp.getExpiredTime();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		String phoneD = prp.getDebitPhone();
		String cardNo = prp.getDebitCardNo();
		String rate = prp.getRate();
		String ExtraFee = prp.getExtraFee();
		String userId = prp.getUserId();

		Map<String, Object> maps = new HashMap<String, Object>();

		TYTRegister tytRegister = topupPayChannelBusiness.getTYTRegisterByIdCard(idCard);
		String merchId = tytRegister.getMerchantId();
		// FFZCBindCard ffzcBindCard =
		// topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("branchId", branchId);
		map.put("merchId", merchId);
		map.put("lpName", userName);

		String lpCertNo = this.idCardLastToUppercase(idCard);// 身份证最后一位转大写
		map.put("lpCertNo", lpCertNo);
		
		map.put("merchName", branchId+userId);
		map.put("accNo", cardNo);
		map.put("telNo", phoneD);
		map.put("city", "310100");
		map.put("bizTypes", "5601");
		map.put("5601_fee", rate);
		map.put("5601_tzAddFee", ExtraFee);
		
		String expDate = this.expiredTimeToMMYY(expiredTime);
		map.put("expDate", expDate);// MMYY
		
		String signStr = Md5Util.getSignDataStr(map) + branchKey;
		String Sign = Md5Util.MD5(signStr);

		LOG.info("MD5加密密文：" + Sign);

		map.put("sign", Sign);

		String url = "https://sd-app1.tfb8.com/jk/BranchMerchAction_update";

		String params = new String(Base64.getEncoder().encode(JsonUtil.format(map).getBytes("UTF-8")), "UTF-8");

		LOG.info("天亿通商户信息变更请求报文：" + params);

		String respStr = HttpUtil.sendGet(url + "?params=" + URLEncoder.encode(params));

		LOG.info("天亿通商户信息变更响应报文：" + respStr);

		JSONObject respParmas = JSONObject.parseObject(respStr);
		String result = respParmas.getString("params");

		byte[] resultStr = Base64.getDecoder().decode(result);

		String respParams = new String(resultStr);
		LOG.info("解密明文：" + respParams);

		JSONObject realParam = JSONObject.parseObject(respParams);

		String resCode = realParam.getString("resCode");
		String resMsg = realParam.getString("resMsg");
		if ("00".equals(resCode)) {
			LOG.info("天亿通商户信息变更成功：" + resMsg);

			tytRegister.setExtraFee(ExtraFee);
			tytRegister.setRate(rate);
			tytRegister.setBankCard(cardNo);
			topupPayChannelBusiness.createTYTRegister(tytRegister);
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;

		} else {
			LOG.info("天亿通商户信息变更失败：" + resMsg);

			this.addOrderCauseOfFailure(orderCode, resMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}

	/**
	 * 交易查询
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/tytone/queryOrderStatus")
	public @ResponseBody Object queryOrderStatus(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入天亿通交易查询接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();

		Map<String, Object> maps = new HashMap<String, Object>();

		TYTRegister tytRegister = topupPayChannelBusiness.getTYTRegisterByIdCard(idCard);
		String merchId = tytRegister.getMerchantId();
		
		// FFZCBindCard ffzcBindCard =
		// topupPayChannelBusiness.getFFZCBindCardByBankCard(bankCard);
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("version", "1.0");
		map.put("branchId", branchId);
		map.put("merchId", merchId);
		map.put("orderId", orderCode);
		
		String signStr = Md5Util.getSignDataStr(map) + branchKey;
		String Sign = Md5Util.MD5(signStr);

		LOG.info("MD5加密密文：" + Sign);

		map.put("sign", Sign);

		String url = "https://sd-app1.tfb8.com/jk/query/order";

		String params = new String(Base64.getEncoder().encode(JsonUtil.format(map).getBytes("UTF-8")), "UTF-8");

		LOG.info("天亿通交易查询请求报文：" + params);

		String respStr = HttpUtil.sendGet(url + "?params=" + URLEncoder.encode(params));

		LOG.info("天亿通交易查询响应报文：" + respStr);

		JSONObject respParmas = JSONObject.parseObject(respStr);
		String result = respParmas.getString("params");

		byte[] resultStr = Base64.getDecoder().decode(result);

		String respParams = new String(resultStr);
		LOG.info("解密明文：" + respParams);

		JSONObject realParam = JSONObject.parseObject(respParams);

		String resCode = realParam.getString("resCode");
		String resMsg = realParam.getString("resMsg");
		if ("00".equals(resCode)) {
			LOG.info("天亿通交易查询成功：" + resMsg);
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;

		} else {
			LOG.info("天亿通交易查询失败：" + resMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resMsg);
			return maps;
		}
	}
	
	/**
	 * 交易异步通知
	 * 
	 * @param encryptData
	 * @param signature
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/tytone/pay/call-back")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String spBillNo = request.getParameter("spBillNo");// 平台订单号
		String merchId = request.getParameter("merchId");
		String orderCode = request.getParameter("orderId");// 商户订单号
		String rspCode = request.getParameter("rspCode");// 商户订单号
		String rspMsg = request.getParameter("rspMsg");// 商户订单号
		LOG.info("天亿通消费回调： " + rspMsg);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if ("00".equals(rspCode)) {
			LOG.error("------------------- 天亿通交易成功 -------------------");
			LOG.info("交易订单：" + orderCode);
			LOG.info("第三方查询流水号：" + spBillNo);
			LOG.info("交易商户号：" + merchId);
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", spBillNo);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}
			LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + result);

			PrintWriter pw = response.getWriter();
			pw.print("notify_success");
			pw.close();
		} else if ("XX".equals(rspCode)) {
			LOG.info("--------------- 交易处理中 ---------------");
			Map<String, Object> putMap = (Map<String, Object>) queryOrderStatus(orderCode);

			String putRespCode = (String) putMap.get("resp_code");
			String putRespMsg = (String) putMap.get("resp_message");
			if ("000000".equals(putRespCode)) {
				LOG.error("------------------- 天亿通交易成功 -------------------" + putRespMsg);
				LOG.info("交易订单：" + orderCode);
				LOG.info("第三方查询流水号：" + spBillNo);
				LOG.info("交易商户号：" + merchId);
				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String url = null;
				String result = null;
				
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", spBillNo);
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
				LOG.info("添加第三方流水号成功：===================" + orderCode + "====================" + result);
			}else {
				this.addOrderCauseOfFailure(orderCode, putRespMsg, prp.getIpAddress());
			}
			PrintWriter pw = response.getWriter();
			pw.print("notify_success");
			pw.close();
		} else {
			this.addOrderCauseOfFailure(orderCode, rspMsg, prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("notify_success");
			pw.close();
		}
	}
}
