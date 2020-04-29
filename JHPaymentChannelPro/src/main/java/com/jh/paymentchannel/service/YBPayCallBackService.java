package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
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
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.RegisterAuth;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YBPayCallBackService {

	private static final Logger LOG = LoggerFactory.getLogger(YBPayCallBackService.class);
	private static String key = Conts.hmacKey; // 商户秘钥

	@Autowired
	private RegisterAuthBusiness registerAuthBusiness;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	/**
	 * 请求参数 验签在数组最后
	 *
	 */
	private static NameValuePair[] param = {
			// 出款金额
			new NameValuePair("amount", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款订单号
			new NameValuePair("externalNo", ""),
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),

			// 出款方式
			new NameValuePair("transferWay", ""),

			new NameValuePair("callBackUrl", ""),

			// 签名串
			new NameValuePair("hmac", ""),

	};

	private static NameValuePair[] param1 = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款订单号
			new NameValuePair("balanceType", ""),

			// 签名串
			new NameValuePair("hmac", ""),

	};

	private static NameValuePair[] param2 = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			/*
			 * // 小商户编号 new NameValuePair("customerNumber", ""),
			 */
			// 出款订单号
			new NameValuePair("requestId", ""),

			new NameValuePair("createTimeBegin", ""), new NameValuePair("pageNo", ""),
			// 签名串
			new NameValuePair("hmac", ""),

	};

	private static NameValuePair[] param3 = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			// 小商户编号
			new NameValuePair("customerNumber", ""),
			// 出款订单号
			new NameValuePair("requestId", ""),

			new NameValuePair("transAmount", ""),

			new NameValuePair("remark", ""),
			// 签名串
			new NameValuePair("hmac", ""),

	};

	@Autowired
	Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yb/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("进入易宝回调接口================" + req.getParameterMap().toString());
		// 获取支付报文参数
		Map<String, String[]> params = req.getParameterMap();
		LOG.info("params================" + params);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : params.keySet()) {
			String[] values = params.get(key);
			if (values.length > 0) {
				map.put(key, values[0]);
			}
		}
		PrintWriter pw = res.getWriter();
		if (map.containsKey("transferStatus")) {
			String transferStatus = map.get("transferStatus");
			if ("RECEIVED".equalsIgnoreCase(transferStatus) || "PROCESSING".equalsIgnoreCase(transferStatus)
					|| "SUCCESSED".equalsIgnoreCase(transferStatus)) {
				String ordercode = map.get("externalNo");
				String externalld = map.get("serialNo");
				/** 调用下单，需要得到用户的订单信息 */
				RestTemplate restTemplate = new RestTemplate();
				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/update";
				/** 根据的用户手机号码查询用户的基本信息 */
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("third_code", externalld);
				requestEntity.add("order_code", ordercode);
				LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + ordercode + "," + externalld);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
				/** 判断是否有外放的通道的处理， 如果有那么继续回调外放哦 */
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String outMerOrdercode = resultObj.getString("outMerOrdercode");
				String orderdesc = resultObj.getString("desc");
				String phone = resultObj.getString("phone");
				String tranamount = resultObj.getString("amount");
				String channelTag = resultObj.getString("channelTag");
				String notifyURL = resultObj.getString("outNotifyUrl");
				if (outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")) {
					uri = util.getServiceUrl("channel", "error url request!");
					url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("merchant_no", phone);
					requestEntity.add("amount", tranamount);
					requestEntity.add("channel_tag", channelTag);
					requestEntity.add("order_desc", URLEncoder.encode(orderdesc, "UTF-8"));
					requestEntity.add("order_code", outMerOrdercode);
					requestEntity.add("sys_order", ordercode);
					requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
					result = restTemplate.postForObject(url, requestEntity, String.class);
				}
				pw.write("success");

			}

		} else {
			LOG.info("map================" + map);
			String status = map.get("status");
			LOG.info("status================" + status);

			if (status.equalsIgnoreCase("SUCCESS")) {
				LOG.info("==========================welcome========================");
				String ordercode = map.get("requestId");

				RestTemplate restTemplate = new RestTemplate();
				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", ordercode);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String realAmt = resultObj.getString("realAmount");
				String extraFee = resultObj.getString("extraFee");
				// BigDecimal realAmount = new BigDecimal(realAmt).add(new
				// BigDecimal(extraFee)).setScale(2,
				// BigDecimal.ROUND_HALF_UP).setScale(2,BigDecimal.ROUND_HALF_DOWN);

				BigDecimal realAmount = new BigDecimal(realAmt).setScale(2, BigDecimal.ROUND_HALF_UP).setScale(2,
						BigDecimal.ROUND_HALF_DOWN);
				// realAmt = String
				// .valueOf(Integer.valueOf(realAmt.substring(0,
				// realAmt.indexOf(".")))
				// + Integer.valueOf(extraFee.substring(0,
				// extraFee.indexOf("."))))
				// + realAmt.substring(realAmt.indexOf("."), realAmt.length());
				// BigDecimal b1 = new BigDecimal(realAmt);
				// BigDecimal b2 = new BigDecimal("0.01");
				// String realAmount = String.valueOf(new
				// Double(b1.subtract(b2).doubleValue()));
				LOG.info("代付金额=======" + realAmount.toString());
				String externalld = map.get("externalld");
				String amount = map.get("amount");
				String customerNumber = map.get("customerNumber");
				String mainCustomerNumber = Conts.customerNumber; // 代理商编码
				LOG.info("===============" + ordercode + "," + externalld + "," + amount + "," + customerNumber + ","
						+ mainCustomerNumber);
				param[0].setValue(realAmount.toString());
				param[1].setValue(customerNumber);
				param[2].setValue(ordercode);
				param[3].setValue(mainCustomerNumber);
				param[4].setValue("1");
				param[5].setValue(ipAddress + "/v1.0/paymentchannel/topup/yb/notify_call");
				param[param.length - 1].setValue(hmacSign());
				PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/withDrawApi.action");
				HttpClient client = new HttpClient();
				postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				try {
					postMethod.setRequestBody(param);
					int status2 = client.executeMethod(postMethod);
					LOG.info("==========结算status2==========" + status2);
					String backinfo = postMethod.getResponseBodyAsString();
					LOG.info("==========结算backinfo==========" + backinfo);
					if (status2 == HttpStatus.SC_OK) {
						JSONObject obj = JSONObject.fromObject(backinfo);
						if (obj.getString("code").equals("0000")) {
							/** 调用下单，需要得到用户的订单信息 */
							restTemplate = new RestTemplate();
							uri = util.getServiceUrl("transactionclear", "error url request!");
							url = uri.toString() + "/v1.0/transactionclear/payment/update";
							/** 根据的用户手机号码查询用户的基本信息 */
							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("status", "1");
							requestEntity.add("third_code", externalld);
							requestEntity.add("order_code", ordercode);
							LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + ordercode + ","
									+ externalld);
							result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
							/** 判断是否有外放的通道的处理， 如果有那么继续回调外放哦 */
							uri = util.getServiceUrl("transactionclear", "error url request!");
							url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";

							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("order_code", ordercode);
							result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================"
									+ result);
							jsonObject = JSONObject.fromObject(result);
							resultObj = jsonObject.getJSONObject("result");
							String outMerOrdercode = resultObj.getString("outMerOrdercode");
							String orderdesc = resultObj.getString("desc");
							String phone = resultObj.getString("phone");
							String tranamount = resultObj.getString("amount");
							String channelTag = resultObj.getString("channelTag");
							String notifyURL = resultObj.getString("outNotifyUrl");
							if (outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")) {
								uri = util.getServiceUrl("channel", "error url request!");
								url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
								requestEntity = new LinkedMultiValueMap<String, String>();
								requestEntity.add("merchant_no", phone);
								requestEntity.add("amount", tranamount);
								requestEntity.add("channel_tag", channelTag);
								requestEntity.add("order_desc", URLEncoder.encode(orderdesc, "UTF-8"));
								requestEntity.add("order_code", outMerOrdercode);
								requestEntity.add("sys_order", ordercode);
								requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
								result = restTemplate.postForObject(url, requestEntity, String.class);
							}
							pw.write("success");
						} else {
							status2 = client.executeMethod(postMethod);
							backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========结算backinfo==========" + backinfo);
							obj = JSONObject.fromObject(backinfo);
							if (obj.getString("code").equals("0000")) {
								/** 调用下单，需要得到用户的订单信息 */
								restTemplate = new RestTemplate();

								uri = util.getServiceUrl("transactionclear", "error url request!");
								url = uri.toString() + "/v1.0/transactionclear/payment/update";

								/** 根据的用户手机号码查询用户的基本信息 */
								requestEntity = new LinkedMultiValueMap<String, String>();
								requestEntity.add("status", "1");
								requestEntity.add("third_code", externalld);
								requestEntity.add("order_code", ordercode);
								LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + ordercode + ","
										+ externalld);
								result = restTemplate.postForObject(url, requestEntity, String.class);
								LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
								/** 判断是否有外放的通道的处理， 如果有那么继续回调外放哦 */
								uri = util.getServiceUrl("transactionclear", "error url request!");
								url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";

								requestEntity = new LinkedMultiValueMap<String, String>();
								requestEntity.add("order_code", ordercode);
								result = restTemplate.postForObject(url, requestEntity, String.class);
								LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================"
										+ result);
								jsonObject = JSONObject.fromObject(result);
								resultObj = jsonObject.getJSONObject("result");
								String outMerOrdercode = resultObj.getString("outMerOrdercode");
								String orderdesc = resultObj.getString("desc");
								String phone = resultObj.getString("phone");
								String tranamount = resultObj.getString("amount");
								String channelTag = resultObj.getString("channelTag");
								String notifyURL = resultObj.getString("outNotifyUrl");
								if (outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")) {
									uri = util.getServiceUrl("channel", "error url request!");
									url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
									requestEntity = new LinkedMultiValueMap<String, String>();
									requestEntity.add("merchant_no", phone);
									requestEntity.add("amount", tranamount);
									requestEntity.add("channel_tag", channelTag);
									requestEntity.add("order_desc", URLEncoder.encode(orderdesc, "UTF-8"));
									requestEntity.add("order_code", outMerOrdercode);
									requestEntity.add("sys_order", ordercode);
									requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
									result = restTemplate.postForObject(url, requestEntity, String.class);
								}
								pw.write("success");
							} else {
								pw.write("fail");
							}
						}
					} else if (status2 == HttpStatus.SC_MOVED_PERMANENTLY
							|| status2 == HttpStatus.SC_MOVED_TEMPORARILY) {
						// 从头中取出转向的地址
						Header locationHeader = postMethod.getResponseHeader("location");
						String location = null;
						if (locationHeader != null) {
							location = locationHeader.getValue();
							System.out.println("The page was redirected to:" + location);
						} else {
							System.err.println("Location field value is null.");
						}
					} else {
						System.out.println("fail======" + status2);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				} finally {
					// 释放连接
					postMethod.releaseConnection();
				}
			} else {
				pw.write("fail");
			}

		}
		pw.flush();
		pw.close();

	}

	// 审核接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yb/shenhe")
	public @ResponseBody Object shenhe(HttpServletRequest req, HttpServletResponse res,
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "mainCustomerNumber") String mainCustomerNumber) throws IOException {

		String stat = "SUCCESS";
		PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/auditMerchant.action");
		HttpClient client = new HttpClient();
		StringBuilder hmacStr = new StringBuilder();
		hmacStr.append(customerNumber == null ? "" : customerNumber)
				.append(mainCustomerNumber == null ? "" : mainCustomerNumber).append(stat == null ? "" : stat);
		String hmac = Digest.hmacSign(hmacStr.toString(), key);
		Part[] parts2 = new AuditMerchantPartsBuilders().setMainCustomerNumber(mainCustomerNumber)
				.setCustomerNumber(customerNumber).setStatus(stat).setHmac(hmac).generateParams();

		postMethod.setRequestEntity(new MultipartRequestEntity(parts2, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========backinfo2==========" + backinfo);
		}

		return null;
	}

	/**
	 * 签名
	 *
	 * @return
	 */
	private static String hmacSign() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");

		String hmac = Digest.hmacSign(hmacStr.toString(), key);

		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");

		return hmac;
	}

	private static String hmacSign1() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param1) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");

		String hmac = Digest.hmacSign(hmacStr.toString(), key);

		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");

		return hmac;
	}

	private static String hmacSign2() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param2) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");

		String hmac = Digest.hmacSign(hmacStr.toString(), key);

		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");

		return hmac;
	}

	private static String hmacSign3() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param3) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		System.out.println("===============");
		System.out.println("hmacStr.toString()=" + hmacStr.toString());
		System.out.println("===============");

		String hmac = Digest.hmacSign(hmacStr.toString(), key);

		System.out.println("===============");
		System.out.println("hmac=" + hmac);
		System.out.println("===============");

		return hmac;
	}

	// 根据订单号查询银联5进件的商户编号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/querymerchantby/ordercode")
	public @ResponseBody Object queryMerchant(HttpServletRequest request,
			@RequestParam(value = "order_code") String orderCode) {

		Map<String, String> map = new HashMap<String, String>();

		// LinkedMultiValueMap<String, String> linkedMultiValueMap = new
		// LinkedMultiValueMap<String, String>();

		// linkedMultiValueMap.add("orderCode", orderCode);

		// Map<String, Object> restTemplateDoPost =
		// util.restTemplateDoPost("transactionclear",
		// "/v1.0/transactionclear/payment/query/ordercode",
		// linkedMultiValueMap);

		// String phone = (String) restTemplateDoPost.get("phone");

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		String phone = null;
		try {
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			phone = resultObj.getString("phone");
		} catch (Exception e) {
			LOG.error("查询订单信息失败啦=======");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, "查询订单信息有误");

		}

		String customerNumber = null;
		try {
			RegisterAuth queryByMobile = registerAuthBusiness.queryByMobile(phone);

			customerNumber = queryByMobile.getCustomerNumber();
		} catch (Exception e) {
			LOG.error("查询商户编号出错啦======");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, "查询商户编号有误");
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, customerNumber);

		return map;
	}

	// 根据身份证号查询银联5进件的商户编号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/ybfive/querymerchantbyidcard")
	public @ResponseBody Object queryMerchantByIdCard(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard) {

		Map<String, String> map = new HashMap<String, String>();
		RegisterAuth queryByIdCard = registerAuthBusiness.getRegisterAuthByIdCard(idCard);
		if (queryByIdCard==null) {
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "银联5用户未进件");
		}else{
			
			String customerNumber = queryByIdCard.getCustomerNumber();
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "银联5用户已进件");
			map.put(CommonConstants.RESULT, customerNumber);
		}
		return map;
	}

	// 查询余额
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/customer/balancequery")
	public @ResponseBody Object transferToCustomer(HttpServletRequest request,
			@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "10015703385") String mainCustomerNumber,
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "balanceType", required = false, defaultValue = "3") String balanceType) {

		Map<String, String> map = new HashMap<String, String>();

		param1[0].setValue(mainCustomerNumber);
		param1[1].setValue(customerNumber);
		param1[2].setValue(balanceType);
		param1[param1.length - 1].setValue(hmacSign1());
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerBalanceQuery.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		postMethod.setRequestBody(param1);
		String code = null;
		String balance = null;
		int status2;
		try {
			status2 = client.executeMethod(postMethod);
			LOG.info("==========status2==========" + status2);
			String response = postMethod.getResponseBodyAsString();
			LOG.info("==========response==========" + response);

			JSONObject fromObject = JSONObject.fromObject(response);

			code = (String) fromObject.get("code");
			balance = (String) fromObject.get("balance");

		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("",e);
		}

		if ("0000".equals(code)) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, balance);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return map;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/query/ordercode")
	public @ResponseBody Object queryOrderCode(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode) {

		param2[0].setValue("10015703385");
		param2[1].setValue(orderCode);
		param2[2].setValue(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
		param2[3].setValue("1");
		param2[param2.length - 1].setValue(hmacSign2());
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/tradeReviceQuery.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		postMethod.setRequestBody(param2);
		String code = null;
		String balance = null;
		int status2;
		try {
			status2 = client.executeMethod(postMethod);
			LOG.info("==========status2==========" + status2);
			String response = postMethod.getResponseBodyAsString();
			LOG.info("==========response==========" + response);

			JSONObject fromObject = JSONObject.fromObject(response);

			code = (String) fromObject.get("code");
			balance = (String) fromObject.get("balance");

		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("",e);
		}

		return null;
	}

	// 代理商返佣转账接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/yb/transfertocustomer")
	public @ResponseBody Object transferToCustomer(HttpServletRequest request) {

		param3[0].setValue("10015703385");
		param3[1].setValue("10024295086");
		param3[2].setValue(UUID.randomUUID().toString().substring(0, 15));
		param3[3].setValue("2.00");
		param3[4].setValue("转账");
		param3[param3.length - 1].setValue(hmacSign3());
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/transferToCustomer.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		postMethod.setRequestBody(param3);
		String code = null;
		String balance = null;
		int status2;
		try {
			status2 = client.executeMethod(postMethod);
			LOG.info("==========status2==========" + status2);
			String response = postMethod.getResponseBodyAsString();
			LOG.info("==========response==========" + response);

			JSONObject fromObject = JSONObject.fromObject(response);

			code = (String) fromObject.get("code");

		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("",e);
		}

		return null;
	}

}

