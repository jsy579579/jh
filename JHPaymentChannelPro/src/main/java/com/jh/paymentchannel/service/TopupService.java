package com.jh.paymentchannel.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sound.sampled.Line;

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
import com.jh.paymentchannel.pojo.ChannelDetail;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.TopupPayChannelRoute;
import com.jh.paymentchannel.pojo.WeixinResult;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class TopupService extends BaseChannel{

	private static final Logger LOG = LoggerFactory.getLogger(TopupService.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private PaymentChannelFactory factory;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private Util util;
	
	@Value("${paymentgateway.url}")
	private String paymentGateUrl;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;

	/** 发起请求 请求url, 并返回跳装的url */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/request")
	public @ResponseBody Object topupRequest(HttpServletRequest request,
											 @RequestParam(value = "ordercode") String ordercode,
											 @RequestParam(value = "orderdesc", required = false) String orderdesc,
											 @RequestParam(value = "amount", required = false) String amount,
											 @RequestParam(value = "extra", required = false, defaultValue = "") String authcode,
											 @RequestParam(value = "userid", required = false, defaultValue = "") String userid,
											 @RequestParam(value = "brandcode", required = false, defaultValue = "0") String brandcode,
											 @RequestParam(value = "channel_type", required = false, defaultValue = "0") String channelType,
											 @RequestParam(value = "channel_tag", required = false) String channelTag
	) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (Tools.checkAmount(amount) == false) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_AMOUNT_ERROR);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}

		LOG.info("orderCode====="+ordercode+"=====brandcode====="+brandcode+"=====channelType====="+channelType+"=====channelTag====="+channelTag);

		TopupPayChannelRoute payChannelRoute;
		String channelParams;
		String notifyURL;
		String returnURL;
		TopupRequest paymentRequest;
		JSONObject resultJSON = null;
		Map<String,Object> params = new HashMap<>();
		params.put("extra", authcode);
		params.put("request", request);
		try {
			payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandcode(brandcode,channelType, channelTag);
			LOG.info("orderCode====="+ordercode+"=====payChannelRoute==========="+payChannelRoute);

			ChannelDetail channelDetail = topupPayChannelBusiness.getChannelDetailByTag(payChannelRoute.getTargetChannelTag());

			/** 更新订单的是否自动清算 */
			RestTemplate restTemplate = new RestTemplate();
			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update/autoclearing";

			/** 根据的用户手机号码查询用户的基本信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("auto_clearing", channelDetail.getAutoclearing());
			requestEntity.add("order_code", ordercode);
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSON = JSONObject.fromObject(resultString);
			if(!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))){
				return ResultWrap.init(CommonConstants.FALIED, resultJSON.getString(CommonConstants.RESP_MESSAGE));
			}

			resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
			PaymentOrder paymentOrder = (PaymentOrder) JSONObject.toBean(resultJSON, PaymentOrder.class);

			channelParams = channelDetail.getChannelParams();
			notifyURL = channelDetail.getNotifyURL();
			returnURL = channelDetail.getReturnURL();

			params.put("paymentOrder", paymentOrder);
			params.put("notifyURL", notifyURL);
			params.put("returnURL", returnURL);
			params.put("channelParams", channelParams);
			params.put("channelTag", payChannelRoute.getTargetChannelTag());

			paymentRequest = factory.getTopupChannelRequest(payChannelRoute.getTargetChannelTag());
		} catch (RestClientException e1) {
			e1.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}

		if(paymentRequest == null) {

//			Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
//			Object object = queryOrdercode.get("result");
//			JSONObject fromObject = JSONObject.fromObject(object);
//			JSONObject resultObj = fromObject.getJSONObject("result");
			LOG.info("orderCode====="+ordercode);
			JSONObject resultObj = resultJSON;

			String bankCard = resultObj.getString("bankcard");
			String realAmount = resultObj.getString("realAmount");
			String userId = resultObj.getString("userid");
			String rate = resultObj.getString("rate");
			String extraFee = resultObj.getString("extraFee");
			String orderType = resultObj.getString("type");
			String phone = resultObj.getString("phone");

			String userName = null;
			String idCard = null;
			String creditCardPhone = null;
			String creditCardBankName = null;
			String creditCardNature = null;
			String creditCardCardType = null;
			String expiredTime = null;
			String securityCode = null;
			JSONObject fromObject = null;
			if(bankCard != null && !"".equals(bankCard) && !"null".equals(bankCard)) {

				Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

				Object object2 = queryBankCardByCardNoAndUserId.get("result");
				fromObject = JSONObject.fromObject(object2);

				userName = fromObject.getString("userName");
				idCard = fromObject.getString("idcard");
				creditCardPhone = fromObject.getString("phone");
				creditCardBankName = fromObject.getString("bankName");
				creditCardNature = fromObject.getString("nature");
				creditCardCardType = fromObject.getString("cardType");
				expiredTime = fromObject.getString("expiredTime");
				securityCode = fromObject.getString("securityCode");

				if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {
					expiredTime = "";
				}

				if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {
					securityCode = "";
				}

			}

			String debitCardNo = null;
			String debitPhone = null;
			String debitBankName = null;
			String debitCardNature = null;
			String debitCardCardType = null;
			if("0".equals(orderType) || "2".equals(orderType)) {

				Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
				if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, queryBankCardByUserId.get("resp_message"));
					return map;
				}


				Object object3 = queryBankCardByUserId.get("result");
				fromObject = JSONObject.fromObject(object3);

				debitCardNo = fromObject.getString("cardNo");
				debitPhone = fromObject.getString("phone");
				debitBankName = fromObject.getString("bankName");
				debitCardNature = fromObject.getString("nature");
				debitCardCardType = fromObject.getString("cardType");

			}

			JSONObject jsonObject = new JSONObject();

			jsonObject.put("orderCode", ordercode);
			jsonObject.put("amount", amount);
			jsonObject.put("bankCard", bankCard);
			jsonObject.put("realAmount", realAmount);
			jsonObject.put("userId", userId);
			jsonObject.put("rate", rate);
			jsonObject.put("extraFee", extraFee);
			jsonObject.put("userName", userName);
			jsonObject.put("idCard", idCard);
			jsonObject.put("creditCardPhone", creditCardPhone);
			jsonObject.put("creditCardBankName", creditCardBankName);
			jsonObject.put("creditCardNature", creditCardNature);
			jsonObject.put("creditCardCardType", creditCardCardType);
			jsonObject.put("expiredTime", expiredTime);
			jsonObject.put("securityCode", securityCode);
			jsonObject.put("debitCardNo", debitCardNo);
			jsonObject.put("debitPhone", debitPhone);
			jsonObject.put("debitBankName", debitBankName);
			jsonObject.put("debitCardNature", debitCardNature);
			jsonObject.put("debitCardCardType", debitCardCardType);
			jsonObject.put("channelTag", channelTag);
			jsonObject.put("orderType", orderType);
			jsonObject.put("extra", authcode);
			jsonObject.put("phone", phone);
			jsonObject.put("ipAddress", ipAddress);

			RestTemplate rt = new RestTemplate();
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();

			multiValueMap.add("data", jsonObject.toString());

			LOG.info(jsonObject.toString());
			LOG.info("paymentGateUrl"+paymentGateUrl);

			String postForObject = rt.postForObject(paymentGateUrl, multiValueMap, String.class);

			return postForObject;

		}

		Map<String, String> result = new HashMap<String, String>();
		try {
			LOG.info(params.toString());
			result = paymentRequest.topupRequest(params);
		} catch (Exception e) {
			LOG.error("==========请求通道异常===========");
			e.printStackTrace();LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}
		LOG.info("result===="+result);
		if(result.get(CommonConstants.RESP_CODE) == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.由于未知原因,充值失败了哦,请重试~");
			return map;
		}
		if (result.get(CommonConstants.RESP_CODE).equalsIgnoreCase("success")) {
			if (result.get("channel_type").equalsIgnoreCase("quick")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			} else if (result.get("channel_type").equalsIgnoreCase("sdj")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				//map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			} else if (result.get("channel_type").equalsIgnoreCase("jf")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			} else if (result.get("channel_type").equalsIgnoreCase("jfcoin")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			}else if (result.get("channel_type").equalsIgnoreCase("jfapi")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			}else if (result.get("channel_type").equalsIgnoreCase("yh")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;

			}else if (result.get("channel_type").equalsIgnoreCase("elong")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("result"));
				return map;

			} else if (result.get("channel_type").equalsIgnoreCase("weixin")) {

				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				WeixinResult weixinResult = new WeixinResult();
				LOG.info(result.get("code_url"));
				weixinResult.setCodeUrl(result.get("code_url"));
				weixinResult.setImgUrl(result.get("img_url"));
				map.put(CommonConstants.RESULT, weixinResult);
				return map;

			} else if (result.get("channel_type").equalsIgnoreCase("alipay")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				WeixinResult weixinResult = new WeixinResult();
				LOG.info(result.get("code_url"));
				weixinResult.setCodeUrl(result.get("code_url"));
				weixinResult.setImgUrl(result.get("img_url"));

				map.put(CommonConstants.RESULT, weixinResult);
				return map;
			} else if (result.get("channel_type").equalsIgnoreCase("kuaijie")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			} else if (result.get("channel_type").equalsIgnoreCase("wftb2c")) {
				LOG.info("==================页面跳转==================");
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("url"));
				return map;
			} else if (result.get("channel_type").equalsIgnoreCase("yb")) {
				LOG.info("==================页面跳转==================");
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			} else if (result.get("channel_type").equalsIgnoreCase("quickpay")) {
				if (result.get("resp_code").equalsIgnoreCase("success")) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
					map.put(CommonConstants.RESULT, result.get("redirect_url"));
					return map;
				} else {
					map.put(CommonConstants.RESP_MESSAGE, "失败");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					return map;
				}
			} else if (result.get("channel_type").equalsIgnoreCase("sf_pay")) {
				if (result.get("resp_code").equalsIgnoreCase("success")) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
					map.put(CommonConstants.RESULT, result.get("url"));
					return map;
				} else {
					map.put(CommonConstants.RESP_MESSAGE, "失败");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					return map;
				}
			} else if (result.get("channel_type").equalsIgnoreCase("hl_pay")) {
				if ("nourl".equals(result.get("type"))) {
					LOG.info("====================" + "hl_pay:success" + "======================");
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
					LOG.info("==============map==============" + map.toString());
					return map;
				}
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, result.get("url"));
				map.put(CommonConstants.RESP_MESSAGE, "成功");
				return map;
			}else if (result.get("channel_type").equalsIgnoreCase("hlb")) {
				if (result.get("resp_code").equalsIgnoreCase("success")) {
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "成功");
					map.put(CommonConstants.RESULT, result.get(CommonConstants.RESULT));
					LOG.info("进来了：" + result.get(CommonConstants.RESULT));
					return map;
				} else {
					map.put(CommonConstants.RESP_MESSAGE, "失败");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					return map;
				}
			}

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, result.get("redirect_url"));
			return map;


			// 失败
		} else {
			String message = "";
			RestTemplate restTemplate = new RestTemplate();
			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode",  ordercode+ "");
			message = result.containsKey(CommonConstants.RESP_MESSAGE)?result.get(CommonConstants.RESP_MESSAGE):"失败原因未知!";
			requestEntity.add("remark",message);
			restTemplate.postForObject(url, requestEntity, String.class);
			if (result.get("channel_type") == null || result.get("redirect_url") == null) {
				//log.error("失败原因==========" + result);
				message = result.get(CommonConstants.RESP_MESSAGE);
				String respCode = result.get(CommonConstants.RESP_CODE);
				map.put(CommonConstants.RESP_CODE,respCode==null?CommonConstants.FALIED:respCode);
				map.put(CommonConstants.RESP_MESSAGE,"".equals(message) || null == message ? "亲.由于未知原因支付失败了哦,请稍后重试~" : message);
				return map;
			}

			if (result.get("channel_type").equalsIgnoreCase("sdj")) {
				map.put(CommonConstants.RESP_CODE, result.get("resp_code"));
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("jf")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("jfcoin")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("jfapi")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("yh")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("elong")) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				// map.put(CommonConstants.RESULT, result.get("redirect_url"));
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("yb")) {
				map.put(CommonConstants.RESP_MESSAGE, result.get("resp_message"));
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("sf_pay")) {
				map.put(CommonConstants.RESP_MESSAGE, result.get("message"));
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("hl_pay")) {
				map.put(CommonConstants.RESP_MESSAGE, result.get("message"));
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("hlb")) {
				map.put(CommonConstants.RESP_MESSAGE, result.get(CommonConstants.RESP_MESSAGE));
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				return map;
			}
			if (result.get("channel_type").equalsIgnoreCase("wftb2c")) {
				if (result.get("resp_code").equalsIgnoreCase("paying")) {
					LOG.info("==================等待对方支付中==================");
					map.put(CommonConstants.RESP_MESSAGE, "等待对方支付中");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					return map;
				} else {
					map.put(CommonConstants.RESP_MESSAGE, "收款失败");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESULT, result.get("url"));
					return map;
				}
			}
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);

			if (result.get(CommonConstants.RESP_MESSAGE) != null
					&& !result.get(CommonConstants.RESP_MESSAGE).equalsIgnoreCase("")) {
				map.put(CommonConstants.RESP_MESSAGE, result.get(CommonConstants.RESP_MESSAGE));
			} else {
				map.put(CommonConstants.RESP_MESSAGE, "失败");
			}

			/*
			 * else if(result.get("channel_type").equalsIgnoreCase(
			 * "kuaijie_sdj_bindcardsuccess")){
			 * map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			 * map.put(CommonConstants.RESP_MESSAGE, "失败"); return map; }
			 */

			/*
			 * else
			 * if(result.get("channel_type").equalsIgnoreCase("jf_payerror")){
			 * map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			 * map.put(CommonConstants.RESP_MESSAGE, "失败");
			 * map.put(CommonConstants.RESULT, result.get("redirect_url"));
			 * return map; }
			 */

			/*
			 * map.put(CommonConstants.RESP_CODE,CommonConstants.FALIED);
			 * map.put(CommonConstants.RESP_MESSAGE, "失败");
			 * map.put(CommonConstants.RESULT, result.get("redirect_url"));
			 * return map;
			 */

		}
		return map;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/out/topup/request")
	public @ResponseBody Object outTopRequest(HttpServletRequest request,
			@RequestParam(value="orderCode")String orderCode
			){
		String url = "http://transactionclear/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
		JSONObject resultJSON = JSONObject.fromObject(resultString);
		if(!CommonConstants.SUCCESS.equals(resultJSON.getString(CommonConstants.RESP_CODE))){
			return ResultWrap.init(CommonConstants.FALIED,resultJSON.getString(CommonConstants.RESP_MESSAGE));
		}
		
		JSONObject orderJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
		String status = orderJSON.getString("status");
		if(CommonConstants.ORDER_SUCCESS.equals(status)){
			return ResultWrap.init(CommonConstants.ERRRO_ORDER_HAS_CHECKED, "订单已成功");
		}
		
		String channelTag = orderJSON.getString("channelTag");
		return this.topupRequest(request, orderCode, null, null, null, null, null, null, channelTag);
	}
	

}
