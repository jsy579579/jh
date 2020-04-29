
package com.cardmanager.pro.util;

import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Component
public class RestTemplateUtil {
	private static final Logger LOG = LoggerFactory.getLogger(RestTemplateUtil.class);

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private RepaymentBillBusiness repaymentBillBusiness;
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	/**
	 * TODO: Complement this with a simpler version without fallback-url!
	 *
	 * @param serviceId
	 * @param fallbackUri
	 * @return
	 */
	public URI getServiceUrl(String serviceId, String fallbackUri) {
		URI uri = null;
		try {
			ServiceInstance instance = loadBalancer.choose(serviceId);
			uri = instance.getUri();
			LOG.debug("Resolved serviceId '{}' to URL '{}'.", serviceId, uri);

		} catch (RuntimeException e) {
			e.printStackTrace();
			uri = URI.create(fallbackUri);
			LOG.warn("Failed to resolve serviceId '{}'. Fallback to URL '{}'.", serviceId, uri);
		}

		return uri;
	}

	public Date StrToDate(String str) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String DateToStr(Date date) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = format.format(date);
		return str;
	}

	public <T> ResponseEntity<T> createOkResponse(T body) {
		return createResponse(body, HttpStatus.OK);
	}

	public <T> ResponseEntity<T> createResponse(T body, HttpStatus httpStatus) {
		return new ResponseEntity<T>(body, httpStatus);

	}

	public String blogTrim(String blog) {

		String str1 = "";
		String str2 = "";
		String str3 = "";

		// 如果blog中不包含图片，则不用处理，直接返回
		if (!blog.contains("src=")) {
			System.out.println("不包含src的blog" + blog);
			return blog;
		}
		str1 = blog.substring(0, blog.indexOf("src="));
		str2 = blog.substring(blog.indexOf("src="), blog.indexOf(">")).replace(" ", "+");
		str3 = blog.substring(blog.indexOf(">"));

		blog = str1 + str2 + str3;

		return blog;
	}

	//获取返回码 封装到map
	public Map<String, Object> restTemplateDoPost(String serviceName, String apiUrl,
			LinkedMultiValueMap<String, String> requestEntity) {
		Map<String, Object> map = new HashMap<>();
		String url = "http://" + serviceName+apiUrl;
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "服务器繁忙,请稍后重试!");
			return map;
		}
		if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()? "请求失败,请重试!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
	
	@Async
	public void pushMessage(String userId,String message,String version,String creditCardNumber,String returnMessage,String orderCode){
		CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
		String channelName = creditCardManagerConfig.getChannelName();
		creditCardNumber = creditCardNumber.substring(creditCardNumber.length()-4, creditCardNumber.length());
		message = "卡尾号["+creditCardNumber+"]在("+channelName+")"+message;
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("alert", "智能还款");
		requestEntity.add("content", "亲爱的会员,"+message);
		requestEntity.add("btype", "balanceadd");
		requestEntity.add("btypeval", "");
		restTemplate.postForObject("http://user/v1.0/user/jpush/tset", requestEntity,String.class);
		
		if (!(CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version))) {
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", returnMessage + "");
			restTemplate.postForObject("http://transactionclear/v1.0/transactionclear/payment/update/remark",requestEntity, String.class);
		}
		
		{
			RepaymentTaskPOJO repaymentTaskPOJO2 = null;
			ConsumeTaskPOJO consumeTaskPOJO2 = null;
			requestEntity = new LinkedMultiValueMap<String,String>();
			requestEntity.add("order_code", orderCode);
			Map<String, Object> restTemplateDoPost = this.restTemplateDoPost("transactionclear", "/v1.0/transactionclear/payment/query/ordercode", requestEntity);
			JSONObject resultJSON = (JSONObject) restTemplateDoPost.get(CommonConstants.RESULT);
			String amountStr = resultJSON.getString("amount");
			String realAmountStr = resultJSON.getString("realAmount");
			String phone = resultJSON.getString("phone");
			String orderType = resultJSON.getString("type");
			String brandId = resultJSON.getString("brandid");
			String brandname = resultJSON.getString("brandname");
			String bankName = resultJSON.getString("bankName");
			String allbalance ="";
			String billNo ="";
			if(CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)){
				repaymentTaskPOJO2 = repaymentTaskPOJOBusiness.findByOrderCode(orderCode);
				if (repaymentTaskPOJO2 != null) { 
					version = repaymentTaskPOJO2.getVersion();
					billNo=repaymentTaskPOJO2.getCreateTime();
				}
			}
			if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
				consumeTaskPOJO2 = consumeTaskPOJOBusiness.findByConsumeTaskId(orderCode);
				if (consumeTaskPOJO2 != null) {
					version	 = consumeTaskPOJO2.getVersion();
				}
				billNo=consumeTaskPOJO2.getCreateTime(); 

			}
			RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, billNo);
			if(repaymentBill!=null) {
				allbalance=repaymentBill.getTaskAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString();
			}
			
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("user_id", userId);
			requestEntity.add("phone", phone);
			requestEntity.add("brand_id", brandId);
			if(CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)){
				requestEntity.add("tpl_id", "repayment");
				requestEntity.add("time", "23:00");
			}else {
				requestEntity.add("tpl_id", "consume");
			}
			requestEntity.add("bankNum", creditCardNumber);
			requestEntity.add("bankName", bankName);
			requestEntity.add("platform", brandname);
			requestEntity.add("allbalance", allbalance);
			requestEntity.add("balance", amountStr);
			
			restTemplate.postForObject("http://notice/v1.0/notice/sms/inform/send",requestEntity, String.class);

		}
	}
}
