package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.util.MD5;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.abroad.PaymentUtil;
import com.jh.paymentchannel.util.jp.HttpUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class AbroadConsumptionPageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(AbroadConsumptionPageRequest.class);

	@Autowired
	Util util;

	@Autowired
	private TopupService topupService;

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	private String merNo = "3929";
	
	private String Key = "9f0e45b38dde49b1b20de1ba9647f300";
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/abroadconsumption/pay")
	public @ResponseBody Object abroadConsumptionPay(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "channe_tag", required = false, defaultValue = "ABROAD_CONSUME") String channeltag,
			@RequestParam(value = "order_desc", required = false, defaultValue = "境外消费") String orderdesc,
			@RequestParam(value = "brand_id", required = false,defaultValue="-1") String brand_id)
			throws Exception {

		RestTemplate restTemplate = new RestTemplate();
		
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/query/phone";
		/** 根据的用户手机号码查询用户的基本信息 */
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brand_id);
		restTemplate = new RestTemplate();
		JSONObject resultObju = null;
		JSONObject jsonObject;
		String result;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/user/query/phone--RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObju = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/query/phone根据手机号和brandId查询用户信息异常===========" + e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询您的信息有误,请稍后重试!");
		}
		
		String realnameStatus = resultObju.getString("realnameStatus");
		
		if(!"1".equals(realnameStatus)) {
			
			return ResultWrap.init(CommonConstants.FALIED, "很抱歉,您暂未实名,无法进行交易!");
		}
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		map = (Map<String, Object>) abroadConsumptionRateQuery(request, "NZD", "alipay");
		jsonObject = JSONObject.fromObject(map.get("result"));
		String rate = jsonObject.getString("rate");
		BigDecimal setScale = new BigDecimal(amount).multiply(new BigDecimal(rate)).setScale(2, BigDecimal.ROUND_HALF_UP);	
		
		/** 调用下单，需要得到用户的订单信息 */
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/addabroadconsumption/ordercode";

		/** 根据的用户手机号码查询用户的基本信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", setScale + "");
		requestEntity.add("channelTag", channeltag);
		requestEntity.add("desc", orderdesc);
		requestEntity.add("brandId", brand_id);
		JSONObject resultObj;
		String orderCode;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/transactionclear/payment/add--RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			orderCode = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
			
			return ResultWrap.init(CommonConstants.FALIED, "下单失败,请稍后重试!");
		}
		
		//将金额转换为以分为单位:
		String BigAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String format = sdf.format(new Date());
		Map<String, String> requestMap = new TreeMap<String, String>();
		requestMap.put("merNo", merNo);
		requestMap.put("businessOrderNo", orderCode);
		requestMap.put("amount", BigAmount);
		requestMap.put("subject", "qwe");
		requestMap.put("description", "qwe");
		requestMap.put("paytype", "alipay_wap");
		requestMap.put("notifyurl", ipAddress + "/v1.0/paymentchannel/topup/abroadconsumption/notifyurl");
		requestMap.put("referUrl", ipAddress + "/v1.0/paymentchannel/topup/yldzpaying");
		requestMap.put("attach", orderCode);
		requestMap.put("applydate", format);
		
		Set<String> keySet = requestMap.keySet();
		Iterator<String> it = keySet.iterator();

		StringBuffer sb1 = new StringBuffer();
		while (it.hasNext()) {
			String next = it.next();
			sb1.append(next + "=" + requestMap.get(next) + "&");
		}
		
		String substring = sb1.toString().substring(0, sb1.length()-1);
		
		String string = substring + Key;
		
		LOG.info("加签参数string======" + string);
		
		String sign = MD5.md5(string);
		
		requestMap.put("version", "V1.0");
		requestMap.put("bankcode", "1020000");
		requestMap.put("ip", request.getLocalAddr());
		requestMap.put("sign", sign.toLowerCase());
		
		JSONObject fromObject = JSONObject.fromObject(requestMap);
		
		LOG.info("请求参数fromObject======" + fromObject);
		
		String postForObject = HttpUtils.sendPost("http://103.72.167.165/api/payment/create_transaction", fromObject.toString());
		
		LOG.info("请求申请绑卡返回的postForObject======" + postForObject);
		
		JSONObject fromObject2 = JSONObject.fromObject(postForObject);
		
		String result1 = fromObject2.getString("result");
		String errMsg = fromObject2.getString("errMsg");
		
		if("success".equals(result1)) {
			
			String payUrl = fromObject2.getString("payUrl");
			String orderNo = fromObject2.getString("orderNo");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "请求成功!", payUrl);
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errMsg);
		}
		
	}

	
	//订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/abroadconsumption/queryordercode")
	public @ResponseBody Object abroadConsumptionQueryOrderCode(HttpServletRequest request, 
			@RequestParam(value = "orderCode") String orderCode )
			throws Exception {
		
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put("merchant_order_sn", orderCode);
		Map<String, Object> responseMap = PaymentUtil.sendDirectQueryPost(requestMap);
		
		LOG.info("responseMap======" + responseMap);
		
		JSONObject fromObject = JSONObject.fromObject(responseMap);
		
		String errcode = fromObject.getString("errcode");
		String msg = fromObject.getString("msg");
		
		if("0".equals(errcode)) {
			JSONObject data = fromObject.getJSONObject("data");
			
			String pay_status = data.getString("pay_status");

			if("1".equals(pay_status)) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "订单支付成功!");
			}else {
			
				return ResultWrap.init(CommonConstants.FALIED, "支付失败!");
			}
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, msg);
		}
		
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/abroadconsumption/notifyurl")
	public void notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");
	
		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream byteArray = null;
		byteArray = new ByteArrayOutputStream();
		byte[] dat = new byte[2048];
		int l = 0;
		while ((l = inputStream.read(dat, 0, 2048)) != -1) {
			byteArray.write(dat, 0, l);
		}
		byteArray.flush();
		LOG.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
		String info = new String(byteArray.toByteArray(), "UTF-8");
		JSONObject jsonInfo = null;
		try {
			jsonInfo = JSONObject.fromObject(info);
		} catch (Exception e1) {
			//return null;
		}
		LOG.info("jsonInfo=============" + jsonInfo.toString());
		inputStream.close();
		byteArray.close();
		
		String orderCode = jsonInfo.getString("businessOrderNo");
		String orderNo = jsonInfo.getString("orderNo");
		String pay_status = jsonInfo.getString("status");
		String errorMessage = jsonInfo.getString("errorMessage");
		
		LOG.info("pay_status======" + pay_status);
		LOG.info("orderCode======" + orderCode);
		
		if("1".equals(pay_status)) {
			
			LOG.info("111111======");
			
			/*RestTemplate restTemplate = new RestTemplate();

			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/updateordercode";

			// **根据的用户手机号码查询用户的基本信息*//*
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			String result = restTemplate.postForObject(url, requestEntity, String.class);*/
			
			this.updateOrderCode(orderCode, "1", "");
			
			response.getWriter().write("SUCCESS");
			
		}else {
			
			response.getWriter().write("SUCCESS");
		}
		
	}
	
	
	//汇率查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/abroadconsumption/ratequery")
	public @ResponseBody Object abroadConsumptionRateQuery(HttpServletRequest request, 
			@RequestParam(value = "ordCurrency") String ordCurrency,
			@RequestParam(value = "channel") String channel
			) throws Exception {
		
		/*Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put("ord_currency", ordCurrency);
		requestMap.put("payment_channels", channel);
		Map<String, Object> responseMap = PaymentUtil.sendRateQueryPost(requestMap);
		
		LOG.info("responseMap======" + responseMap);
		
		JSONObject fromObject = JSONObject.fromObject(responseMap);
		
		String errcode = fromObject.getString("errcode");
		String msg = fromObject.getString("msg");*/
		
		//if("0".equals(errcode)) {
			//JSONObject data = fromObject.getJSONObject("data");
			//String ord_currency = data.getString("ord_currency");
			//String rate = data.getString("rate");
			
			Map<String,String> map = new HashMap<String, String>();
			map.put("ordCurrency", "NZD");
			//map.put("rate", "4.766359");
			map.put("rate", "4.686057");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "汇率查询成功!", map);
		//}else {
			
			//return ResultWrap.init(CommonConstants.FALIED, msg);
		//}
		
	}
	
	
	private static String doPost(String url, Map<String, String> treeMap) throws IOException {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000) // 服务器返回数据(response)的时间，超过该时间抛出read
																						// timeout
				.setConnectTimeout(5000)// 连接上服务器(握手成功)的时间，超出该时间抛出connect
										// timeout
				.setConnectionRequestTimeout(1000)// 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
													// Timeout waiting for
													// connection from pool
				.build();

		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		try {
			if (treeMap != null) {
				// 设置2个post参数
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				for (String key : treeMap.keySet()) {
					parameters.add(new BasicNameValuePair(key, (String) treeMap.get(key)));
				}
				// 构造一个form表单式的实体
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				// 将请求实体设置到httpPost对象中
				httpPost.setEntity(formEntity);
			}

			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			LOG.info("请求响应码statusCode======" + statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		} finally {
			response.close();
		}
		return result;
	}
	
}
