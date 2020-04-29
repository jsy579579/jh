package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
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

import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YBQuickRegister;
import com.jh.paymentgateway.util.yb.Conts;
import com.jh.paymentgateway.util.yb.Digest;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YBnewCallBackService {

	private static final Logger LOG = LoggerFactory.getLogger(YBnewCallBackService.class);
	private static String key = Conts.hmacKey;;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private RedisUtil redisUtil;

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
			new NameValuePair("customerNumber", ""),
			// 小商户编号
			new NameValuePair("mainCustomerNumber", ""),
			// 出款订单号
			new NameValuePair("productType", ""),

			// 签名串
			new NameValuePair("hmac", ""),

	};

	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/ybn/notify_call")
	public void notifycall(HttpServletRequest req, HttpServletResponse res) throws Exception {
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

				PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);

				/** 调用下单，需要得到用户的订单信息 */
				RestTemplate restTemplate = new RestTemplate();
				
				String url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
				/** 根据的用户手机号码查询用户的基本信息 */
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("third_code", externalld);
				requestEntity.add("order_code", ordercode);
				LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + ordercode + "," + externalld);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
				/** 判断是否有外放的通道的处理， 如果有那么继续回调外放哦 */
				url = prp.getIpAddress() + "/v1.0/transactionclear/payment/query/ordercode";

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
					url = prp.getIpAddress() + "/v1.0/channel/callback/yilian/notify_call";
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

				PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);

				RestTemplate restTemplate = new RestTemplate();
				String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/query/ordercode";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code", ordercode);
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
				JSONObject jsonObject = JSONObject.fromObject(result);
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String realAmt = resultObj.getString("realAmount");
				String extraFee = resultObj.getString("extraFee");
				/*
				 * BigDecimal realAmount = new BigDecimal(realAmt) .add(new
				 * BigDecimal(extraFee).setScale(2, BigDecimal.ROUND_HALF_UP))
				 * .setScale(2, BigDecimal.ROUND_HALF_DOWN);
				 */
				BigDecimal realAmount = new BigDecimal(realAmt).setScale(2, BigDecimal.ROUND_HALF_UP).setScale(2,
						BigDecimal.ROUND_HALF_DOWN);
				String Amount = String.valueOf(Double.valueOf(extraFee)+Double.valueOf(realAmt));
				LOG.info("提现金额+含手续费：" + Amount + "--------------------------");
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
				LOG.info("代付金额=======" + Amount.toString());
				String externalld = map.get("externalld");
				String amount = map.get("amount");
				String customerNumber = map.get("customerNumber");
				String mainCustomerNumber = "10025093920"; // 代理商编码
				LOG.info("===============" + ordercode + "," + externalld + "," + amount + "," + customerNumber + ","
						+ mainCustomerNumber);
				param[0].setValue(Amount.toString());
				param[1].setValue(customerNumber);
				param[2].setValue(ordercode);
				param[3].setValue(mainCustomerNumber);
				param[4].setValue("1");
				param[5].setValue(prp.getIpAddress() + "/v1.0/paymentgateway/topup/ybn/notify_call");
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
							
							url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
							//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
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
							url = prp.getIpAddress() + "/v1.0/transactionclear/payment/query/ordercode";

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
								url = prp.getIpAddress() + "/v1.0/channel/callback/yilian/notify_call";
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

								url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
								//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

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
								url = prp.getIpAddress() + "/v1.0/transactionclear/payment/query/ordercode";

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
									url = prp.getIpAddress() + "/v1.0/channel/callback/yilian/notify_call";
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

	// 根据订单号查询银联11进件的商户编号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/ybeleven/querymerchantbyidCard")
	public @ResponseBody Object queryMerchant(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard) {
		
		Map<String, String> map = new HashMap<String, String>();

		YBQuickRegister ybQuickRegister = topupPayChannelBusiness.getYBQuickRegisterByIdCard(idCard);

		if (ybQuickRegister==null) {
				
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "银联11用户未进件");
		}else{
				
			String customerNumber = ybQuickRegister.getCustomerNum();
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "银联11用户已进件");
			map.put(CommonConstants.RESULT, customerNumber);
		}
		return map;
	}

	// 查询子商户余额
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/ybn/customer/balancequery")
	public @ResponseBody Object transferToYbNewCustomer(HttpServletRequest request,
			@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "10025093920") String mainCustomerNumber,
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

	// 代付
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/ybn/customer/withDrawApi")
	public @ResponseBody Object withDrawApi(HttpServletRequest request,
			@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "10025093920") String mainCustomerNumber,
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "balanceType", required = false, defaultValue = "3") String balanceType,
			@RequestParam(value = "orderCode") String orderCode) {

		Map<String, String> map = new HashMap<String, String>();

		LOG.info("进入代付接口=======");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String realAmount = prp.getRealAmount();
		String extraFee = prp.getExtraFee();
		
		String Amount = String.valueOf(Double.valueOf(extraFee)+Double.valueOf(realAmount));
		LOG.info("提现金额+含手续费：" + Amount + "--------------------------");
		
		param[0].setValue(Amount.toString());
		param[1].setValue(customerNumber);
		param[2].setValue(orderCode);
		param[3].setValue(mainCustomerNumber);
		param[4].setValue("1");
		param[5].setValue(prp.getIpAddress() + "/v1.0/paymentgateway/topup/ybn/notify_call");
		param[param.length - 1].setValue(hmacSign());
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/withDrawApi.action");
		HttpClient client = new HttpClient();
		postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		String url = null;
		String message = null;
		try {
			postMethod.setRequestBody(param);
			int status2 = client.executeMethod(postMethod);
			LOG.info("==========结算status2==========" + status2);
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========结算backinfo==========" + backinfo);
			if (status2 == HttpStatus.SC_OK) {
				JSONObject obj = JSONObject.fromObject(backinfo);
				message = obj.getString("message");
				if (obj.getString("code").equals("0000")) {
					/** 调用下单，需要得到用户的订单信息 */
					restTemplate = new RestTemplate();
					
					url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
					//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
					/** 根据的用户手机号码查询用户的基本信息 */
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("third_code", "");
					requestEntity.add("order_code", orderCode);
					LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + orderCode + ","
							+ "");
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, message);
					map.put(CommonConstants.RESULT, customerNumber);
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, message);
					map.put(CommonConstants.RESULT, customerNumber);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("",e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, message);
			map.put(CommonConstants.RESULT, customerNumber);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/customer/ybn/rquery")
	public @ResponseBody Object transferToCustomer(HttpServletRequest request) {

		param2[0].setValue("10021462986");
		param2[1].setValue("10025093920");
		param2[2].setValue("2");
		param2[param2.length - 1].setValue(hmacSign2());
		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/queryFeeSetApi.action");
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

}

class CustomerInforUpdatePartsBuilders {

	private List<Part> parts = new ArrayList<Part>();

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public CustomerInforUpdatePartsBuilders setMainCustomerNumber(String mainCustomerNumber) {
		this.parts.add(
				new StringPart("mainCustomerNumber", mainCustomerNumber == null ? "" : mainCustomerNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setCustomerNumber(String customerNumber) {
		this.parts.add(new StringPart("customerNumber", customerNumber == null ? "" : customerNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setWhiteList(String whiteList) {
		this.parts.add(new StringPart("whiteList", whiteList == null ? "" : whiteList, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setFreezeDays(String freezeDays) {
		this.parts.add(new StringPart("freezeDays", freezeDays == null ? "" : freezeDays, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setHmac(String hmac) {
		this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setModifyType(String modifyType) {
		this.parts.add(new StringPart("modifyType", modifyType == null ? "" : modifyType, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setBankCardNumber(String bankCardNumber) {
		this.parts.add(new StringPart("bankCardNumber", bankCardNumber == null ? "" : bankCardNumber, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setBankName(String bankName) {
		this.parts.add(new StringPart("bankName", bankName == null ? "" : bankName, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setRiskReserveDay(String riskReserveDay) {
		this.parts.add(new StringPart("riskReserveDay", riskReserveDay == null ? "" : riskReserveDay, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setManualSettle(String manualSettle) {
		this.parts.add(new StringPart("manualSettle", manualSettle == null ? "" : manualSettle, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setSplitter(String splitter) {
		this.parts.add(new StringPart("splitter", splitter == null ? "" : splitter, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setSplitterProfitFee(String splitterProfitFee) {
		this.parts
				.add(new StringPart("splitterProfitFee", splitterProfitFee == null ? "" : splitterProfitFee, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setBusiness(String business) {
		this.parts.add(new StringPart("business", business == null ? "" : business, "UTF-8"));
		return this;
	}

	public CustomerInforUpdatePartsBuilders setMobilePhone(String mobilePhone) {
		this.parts.add(new StringPart("mobilePhone", mobilePhone == null ? "" : mobilePhone, "UTF-8"));
		return this;
	}
}
