package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CardEvaluation;
import com.jh.paymentchannel.pojo.CardEvaluationHistory;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cardevaluation.HttpJsonUrl;

import cn.hihippo.util.ParamsSign;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CardEvaluationPageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CardEvaluationPageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

/*	private String Url = "http://tst.txjk.enjoyfin.cn:15202/opengw/router/rest.htm";
	
	private String orgNo = "JDKJ0000001";
	private String terminalId = "T1038117";
	private String terminalMac = "59E090DA6E6440A9A4BBBFC775320F51";
	private String memberNo = "18774800063";
	private String paymentPwd = "324366";
	private String appKey = "100222";
	private String secret = "74885811-f541-4466-a1d5-bcbaf67b7c60";*/
	
	private String appSecret = "6E1D355C8A2FGR03FD4AA6F3106B2DF2";

	private String account = "shfgwl";

	//下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/payforquery/creditcard")
	public @ResponseBody Object withdraw(HttpServletRequest request, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
			//@RequestParam(value = "amount", required = false, defaultValue = "18") String amount, 
			@RequestParam(value = "order_desc", required = false, defaultValue = "信用卡测评") String orderdesc
	) {

		Map<String, Object> map = new HashMap<String, Object>();

		/** 首先看在不在黑名单里面，如果在不能登录 */
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("risk", "error url request!");
		String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		/** 0为登陆操作 */
		requestEntity.add("operation_type", "2");// 0 表示登陆无法进行 1表示无法充值 2 表示无法提现
													// 3 无法支付
		JSONObject jsonObject;
		String rescode;
		String result;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			rescode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("==========/v1.0/risk/blackwhite/query/phone查询黑白名单异常===========",e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询用户黑白名单异常,请稍后重试!");
		}
		if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
			
			return ResultWrap.init(CommonConstants.FALIED, "系统正在维护中");
		}

		long lbrandId = -1;
		try {
			lbrandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e2) {
			lbrandId = -1;
		}
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/phone";
		/** 根据的用户手机号码查询用户的基本信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", lbrandId + "");
		restTemplate = new RestTemplate();
		JSONObject resultObju;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObju = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询用户数据异常,请稍后重试!");
		}
		String userId = "0";// 0表示没有用户，
		if (resultObju.containsKey("id")) {
			userId = resultObju.getString("id");
		}
		String brandId = "-1";// 给贴牌赋值初始值为空
		if (resultObju.containsKey("brandId")) {
			brandId = resultObju.getString("brandId");
		}

		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
			return "error";
		}
		
		String amount = resultObjb.getString("brandDescription");
		
		if (Tools.checkAmount(amount) == false) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}
		
		//余额对比
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/account/query/phone";
		/** 根据的用户手机号码查询用户的余额 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		restTemplate = new RestTemplate();
		JSONObject userAccounmt;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			userAccounmt = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/account/query/phone根据的用户手机号码查询用户的余额异常===========" + e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询用户账户数据异常,请稍后重试!");
		}
		String balance = "0";
		if (userAccounmt.containsKey("balance")) {
			balance = userAccounmt.getString("balance");
		}
		if (Double.parseDouble(amount) > Double.parseDouble(balance)) {
			
			return ResultWrap.init(CommonConstants.FALIED, "您的账户余额不足,请充值!");
		}

		/** 调用下单，需要得到用户的订单信息 */
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/add";

		/** 根据的用户手机号码查询用户的基本信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", "CARD_EVA");
		requestEntity.add("desc", orderdesc);
		JSONObject resultObj;
		String order;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add 生成订单出错===========" ,e);
			
			return ResultWrap.init(CommonConstants.FALIED, "生成订单异常,请稍后重试!");
		}

		return ResultWrap.init(CommonConstants.SUCCESS, "下单成功", order);
	}

	
	// 查询测评历史记录
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cardeva/queryhistory")
	public @ResponseBody Object verifyQuery(HttpServletRequest request,
			@RequestParam(value = "bankCard", required = false) String bankCard,
			@RequestParam(value = "userId") String userId
			) throws Exception {

		List<CardEvaluationHistory> cardEvaluationHistoryByUserId;
		try {
			cardEvaluationHistoryByUserId = topupPayChannelBusiness.getCardEvaluationHistoryByUserId(userId);
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionUtil.errInfo(e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询出错啦,请稍后重试!");
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", cardEvaluationHistoryByUserId);
	}
	
	
	// 获取短信验证码接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cardeva/smscode")
	public @ResponseBody Object getSMSCode(HttpServletRequest request,
			@RequestParam(value = "orderCode", required = false) String orderCode,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "idCard", required = false) String idCard,
			@RequestParam(value = "phone", required = false) String mobile,
			@RequestParam(value = "bankCard", required = false) String bankCard
			) throws Exception {

		TreeMap<String, String> paramsMap = new TreeMap<String, String>();
		paramsMap.put("account", account);
		paramsMap.put("bankCard", bankCard);
		paramsMap.put("idCard", idCard);
		paramsMap.put("mobile", mobile);
		paramsMap.put("name", userName);

		Map<String, String> tmap = new HashMap<String, String>();
		tmap.put("account", account);
		tmap.put("bankCard", bankCard);
		tmap.put("idCard", idCard);
		tmap.put("mobile", mobile);
		tmap.put("name", userName);
		tmap.put("sign", ParamsSign.value(paramsMap, appSecret));

		LOG.info("获取短信验证码的请求报文======" + tmap);
		HttpJsonUrl ac = new HttpJsonUrl("http://api.hihippo.cn/api/authorizedSms");
		String post = ac.post(JSONObject.fromObject(tmap).toString());

		LOG.info("请求获取短信验证码返回的post======" + post);

		JSONObject fromObject1 = JSONObject.fromObject(post);

		String code = fromObject1.getString("code");
		String msg = fromObject1.getString("msg");
		String result = fromObject1.getString("result");

		if ("0000".equals(code) && "0".equals(result)) {
			JSONObject data = fromObject1.getJSONObject("data");
			String serialNumber = data.getString("serialNumber");
			
			CardEvaluation cardEvaluation = topupPayChannelBusiness.getCardEvaluationByBankCard(bankCard);
			if (cardEvaluation == null) {

				CardEvaluation ce = new CardEvaluation();
				ce.setPhone(mobile);
				ce.setIdCard(idCard);
				ce.setBankCard(bankCard);
				ce.setName(userName);
				ce.setSerialNumber(serialNumber);

				topupPayChannelBusiness.createCardEvaluation(ce);
			} else {

				cardEvaluation.setSerialNumber(serialNumber);

				topupPayChannelBusiness.createCardEvaluation(cardEvaluation);
			}

			return ResultWrap.init(CommonConstants.SUCCESS, "短信发送成功");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "短信发送失败,失败原因: " + msg);
		}
		
	}

	// 银行卡验证接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cardeva/evabankcard")
	public @ResponseBody Object evaBankCard(HttpServletRequest request,
			@RequestParam(value = "orderCode", required = false) String orderCode,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "idCard", required = false) String idCard,
			@RequestParam(value = "phone", required = false) String mobile,
			@RequestParam(value = "bankCard", required = false) String bankCard,
			@RequestParam(value = "smsCode") String smsCode)
		throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, queryOrdercode.get("resp_message"));
			return map;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String amount = resultObj.getString("amount");
		String phone = resultObj.getString("phone");
		
		CardEvaluation cardEvaluation = topupPayChannelBusiness.getCardEvaluationByBankCard(bankCard);

		TreeMap<String, String> paramsMap = new TreeMap<String, String>();
		paramsMap.put("account", account);
		paramsMap.put("orderNo", orderCode);
		paramsMap.put("name", userName);
		paramsMap.put("idCard", idCard);
		paramsMap.put("mobile", mobile);
		paramsMap.put("bankCard", bankCard);
		paramsMap.put("identifyingCode", smsCode);
		paramsMap.put("serialNumber", cardEvaluation.getSerialNumber());

		Map<String, String> tmap = new HashMap<String, String>();
		tmap.put("account", account);
		tmap.put("orderNo", orderCode);
		tmap.put("name", userName);
		tmap.put("idCard", idCard);
		tmap.put("mobile", mobile);
		tmap.put("bankCard", bankCard);
		tmap.put("identifyingCode", smsCode);
		tmap.put("serialNumber", cardEvaluation.getSerialNumber());
		tmap.put("sign", ParamsSign.value(paramsMap, appSecret));

		URI uri = util.getServiceUrl("user", "error url request!");
		String url1 = uri.toString() + "/v1.0/user/account/query/phone";
		/** 根据的用户手机号码查询用户的余额 */
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		restTemplate = new RestTemplate();
		JSONObject userAccounmt;
		try {
			String result = restTemplate.postForObject(url1, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			userAccounmt = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/account/query/phone根据的用户手机号码查询用户的余额异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}
		String balance = "0";
		if (userAccounmt.containsKey("balance")) {
			balance = userAccounmt.getString("balance");
		}
		if (Double.parseDouble(amount) > Double.parseDouble(balance)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您的账户余额不足,请充值!");
			return map;
		}
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url1 = uri.toString() + "/v1.0/user/account/withdraw/freeze";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		requestEntity.add("realamount", amount);
		requestEntity.add("order_code", orderCode);
		// resultObj = jsonObject.getJSONObject("result");
		String withdrawrespcode;
		try {
			String result = restTemplate.postForObject(url1, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			withdrawrespcode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add判断用户的真实提现金额和想提现的金额比较异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}
		if ("999999".equalsIgnoreCase(withdrawrespcode)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_BALANCE_NO_ENOUGH);
			map.put(CommonConstants.RESP_MESSAGE, "您的账户余额不足,请充值!");
			return map;

		}
		
		LOG.info("银行卡验证的请求报文======" + tmap);
		
		HttpJsonUrl ac = new HttpJsonUrl("http://api.hihippo.cn/api/reportCardNoData");
		String post = ac.post(JSONObject.fromObject(tmap).toString());

		LOG.info("请求银行卡验证返回的post======" + post);

		JSONObject fromObject1 = JSONObject.fromObject(post);

		String code = fromObject1.getString("code");
		String msg = fromObject1.getString("msg");
		String result = fromObject1.getString("result");

		if ("0000".equals(code) && "0".equals(result)) {
			JSONObject data = fromObject1.getJSONObject("data");
			String url = data.getString("url");
					
					CardEvaluationHistory ceh = new CardEvaluationHistory();
					ceh.setUserId(userId);
					ceh.setPhone(mobile);
					ceh.setIdCard(idCard);
					ceh.setBankCard(bankCard);
					ceh.setName(userName);
					ceh.setUrl(url);
					
					topupPayChannelBusiness.createCardEvaluationHistory(ceh);
					
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESULT, url);
					map.put(CommonConstants.RESP_MESSAGE, "成功");

					RestTemplate restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					String Url = uri.toString() + "/v1.0/transactionclear/payment/update";
					//** 根据的用户手机号码查询用户的基本信息 *//*
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("order_code", orderCode);
					requestEntity.add("status", "1");
					LOG.info("接口(/v1.0/transactionclear/payment/update)参数================" + requestEntity.toString());
					try {
						result = restTemplate.postForObject(Url, requestEntity, String.class);
						LOG.info("接口(/v1.0/transactionclear/payment/update)-result================" + result);
					} catch (RestClientException e) {
						LOG.error("==========/v1.0/transactionclear/payment/update异常===========" + e);
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
						return map;
					}
					
					return map;
				}else {
					
					unFreezeAccount(orderCode, Long.parseLong(userId), amount);
					
					return ResultWrap.init(CommonConstants.FALIED, msg);
				}
				
		}

	
	public void unFreezeAccount(String order, long userid, String amount) {
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/account/freeze";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", order);
		requestEntity.add("user_id", userid + "");
		requestEntity.add("amount", amount);
		requestEntity.add("add_or_sub", "1");
		try {
			String result = restTemplate.postForObject(url, requestEntity, String.class);
		} catch (RestClientException e) {
			LOG.error("==========/v1.0/user/account/freeze冻结余额异常===========" + e);
		}
	}

}