class CustomerInforUpdatePartsBuilder {

	private List<Part> parts = new ArrayList<Part>();

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public CustomerInforUpdatePartsBuilder setMainCustomerNumber(String mainCustomerNumber) {
		this.parts.add(
				new StringPart("mainCustomerNumber", mainCustomerNumber == null ? "" : mainCustomerNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setCustomerNumber(String customerNumber) {
		this.parts.add(new StringPart("customerNumber", customerNumber == null ? "" : customerNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setWhiteList(String whiteList) {
		this.parts.add(new StringPart("whiteList", whiteList == null ? "" : whiteList, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setFreezeDays(String freezeDays) {
		this.parts.add(new StringPart("freezeDays", freezeDays == null ? "" : freezeDays, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setHmac(String hmac) {
		this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setModifyType(String modifyType) {
		this.parts.add(new StringPart("modifyType", modifyType == null ? "" : modifyType, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setBankCardNumber(String bankCardNumber) {
		this.parts.add(new StringPart("bankCardNumber", bankCardNumber == null ? "" : bankCardNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setBankName(String bankName) {
		this.parts.add(new StringPart("bankName", bankName == null ? "" : bankName, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setRiskReserveDay(String riskReserveDay) {
		this.parts.add(new StringPart("riskReserveDay", riskReserveDay == null ? "" : riskReserveDay, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setManualSettle(String manualSettle) {
		this.parts.add(new StringPart("manualSettle", manualSettle == null ? "" : manualSettle, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setSplitter(String splitter) {
		this.parts.add(new StringPart("splitter", splitter == null ? "" : splitter, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setSplitterProfitFee(String splitterProfitFee) {
		this.parts
				.add(new StringPart("splitterProfitFee", splitterProfitFee == null ? "" : splitterProfitFee, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilder setBusiness(String business) {
		this.parts.add(new StringPart("business", business == null ? "" : business, "UTF-8"));
		return this;
	}
}
