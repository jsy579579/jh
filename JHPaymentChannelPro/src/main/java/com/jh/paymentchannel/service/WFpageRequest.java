package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.ChannelDetail;
import com.jh.paymentchannel.util.HttpUtils;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.wf.DESPlus;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WFpageRequest {

	private static final Logger LOG = LoggerFactory.getLogger(WFpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;
	
	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String appKey = "dzco3ug7";

	private String secretKey = "3D749070B2754399BBD36EB82AD6FE11";

//	private String WFurl = " https://gateway.wowpay.cn/pay/receive";
	//private String WFurl = " http://pay.wowpay.cn/pay/receive";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wf/fastpay")
	public @ResponseBody Object wfPay(HttpServletRequest request, @RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "amount") String amount) throws Exception {
		LOG.info("开始进入支付接口========================");
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询订单信息出错");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "没有该订单信息");
			return maps;
		}
		
		if(securityCode == null || "".equals(securityCode) || expiredTime == null || "".equals(expiredTime)){
			maps.put(CommonConstants.RESP_CODE, "999990");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "有效期或安全码没有填写,无法完成支付!");
			return maps;
		}
		
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		//String amount = resultObj.getString("amount");

		//String realAmount = resultObj.getString("realAmount");
		String mobile = "";
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			mobile = resultObj.getString("phone"); // 预留信用卡手机号码
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "查询不到该银行卡信息,可能已被删除!");
			return maps;
		}
		String idcard = resultObj.getString("idcard");// 身份证号
		String userName = resultObj.getString("userName");// 用户姓名
		String bankName = resultObj.getString("bankName");

		String before = expiredTime.substring(0, 2);
		String after;
		try {
			after = expiredTime.substring(2, 4);
		} catch (Exception e1) {
			e1.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "抱歉,您填写的有效期有误,有效期为4位数字,请检查后重试!");
			return maps;
		}

		BigDecimal big = new BigDecimal(before);
		BigDecimal time = new BigDecimal("12");

		int compareTo = big.compareTo(time);
		// 如果前两位大于12，,代表是年/月的格式
		if (compareTo == 1) {
			expiredTime = after + before;
		} else {
			expiredTime = before + after;
		}

		LOG.info("统一格式后的expiredTime=========" + expiredTime);

		ChannelDetail channelDetail = topupPayChannelBusiness.getChannelDetailByTag("WF_QUICK");
		
		String WFurl = channelDetail.getNotifyURL();
		
		LOG.info("WFurl======"+WFurl);
		
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", ordercode);// 订单号
		jsonObj1.put("transAmount", amount);// 交易金额
		jsonObj1.put("accountName", userName);// 账户姓名
		jsonObj1.put("accountNo", bankCard);// 账号
		jsonObj1.put("idCardNo", idcard);// 身份证号码
		jsonObj1.put("mobileNo", mobile);// 手机号
		jsonObj1.put("cvn2", securityCode);// 安全码
		jsonObj1.put("expDate", expiredTime);// 有效期
		jsonObj1.put("callbackUrl", ipAddress + "/v1.0/paymentchannel/topup/wf/paynotify_call");// 回调地址

		jsonObj.put("bizName", "fastPay");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		// 加密数据
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		JSONObject jsonInfo = null;
		String resultCode = null;
		String resultMsg = null;
		try {
			LOG.info("===================发送报文:" + jsonObj);
			// 请求通道支付接口
			String doGet = HttpUtils.doGet(URL);

			// 请求返回的数据解密
			String decrypt = des.decrypt(doGet);

			jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");

			LOG.info("head====" + head);

			resultCode = head.getString("result_code");

			resultMsg = head.getString("result_msg");

			LOG.info("resultCode===" + resultCode);
			LOG.info("resultMsg===" + resultMsg);
		} catch (Exception e) {
			
			LOG.error("请求支付接口出现异常,调用查询接口======"+e);
			
			long time1 = new Date().getTime();
			boolean isTrue = true;
			while(isTrue) {
				if(System.currentTimeMillis() >= time1 + 5000) {
					isTrue = false;
					break;
				}
			}
			
			LOG.info("请求支付接口等待查询======");
			Map map = (Map) this.OrderCodeQuery(request, ordercode, "fastPay");
			String respCode = (String) map.get("resp_code");

			if ("000000".equals(respCode)) {

				/*WFRegister wfRegisterByBankCard = topupPayChannelBusiness.getWFRegisterByBankCard(bankCard);

				if (null == wfRegisterByBankCard) {
					log.info("用户首次交易，需要保存信息=====");
					WFRegister wfRegister = new WFRegister();
					wfRegister.setPhone(mobile);
					wfRegister.setBankCard(bankCard);
					wfRegister.setIdCard(idcard);
					wfRegister.setExpDate(expiredTime);
					wfRegister.setCvn2(securityCode);

					try {
						topupPayChannelBusiness.createWFRegister(wfRegister);
					} catch (Exception e1) {
						log.error("保存用户信息出错啦====" + e1);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put("channel_type", "sdj");
						maps.put(CommonConstants.RESP_MESSAGE, "亲,保存您的信息出错啦");
						return maps;
					}

					log.info("保存用户信息成功");
				}*/

				// **更新订单状态*//*
				// **调用下单，需要得到用户的订单信息*//*
				restTemplate = new RestTemplate();

				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);

				LOG.info("订单状态修改成功===================");

				LOG.info("订单已支付!");

				maps.put(CommonConstants.RESP_CODE, "success");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功啦");
				return maps;

			} else {
				LOG.info("请求支付接口失败===================" + jsonInfo);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);

				return maps;
			}
			
		}

		Map mapp = new HashMap();
		// 支付成功
		if ("SUCCESS".equalsIgnoreCase(resultCode)) {

			LOG.info("请求支付接口成功===================" + jsonInfo);
			/*
			 * WFRegister wfRegisterByBankCard =
			 * topupPayChannelBusiness.getWFRegisterByBankCard(bankCard);
			 * 
			 * if (null == wfRegisterByBankCard) { log.info("用户首次交易，需要保存信息=====");
			 * WFRegister wfRegister = new WFRegister(); wfRegister.setPhone(mobile);
			 * wfRegister.setBankCard(bankCard); wfRegister.setIdCard(idcard);
			 * wfRegister.setExpDate(expiredTime); wfRegister.setCvn2(securityCode);
			 * 
			 * try { topupPayChannelBusiness.createWFRegister(wfRegister); } catch
			 * (Exception e) { log.error("保存用户信息出错啦"); maps.put("resp_code", "failed");
			 * maps.put("channel_type", "sdj"); maps.put("resp_message", "亲,保存您的信息出错啦");
			 * return maps; }
			 * 
			 * log.info("保存用户信息成功"); }
			 */

			/**
			 * 以下注释的代码是 快捷用的，信用卡还款 可以不用
			 */

			/*
			 * BigDecimal bigamount = new BigDecimal(amount); BigDecimal bigrate = new
			 * BigDecimal(rate); BigDecimal bigextraFee = new BigDecimal(extraFee);
			 * 
			 * // 订单总金额乘以费率的值 BigDecimal multiply = bigamount.multiply(bigrate); // 订单总金额减去
			 * 订单总金额乘以费率的值 BigDecimal subtract = bigamount.subtract(multiply); // 再减去额外费用
			 * BigDecimal Amount = subtract.subtract(bigextraFee);
			 * 
			 * BigDecimal setScale = Amount.setScale(2, RoundingMode.CEILING);
			 * 
			 * log.info("代付的金额====setScale" + setScale);
			 * 
			 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); Date now = new
			 * Date(); StringBuffer sb = new StringBuffer(sdf.format(now)); String uuid =
			 * UUID.randomUUID().toString().replaceAll("-", ""); String substring =
			 * uuid.substring(0, 4); String orderId = sb.append(substring).toString();
			 * 
			 * Map map = (Map) this.OrderCodeTransfer(request, orderId, userName1, cardNo,
			 * setScale + "", ipAddress +
			 * "/v1.0/paymentchannel/topup/wf/transfernotify_call");
			 * 
			 * String respCode = (String) map.get("resp_code");
			 * 
			 * if("000000".equals(respCode)){ mapp.put(CommonConstants.RESP_CODE,
			 * "success"); mapp.put("channel_type", "sdj");
			 * mapp.put(CommonConstants.RESP_MESSAGE, "支付成功啦"); return mapp; }else {
			 * mapp.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			 * mapp.put("channel_type", "sdj"); mapp.put(CommonConstants.RESP_MESSAGE,
			 * "支付失败啦"); return mapp; }
			 */

			// **更新订单状态*//*
			// **调用下单，需要得到用户的订单信息*//*
			restTemplate = new RestTemplate();

			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", ordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);

			LOG.info("订单状态修改成功===================");

			LOG.info("订单已支付!");

			mapp.put(CommonConstants.RESP_CODE, "success");
			mapp.put("channel_type", "sdj");
			mapp.put(CommonConstants.RESP_MESSAGE, "支付成功啦");
			return mapp;

		} else if ("SEND".equalsIgnoreCase(resultCode) || "ERROR".equalsIgnoreCase(resultCode)) {

			long time1 = new Date().getTime();
			boolean isTrue = true;
			while (isTrue) {
				if (System.currentTimeMillis() >= (time1 + 5000)) {
					isTrue = false;
					break;
				}
			}
			LOG.info("请求支付接口等待查询======");

			Map map = (Map) this.OrderCodeQuery(request, ordercode, "fastPay");
			String respCode = (String) map.get("resp_code");

			if ("000000".equals(respCode)) {

				/*WFRegister wfRegisterByBankCard = topupPayChannelBusiness.getWFRegisterByBankCard(bankCard);

				if (null == wfRegisterByBankCard) {
					log.info("用户首次交易，需要保存信息=====");
					WFRegister wfRegister = new WFRegister();
					wfRegister.setPhone(mobile);
					wfRegister.setBankCard(bankCard);
					wfRegister.setIdCard(idcard);
					wfRegister.setExpDate(expiredTime);
					wfRegister.setCvn2(securityCode);

					try {
						topupPayChannelBusiness.createWFRegister(wfRegister);
					} catch (Exception e) {
						log.error("保存用户信息出错啦");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put("channel_type", "sdj");
						maps.put(CommonConstants.RESP_MESSAGE, "亲,保存您的信息出错啦");
						return maps;
					}

					log.info("保存用户信息成功");
				}*/

				/**
				 * 以下注释的代码是 快捷用的，信用卡还款 可以不用
				 */

				/*
				 * BigDecimal bigamount = new BigDecimal(amount); BigDecimal bigrate = new
				 * BigDecimal(rate); BigDecimal bigextraFee = new BigDecimal(extraFee);
				 * 
				 * // 订单总金额乘以费率的值 BigDecimal multiply = bigamount.multiply(bigrate); // 订单总金额减去
				 * 订单总金额乘以费率的值 BigDecimal subtract = bigamount.subtract(multiply); // 再减去额外费用
				 * BigDecimal Amount = subtract.subtract(bigextraFee);
				 * 
				 * BigDecimal setScale = Amount.setScale(2, RoundingMode.CEILING);
				 * 
				 * log.info("代付的金额====setScale" + setScale);
				 * 
				 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); Date now = new
				 * Date(); StringBuffer sb = new StringBuffer(sdf.format(now)); String uuid =
				 * UUID.randomUUID().toString().replaceAll("-", ""); String substring =
				 * uuid.substring(0, 4); String orderId = sb.append(substring).toString();
				 * 
				 * Map map2 = (Map) this.OrderCodeTransfer(request, orderId, userName1, cardNo,
				 * setScale + "", ipAddress +
				 * "/v1.0/paymentchannel/topup/wf/transfernotify_call");
				 * 
				 * String respCode1 = (String) map2.get("resp_code");
				 * 
				 * if("000000".equals(respCode1)){ mapp.put(CommonConstants.RESP_CODE,
				 * "success"); mapp.put("channel_type", "sdj");
				 * mapp.put(CommonConstants.RESP_MESSAGE, "支付成功啦"); return mapp; }else {
				 * mapp.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				 * mapp.put("channel_type", "sdj"); mapp.put(CommonConstants.RESP_MESSAGE,
				 * "支付失败啦"); return mapp; }
				 */

				// **更新订单状态*//*
				// **调用下单，需要得到用户的订单信息*//*
				restTemplate = new RestTemplate();

				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);

				LOG.info("订单状态修改成功===================");

				LOG.info("订单已支付!");
				
				mapp.put(CommonConstants.RESP_CODE, "success");
				mapp.put("channel_type", "sdj");
				mapp.put(CommonConstants.RESP_MESSAGE, "支付成功啦");
				return mapp;

			} else {
				LOG.info("请求支付接口失败===================" + jsonInfo);
				mapp.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				mapp.put("channel_type", "sdj");
				mapp.put(CommonConstants.RESP_MESSAGE, resultMsg);

				return mapp;
			}

		} else {
			LOG.info("请求支付接口失败===================" + jsonInfo);
			mapp.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			mapp.put("channel_type", "sdj");
			mapp.put(CommonConstants.RESP_MESSAGE, resultMsg);
			return mapp;
		}
	}

	// 单笔代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wf/transfer")
	public @ResponseBody Object OrderCodeTransfer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "accountName") String accountName,
			@RequestParam(value = "accountNo") String accountNo,
			@RequestParam(value = "transAmount") String transAmount,
			@RequestParam(value = "callbackUrl") String callbackUrl) throws Exception {
		LOG.info("开始进入单笔代付接口======");
		
		ChannelDetail channelDetail = topupPayChannelBusiness.getChannelDetailByTag("WF_QUICK");
		
		String WFurl = channelDetail.getNotifyURL();
		
		LOG.info("WFurl======"+WFurl);
		
		Map<String, String> maps = new HashMap<String, String>();
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", ordercode);// 订单号
		jsonObj1.put("accountName", accountName);// 交易金额
		jsonObj1.put("accountNo", accountNo);// 账号
		jsonObj1.put("transAmount", transAmount);// 身份证号码
		jsonObj1.put("callbackUrl", callbackUrl);// 异步回调地址

		jsonObj.put("bizName", "transfer");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;

		String resultCode = null;
		String resultMsg = null;
		try {
			LOG.info("===================发送报文:" + jsonObj);
			// 请求通道支付接口
			String doGet = HttpUtils.doGet(URL);

			// 请求返回的数据解密
			String decrypt = des.decrypt(doGet);

			JSONObject jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("单笔代付   jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");
			JSONObject content = jsonInfo.getJSONObject("content");

			LOG.info("单笔代付   head====" + head);
			LOG.info("单笔代付   content====" + content);

			resultCode = head.getString("result_code");

			resultMsg = head.getString("result_msg");

			String orderNo = "";
			if ("null" != content.toString() && !"null".equals(content)) {
				orderNo = content.getString("order_no");
			}

			LOG.info("单笔代付  resultCode===" + resultCode);
			LOG.info("单笔代付  resultMsg===" + resultMsg);
			LOG.info("单笔代付  orderNo===" + orderNo);
		} catch (Exception e) {
			
			LOG.error("请求代付接口出现异常,调用查询接口======"+e);

			long time1 = new Date().getTime();
			boolean isTrue = true;
			while(isTrue) {
				if(System.currentTimeMillis() >= time1 + 5000) {
					isTrue = false;
					break;
				}
			}
			LOG.info("请求代付接口等待查询======");
			Map map = (Map) this.OrderCodeQuery(request, ordercode, "transfer");
			String respCode = (String) map.get("resp_code");

			if ("000000".equals(respCode)) {

				// **更新订单状态*//*
				// **调用下单，需要得到用户的订单信息*//*
				RestTemplate restTemplate = new RestTemplate();

				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/update";

				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", ordercode);
				String result = restTemplate.postForObject(url, requestEntity, String.class);

				LOG.info("订单状态修改成功===================");

				LOG.info("订单已代付!");
				maps.put(CommonConstants.RESP_CODE, "success");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "成功");

			} else {
				LOG.info("请求代付接口失败======");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
			}
			
		}

		if ("SEND".equalsIgnoreCase(resultCode) || "ERROR".equalsIgnoreCase(resultCode)) {
			/*
			 * //调用订单查询借口 Map map = (Map) this.OrderCodeQuery(request, ordercode,
			 * "transfer"); String respCode = (String) map.get("resp_code");
			 * 
			 * if ("000000".equals(respCode)) {
			 * 
			 * synchronized (this) { // **更新订单状态
			 */
			/*
			 * // **调用下单，需要得到用户的订单信息
			 *//*
				 * RestTemplate restTemplate = new RestTemplate();
				 * 
				 * URI uri = util.getServiceUrl("transactionclear", "error url request!");
				 * String url = uri.toString() + "/v1.0/transactionclear/payment/update";
				 * 
				 * MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String,
				 * String>(); requestEntity.add("status", "1"); requestEntity.add("order_code",
				 * ordercode); String result = restTemplate.postForObject(url, requestEntity,
				 * String.class);
				 * 
				 * log.info("订单状态修改成功===================");
				 * 
				 * log.info("订单已代付!");
				 * 
				 * maps.put(CommonConstants.RESP_CODE, "success");
				 * maps.put(CommonConstants.RESP_MESSAGE, "成功"); }
				 * 
				 * 
				 * } else {
				 * 
				 * log.info("订单支付失败!"); maps.put(CommonConstants.RESP_CODE,
				 * CommonConstants.FALIED); maps.put(CommonConstants.RESP_MESSAGE, resultMsg); }
				 */

			LOG.info("请求代付接口,等待回调======");
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款");
			maps.put("redirect_url", "");

		} else if ("SUCCESS".equals(resultCode)) {
			LOG.info("请求代付接口成功======");

			// **更新订单状态*//*
			// **调用下单，需要得到用户的订单信息*//*
			RestTemplate restTemplate = new RestTemplate();

			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update";

			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", ordercode);
			String result = restTemplate.postForObject(url, requestEntity, String.class);

			LOG.info("订单状态修改成功===================");

			LOG.info("订单已代付!");
			maps.put(CommonConstants.RESP_CODE, "success");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "成功");

		} else {
			LOG.info("请求代付接口失败======");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
		}
		LOG.info("maps=====" + maps);
		LOG.info("-----处理完成----");

		return maps;

	}

	// 订单查询接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wf/ordercodequery")
	public @ResponseBody Object OrderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "transType") String transType

	) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

        ChannelDetail channelDetail = topupPayChannelBusiness.getChannelDetailByTag("WF_QUICK");
		
		String WFurl = channelDetail.getNotifyURL();
		
		LOG.info("WFurl======"+WFurl);
		
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", ordercode);// 订单号
		jsonObj1.put("transType", transType);// 业务名称

		jsonObj.put("bizName", "search");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		// 请求通道支付接口
		String doGet = HttpUtils.doGet(URL);

		// 请求返回的数据解密
		String decrypt = des.decrypt(doGet);

		JSONObject jsonInfo = JSONObject.fromObject(decrypt);

		LOG.info("订单查询  jsonInfo====" + jsonInfo);

		JSONObject head = jsonInfo.getJSONObject("head");

		LOG.info("订单查询   head====" + head);

		String resultCode = head.getString("result_code");

		String resultMsg = head.containsKey("result_msg")?head.getString("result_msg"):"";
		

		LOG.info("订单查询  resultCode===" + resultCode);
		LOG.info("订单查询  resultMsg===" + resultMsg);

		if ("SUCCESS".equalsIgnoreCase(resultCode)) {
			LOG.info("订单查询结果为已成功======");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
		}else if("SEND".equalsIgnoreCase(resultCode)) {
			LOG.info("订单查询结果为待处理======");
			
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
			
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
		}

		return maps;
	}

	// 支付接口异步回调接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wf/paynotify_call")
	public void wfQuickPayNotifyCallback(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="data") String data
	) throws Exception {
		LOG.info("支付接口异步回调进来了");
		
		Map<String, String> maps = new HashMap<String, String>();

		DESPlus des = new DESPlus(secretKey);
		String decrypt = des.decrypt(data);

		LOG.info("支付接口异步回调   decrypt====" + decrypt);

		JSONObject jsonInfo1 = JSONObject.fromObject(decrypt);

		LOG.info("支付接口异步回调   jsonInfo====" + jsonInfo1);

		JSONObject head = jsonInfo1.getJSONObject("head");
		JSONObject content = jsonInfo1.getJSONObject("content");

		LOG.info("支付接口异步回调   head====" + head);
		LOG.info("支付接口异步回调   content====" + content);

		String resultCode = head.getString("result_code");

		String resultMsg = head.getString("result_msg");

		LOG.info("支付接口异步回调  resultCode===" + resultCode);
		LOG.info("支付接口异步回调  resultMsg===" + resultMsg);

		String orderNo = "";
		if ("null" != content.toString() && !"null".equals(content)) {
			orderNo = content.getString("order_no");
		}
		
		LOG.info("orderNo======"+orderNo);
		if ("SUCCESS".equalsIgnoreCase(resultCode)) {
			LOG.info("异步通知支付已成功");

//			RestTemplate restTemplate = new RestTemplate();
//			URI uri = util.getServiceUrl("creditcardmanager", "error url request!");
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			// **更新订单状态*//*
			// **调用下单，需要得到用户的订单信息*//*
//			restTemplate = new RestTemplate();

//			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+orderNo+"====================" + result);

			LOG.info("订单已支付!");

			PrintWriter writer = response.getWriter();
			writer.print("204");
			writer.flush();
			writer.close();

		}
		
	}

	// 代付接口异步回调接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wf/transfernotify_call")
	public @ResponseBody Object wfTransferNotifyCallback(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="data") String data

	) throws Exception {
		LOG.info("代付接口异步回调进来了");
		Map<String, String> maps = new HashMap<String, String>();

		DESPlus des = new DESPlus(secretKey);
		String decrypt = des.decrypt(data);

		LOG.info("代付接口异步回调   decrypt====" + decrypt);

		JSONObject jsonInfo1 = JSONObject.fromObject(decrypt);

		LOG.info("代付接口异步回调   jsonInfo====" + jsonInfo1);

		JSONObject head = jsonInfo1.getJSONObject("head");
		JSONObject content = jsonInfo1.getJSONObject("content");

		LOG.info("代付接口异步回调   head====" + head);
		LOG.info("代付接口异步回调   content====" + content);

		String resultCode = head.getString("result_code");

		String resultMsg = head.getString("result_msg");

		LOG.info("代付接口异步回调  resultCode===" + resultCode);
		LOG.info("代付接口异步回调  resultMsg===" + resultMsg);

		String orderNo = "";
		if ("null" != content.toString() && !"null".equals(content)) {
			orderNo = content.getString("order_no");
		}
		
		LOG.info("orderNo======"+orderNo);
		if ("SUCCESS".equalsIgnoreCase(resultCode)) {
			LOG.info("异步通知代付已成功");

//			RestTemplate restTemplate = new RestTemplate();
//			URI uri = util.getServiceUrl("creditcardmanager", "error url request!");
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			// **更新订单状态*//*
			// **调用下单，需要得到用户的订单信息*//*
//			restTemplate = new RestTemplate();

//			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+orderNo+"====================" + result);

			LOG.info("订单已代付!");

			PrintWriter writer = response.getWriter();
			writer.print("204");
			writer.flush();
			writer.close();
		}
		return null;
	}

	// 页面中转接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/towfbankinfo")
	public String towfbankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String userName = request.getParameter("userName");
		String idCard = request.getParameter("idCard");
		String nature = request.getParameter("nature");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String phone = request.getParameter("phone");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("userName", userName);
		model.addAttribute("idCard", idCard);
		model.addAttribute("nature", nature);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("phone", phone);
		model.addAttribute("ipAddress", ipAddress);

		return "wfbankinfo";
	}

}