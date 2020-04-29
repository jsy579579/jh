package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.ChannelDetail;
import com.jh.paymentchannel.pojo.TopupPayChannelRoute;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.WithDrawOrder;

import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class PayService extends BaseChannel {

	
	private static final Logger log = LoggerFactory.getLogger(PayService.class);
	
	
	@Autowired
	private TopupPayChannelBusiness  topupPayChannelBusiness;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Value("${paymentgateway.url}")
	private String paymentGateUrl;
	
	@Value("${paymentgateway.withdrawKey}")
	private String brandKey;
	@Autowired
	private PaymentChannelFactory factory;
	
	@Autowired
	private Util util;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/***请求url, 并返回跳装的url*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/request")
	public @ResponseBody Object payrequest(HttpServletRequest request, 
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "cardno") String cardno,
			@RequestParam(value = "username") String username,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "bankname") String bankname,
			@RequestParam(value = "phone") String phone,						
			@RequestParam(value = "pri_or_pub", required = false, defaultValue = "0") String priOrpub,			
			@RequestParam(value = "brandcode", required = false, defaultValue = "0") String brandcode,//品牌id
			//@RequestParam(value = "brandName") String brandName,//brandName参数，2019.4.18
			@RequestParam(value = "channel_type", required = false, defaultValue = "2") String channelType,	//0充值1代付
			@RequestParam(value = "channel_tag", required = false, defaultValue = "YILIAN") String channelTag//渠道标识
			){
		
		String notifyURL;
		String returnURL;
		Map map = new HashMap();
		
		if(Tools.checkAmount(amount) == false){
			
			map.put(CommonConstants.RESP_CODE,CommonConstants.ERROR_AMOUNT_ERROR);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}
		
		
		TopupPayChannelRoute payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandcode(brandcode, channelType, channelTag);
		log.info("TopupPayChannelRoute--RESULT================"+payChannelRoute);
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/channel/query";
		
		/**根据的渠道标识或去渠道的相关信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("channel_tag", channelTag);
		RestTemplate restTemplate=new RestTemplate();
		String resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/channel/query--RESULT================"+resultObjx);
		JSONObject jsonObject =  JSONObject.fromObject(resultObjx);
		JSONObject resultObj  =  jsonObject.getJSONObject("result");
		String channelid  = resultObj.getString("id");		
		ChannelDetail channelDetail = topupPayChannelBusiness
				.getChannelDetailByTag(payChannelRoute.getTargetChannelTag());
		notifyURL = channelDetail.getNotifyURL();
		returnURL = channelDetail.getReturnURL();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/brandrate/query";
		/**根据的渠道标识或去渠道的相关信息*/
		requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("brand_id", brandcode);
		requestEntity.add("channel_id", channelid);
		restTemplate=new RestTemplate();
		resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/brandrate/query--RESULT================"+resultObjx);
		jsonObject =  JSONObject.fromObject(resultObjx);
		resultObj  =  jsonObject.getJSONObject("result");
		String  withdrawFee  = resultObj.getString("withdrawFee");	
		String  extraFee  = resultObj.getString("extraFee");	
		
		/**
		 * 判断提现通道
		 * ***/
		String targetChannelTag=payChannelRoute.getTargetChannelTag();
		/*long accountLong=Long.parseLong(amount);
		//获取提现通道
		List<ChannelDetail> channelDetails = topupPayChannelBusiness.getChannelDetailByNO(channelType);
		String paymentGateIP=paymentGateUrl.substring(0, paymentGateUrl.indexOf("/v1.0"));;
		for(ChannelDetail ChannelDetail:channelDetails){
			if(ChannelDetail.getSubChannelTag()!=null){
				RestTemplate rt = new RestTemplate();
				MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
				String postForObject = rt.postForObject(paymentGateIP+ChannelDetail.getSubChannelTag(), multiValueMap, String.class);
				log.info("postForObject"+paymentGateIP+ChannelDetail.getSubChannelTag()+postForObject);
				JSONObject ForObject = JSONObject.fromObject(postForObject);
				long channelaccount = ForObject.getLong("result");
				if(channelaccount>accountLong){
					targetChannelTag=ChannelDetail.getChannelTag();
					break;
				}
			}
		}*/
						
		PayRequest  paymentRequest = factory.getPayChannelRequest(targetChannelTag);
		if(paymentRequest == null){
			Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
			
			Object object = queryOrdercode.get("result");
			JSONObject fromObject = JSONObject.fromObject(object);
			resultObj = fromObject.getJSONObject("result");

			String bankCard = resultObj.getString("bankcard");
			String realAmount = resultObj.getString("realAmount");
			String userId = resultObj.getString("userid");
			String rate = resultObj.getString("rate");
			extraFee = resultObj.getString("extraFee");
			String orderType = resultObj.getString("type");
			phone = resultObj.getString("phone");
			String authcode=resultObj.getString("desc");
			String userName = null;
			String idCard = null;
			String creditCardPhone = null;
			String creditCardBankName = null;
			String creditCardNature = null;
			String creditCardCardType = null;
			String expiredTime = null;
			String securityCode = null;
			
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
				userName = fromObject.getString("userName");
				idCard = fromObject.getString("idcard");
			}
			
			//到redis中获取贴牌对应的秘钥
			//String brandKey=String.valueOf(redisTemplate.opsForValue().get("FG:whitelist:"+brandcode));
			
			//传参秘钥   2019.4.20
			//String brandKey="0880070faeef068a486b3e052ff18666";
			
			//根据brandId到数据库中获取贴牌信息  2019.4.18
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandcode;
			restTemplate = new RestTemplate();
			
			String result = restTemplate.getForObject(url, String.class);
			log.info(" brand RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject resultObject;
			resultObject = jsonObject.getJSONObject("result");
			String brandName=resultObject.getString("name");
			
			
			jsonObject = new JSONObject();
			
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
			jsonObject.put("channelTag", targetChannelTag);
			jsonObject.put("orderType", orderType);
			jsonObject.put("extra", authcode);
			jsonObject.put("phone", phone);
			//2019.4.17 新增brandcode和brandKey参数
			jsonObject.put("brandId", brandcode);
			jsonObject.put("secretKey", brandKey);
			jsonObject.put("brandName", brandName);//新增brandName 2019.4.18
			jsonObject.put("ipAddress", ipAddress);
			log.info("data===="+jsonObject);
			RestTemplate rt = new RestTemplate();
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("data", jsonObject.toString());
			
			String postForObject = rt.postForObject(paymentGateUrl, multiValueMap, String.class);
			log.info("postForObject===="+postForObject);
			return postForObject;
			
		}
		
		WithDrawOrder withdrawOrder = paymentRequest.payRequest(ordercode, cardno, username, new BigDecimal(amount).subtract(new BigDecimal(withdrawFee)).subtract(new BigDecimal(extraFee)).toString(), bankname, phone, priOrpub, notifyURL,returnURL);
		/**
		 * 如果是E秒付
		 * **/
		if(withdrawOrder.getReqcode().length()==4){
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, withdrawOrder);
			map.put(CommonConstants.RESP_MESSAGE, "下单成功");
			return map;
		}
		/**
		 * 如果是联动
		 * **/
		if(withdrawOrder.getReqcode().length()==5){
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, withdrawOrder);
			map.put(CommonConstants.RESP_MESSAGE, "下单成功");
			return map;
		}
		
		if(withdrawOrder.getReqcode().equalsIgnoreCase(CommonConstants.SUCCESS)){
			map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, withdrawOrder);
			map.put(CommonConstants.RESP_MESSAGE, "下单成功");
		}else{
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_ORDER_FAIL);
			map.put(CommonConstants.RESULT, withdrawOrder);
			map.put(CommonConstants.RESP_MESSAGE, "下单失败");
			
		}
		
		return map;
		
	}
	
	
	
	
	
	
	/**代付结果查询*/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/pay/query")
	public @ResponseBody Object payquery(HttpServletRequest request, 
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "brandcode", required = false, defaultValue = "0") String brandcode,
			@RequestParam(value = "channel_type", required = false, defaultValue = "2") String channelType,	
			@RequestParam(value = "channel_tag", required = false, defaultValue = "YILIAN") String channelTag){
	
			Map map = new HashMap();
			TopupPayChannelRoute payChannelRoute = topupPayChannelBusiness.getTopupChannelByBrandcode(brandcode, channelType, channelTag);
			PayRequest  paymentRequest = factory.getPayChannelRequest(payChannelRoute.getTargetChannelTag());
			log.info("paymentRequest--RESULT================"+paymentRequest);
			WithDrawOrder   withdrawOrder = paymentRequest.queryPay(ordercode);
			log.info("withdrawOrder--RESULT================"+withdrawOrder);
			if(withdrawOrder.getReqcode().equalsIgnoreCase(CommonConstants.SUCCESS)){
				map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
				map.put(CommonConstants.RESULT, withdrawOrder);
				map.put(CommonConstants.RESP_MESSAGE, "交易成功");
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_WITHDRAW_ORDER_FAIL);
				map.put(CommonConstants.RESULT, withdrawOrder);
				map.put(CommonConstants.RESP_MESSAGE, "交易失败");
			}
			
			return map;
		
	}
	
	
	
	
	
}
