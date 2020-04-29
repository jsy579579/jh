package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.CardEvaluationBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CardEvaInfo;
import com.jh.paymentchannel.pojo.CardEvaluation;
import com.jh.paymentchannel.pojo.CardEvaluationHistory;
import com.jh.paymentchannel.pojo.UserQueryCount;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cardevaluation.HttpJsonUrl;

import cn.hihippo.util.ParamsSign;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CardEvaluationNewPageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CardEvaluationNewPageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private CardEvaluationBusiness cardEvaluationBusiness;

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
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 支付宝下单接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/cardevaluationnew/add/alipayordercode")
	public @ResponseBody Object addAliPayOrderCodeByCardEvaluation(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String brandId,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "channe_tag", required = false, defaultValue = "SPALI_PAY") String channeltag,
			@RequestParam(value = "order_desc", required = false, defaultValue = "信用卡测评支付宝付款订单") String orderdesc, Model model) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id 查询贴牌信息异常===========", e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询贴牌信息异常");
		}

		String amount = resultObjb.getString("brandDescription");

		if (Tools.checkAmount(amount) == false) {

			return ResultWrap.init(CommonConstants.FALIED, "支付金额有错");
		}

		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/add";

		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channeltag);
		requestEntity.add("desc", orderdesc);
		requestEntity.add("notify_url", ipAddress + "/v1.0/paymentchannel/cardevaluationnew/alipay/notify_call");
		String order;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请求下单异常,请稍后重试!");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求下单异常,请稍后重试!", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求下单异常,请稍后重试!", "UTF-8"));
			return map;
		}

		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/topup/request";

		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("amount", amount);
		requestEntity.add("ordercode", order);
		requestEntity.add("brandcode", brandId);
		requestEntity.add("orderdesc", orderdesc);
		requestEntity.add("channel_tag", channeltag);
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "请求交易异常,请稍后重试!");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求交易异常,请稍后重试!", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("请求交易异常,请稍后重试!", "UTF-8"));
			return map;
		}
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (!CommonConstants.SUCCESS.equals(respCode)) {
			if ("999990".equals(respCode)) {
				map.put(CommonConstants.RESP_CODE, "999990");
				map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
				return map;
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "请求支付失败,请稍后重试!");
				map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("请求支付失败,请稍后重试!", "UTF-8"));
				response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("请求支付失败,请稍后重试!", "UTF-8"));
				return map;
			}
		}

		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// 判断是否需要购买的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/queryishavecount")
	public @ResponseBody Object queryIsAddOrderCode(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId") String brandId) {

		RestTemplate restTemplate = new RestTemplate();

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id 查询贴牌信息异常===========", e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询贴牌信息异常");
		}

		String amount = resultObjb.getString("brandDescription");

		if (Tools.checkAmount(amount) == false) {

			return ResultWrap.init(CommonConstants.FALIED, "支付金额异常,请联系客服设置支付金额!");
		}
		
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId + "");
		JSONObject resultObju;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObju = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
			return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
		}
		String userId = "0";
		if (resultObju.containsKey("id")) {
			userId = resultObju.getString("id");
		} else {
			userId = "0";
		}
		
		UserQueryCount userQueryCountByUserId = cardEvaluationBusiness.getUserQueryCountByUserId(userId);

		if (userQueryCountByUserId != null) {
			int queryCount = userQueryCountByUserId.getQueryCount();
			if (queryCount > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "已购买过查询次数!", amount);
			} else {

				return ResultWrap.init("666666", "需要充值!", amount);
			}

		} else {

			return ResultWrap.init("666666", "需要充值!", amount);
		}

	}

	
	// 下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/addordercode")
	public @ResponseBody Object addOrderCode(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId, @RequestParam(value = "phone") String phone) {

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
		}

		String amount = resultObjb.getString("brandDescription");

		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/phone";
			/** 根据的用户手机号码查询用户的基本信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId + "");
			JSONObject resultObju;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================purchase" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObju = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
				return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
			}
			String userId = "0";
			if (resultObju.containsKey("id")) {
				userId = resultObju.getString("id");
			} else {
				userId = "0";
			}

			UserQueryCount userQueryCountByUserId = cardEvaluationBusiness.getUserQueryCountByUserId(userId);

			if (userQueryCountByUserId != null) {
				int queryCount = userQueryCountByUserId.getQueryCount();
				if (queryCount > 0) {

					String orderCode = this.addOrder(phone, amount, "0", "CARD_EVA", "信用卡测评");

					if (orderCode != null) {

						return ResultWrap.init(CommonConstants.SUCCESS, "下单成功!", orderCode);
					} else {

						return ResultWrap.init(CommonConstants.FALIED, "下单失败,请稍后重试!");
					}
				} else {

					return ResultWrap.init("666666", "需要充值!");
				}

			} else {

				return ResultWrap.init("666666", "需要充值!");
			}
	}

	// 查询测评历史记录
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/queryhistory")
	public @ResponseBody Object verifyQuery(HttpServletRequest request,
			@RequestParam(value = "bankCard", required = false) String bankCard,
			@RequestParam(value = "userId") String userId) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		List<CardEvaluationHistory> cardEvaluationHistoryByUserId;
		try {
			cardEvaluationHistoryByUserId = topupPayChannelBusiness.getCardEvaluationHistoryByUserId(userId);
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionUtil.errInfo(e);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询出错啦,请稍后重试!");

			return maps;
		}

		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESULT, cardEvaluationHistoryByUserId);
		maps.put(CommonConstants.RESP_MESSAGE, "查询成功");

		return maps;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/createcardevainfo")
	public @ResponseBody Object createCardEvaInfo(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "bankCard") String bankCard) throws Exception {
		
		CardEvaInfo cardEvaInfoByUserId = cardEvaluationBusiness.getCardEvaInfoByUserId(Long.parseLong(userId));
		
		if(cardEvaInfoByUserId == null) {
			CardEvaInfo cardEvaInfo = new CardEvaInfo();
			
			cardEvaInfo.setUserId(Long.parseLong(userId));
			cardEvaInfo.setBankCard(bankCard);
			cardEvaInfo.setIdCard(idCard);
			cardEvaInfo.setPhone(phone);
			cardEvaInfo.setUserName(userName);
			
			cardEvaluationBusiness.createCardEvaInfo(cardEvaInfo);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "添加数据成功!");
			
		}else {
			
			cardEvaInfoByUserId.setBankCard(bankCard);
			cardEvaInfoByUserId.setIdCard(idCard);
			cardEvaInfoByUserId.setPhone(phone);
			cardEvaInfoByUserId.setUserName(userName);
			cardEvaInfoByUserId.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
			
			cardEvaluationBusiness.createCardEvaInfo(cardEvaInfoByUserId);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "修改数据成功!");
		}
		
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/getcardevainfobyuserid")
	public @ResponseBody Object getCardEvaInfoByUserId(HttpServletRequest request,
			@RequestParam(value = "userId") String userId
			) throws Exception {
		
		CardEvaInfo cardEvaInfoByUserId = cardEvaluationBusiness.getCardEvaInfoByUserId(Long.parseLong(userId));
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", cardEvaInfoByUserId);
		
	}
	
	
	// 获取短信验证码接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/smscode")
	public @ResponseBody Object getSMSCode(HttpServletRequest request,
			@RequestParam(value = "orderCode", required = false) String orderCode,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "idCard", required = false) String idCard,
			@RequestParam(value = "phone", required = false) String mobile,
			@RequestParam(value = "bankCard", required = false) String bankCard) throws Exception {

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
		} else {

			this.addOrderCauseOfFailure(orderCode, "短信发送失败,失败原因: " + msg);
			
			return ResultWrap.init(CommonConstants.FALIED, "短信发送失败,失败原因: " + msg);
		}

	}

	// 银行卡测评接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/evabankcard")
	public @ResponseBody Object evaBankCard(HttpServletRequest request,
			@RequestParam(value = "orderCode", required = false) String orderCode,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "idCard", required = false) String idCard,
			@RequestParam(value = "phone", required = false) String mobile,
			@RequestParam(value = "bankCard", required = false) String bankCard,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {

			return ResultWrap.init(CommonConstants.FALIED, queryOrdercode.get("resp_message") + "");
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");

		CardEvaluation cardEvaluation = topupPayChannelBusiness.getCardEvaluationByBankCard(bankCard);

		UserQueryCount userQueryCountByUserId = cardEvaluationBusiness.getUserQueryCountByUserId(userId);

		int queryCount;
		if (userQueryCountByUserId != null) {
			queryCount = userQueryCountByUserId.getQueryCount();
			LOG.info("当前可用查询次数为：  " + queryCount);
			if (queryCount > 0) {
				queryCount = queryCount - 1;
				userQueryCountByUserId.setQueryCount(queryCount);
				cardEvaluationBusiness.createUserQueryCount(userQueryCountByUserId);

				LOG.info("当前可用查询次数为：  " + queryCount);
			} else {

				return ResultWrap.init("666666", "需要充值!");
			}
		} else {

			return ResultWrap.init("666666", "需要充值!");
		}

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

			this.updateOrderCode(orderCode, "1", "");

			return map;

		} else {

			queryCount = queryCount + 1;
			userQueryCountByUserId.setQueryCount(queryCount);
			cardEvaluationBusiness.createUserQueryCount(userQueryCountByUserId);

			LOG.info("当前可用查询次数为：  " + queryCount);

			this.addOrderCauseOfFailure(orderCode, msg);

			return ResultWrap.init(CommonConstants.FALIED, msg);
		}
	}

	// 支付宝异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/cardevaluationnew/alipay/notify_call")
	public @ResponseBody Object aliPayWapNotify(HttpServletRequest request) {
		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();

		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}

			params.put(name, valueStr);
		}
		LOG.info("支付宝WAP回调进来了============params:" + params);

		String realChannelOrderCode = params.get("trade_no");
		String orderCode = params.get("out_trade_no");
		String amount = params.get("total_amount");
		String tradeStatus = params.get("trade_status");

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
		LOG.info("resultObj======" + resultObj);
		String userId = resultObj.getString("userid");

		AlipayServiceEnvConstants alipayClient = new AlipayServiceEnvConstants();
		boolean flag = false;
		if (alipayClient != null) {
			try {
				flag = AlipaySignature.rsaCheckV1(params, AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY,
						AlipayServiceEnvConstants.CHARSET, "RSA2");
			} catch (AlipayApiException e) {
				e.printStackTrace();
				return ResultWrap.err(LOG, CommonConstants.FALIED, "验签异常");
			}
		} else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败,无支付宝密钥配置");
		}

		if (!flag) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
		}

		if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) && !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "非成功回调");
		}

		if (new BigDecimal(resultObj.getString("amount")).compareTo(new BigDecimal(amount)) != 0) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验证金额失败");
		}
		if ("1".equals(resultObj.getString("status"))) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单已处理");
		}

		RestTemplate restTemplate = new RestTemplate();

		UserQueryCount userQueryCountByUserId = cardEvaluationBusiness.getUserQueryCountByUserId(userId);

		if (userQueryCountByUserId != null) {
			LOG.info("111111111");
			userQueryCountByUserId.setQueryCount(userQueryCountByUserId.getQueryCount() + 1);
			userQueryCountByUserId
					.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

			cardEvaluationBusiness.createUserQueryCount(userQueryCountByUserId);
		} else {
			LOG.info("2222222");
			UserQueryCount userQueryCount = new UserQueryCount();

			userQueryCount.setUserId(userId);
			userQueryCount.setPhone(resultObj.getString("phone"));
			userQueryCount.setQueryCount(1);

			cardEvaluationBusiness.createUserQueryCount(userQueryCount);

		}

		this.updateOrderCodeStatus(orderCode);

		LOG.info("订单状态修改成功===================");

		LOG.info("订单已支付!");

		return "SUCCESS";
	}

	// 生成订单的方法
	public String addOrder(String phone, String amount, String type, String channelTag, String desc) {

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/add";

		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", type);
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channelTag);
		requestEntity.add("desc", desc);
		String order;
		try {
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e.getMessage());
			return null;
		}

		return order;
	}

}