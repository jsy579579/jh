package com.jh.paymentgateway.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.jh.paymentgateway.business.ChannelDetailBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.business.WithdrawRequestBusiness;
import com.jh.paymentgateway.business.impl.PaymentFactory;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.ChannelDetail;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
@RequestMapping(value="/v1.0/paymentgateway")
public class TopupRequestController{

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Value("${withdraw.key}")
	private String SCRECTKEY;

	@Value("${brandid.blacklist}")
	private String brandIdBlacklist;

	@Value("${brandid.inblacklisterrortip}")
	private String requestError;

	@Autowired
	private PaymentFactory paymentFactory;

	@Autowired
	private ChannelDetailBusiness channelDetailBusiness;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private Util util;
	
	@Autowired
	RedisTemplate redisTemplate;

	@RequestMapping(method=RequestMethod.POST,value="/pay/gateway")
	public @ResponseBody Object payGateway(HttpServletRequest request,HttpServletResponse response
			){
		String parameterData = request.getParameter("data");
		LOG.info("=====data=====:" + parameterData);

		JSONObject parameterJSON = null;
		try {
			parameterJSON = JSONObject.fromObject(parameterData);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultWrap.err(LOG, CommonConstants.FALIED, "JSON校验失败!");
		}
		String orderCode = parameterJSON.getString("orderCode");
		String channelTag = parameterJSON.getString("channelTag");
		String channelType = parameterJSON.getString("orderType");
		String realAmount = parameterJSON.getString("realAmount");
		LOG.info("订单类型==========="+channelType);
		//特殊贴牌不允许提现操作
		if(channelType.equals("2")) {//orderType=2 为提现操作
			//获取贴牌信息及秘钥 2019.4.17
			//String brandId=parameterJSON.getString("brandId");
			Object has_brandName = parameterJSON.get("brandName");
			Object has_secretKey = parameterJSON.get("secretKey");
			String brandName=has_brandName!=null?parameterJSON.getString("brandName"):"";
			String secretKey=has_secretKey!=null?parameterJSON.getString("secretKey"):"";
			LOG.info("withdraw:request,brandName---------"+brandName);
			LOG.info("withdraw:request,brandIdBlacklist---------"+brandIdBlacklist);

			//redis缓存中获取秘钥,FG:{brandId}
			//String key = "FG:balcklist:"+brandId;
			//String seed_secretKey = String.valueOf(redisTemplate.opsForValue().get(key));
			/*if(brandId==null||secretKey==null||!SCRECTKEY.equals(secretKey)||brandIdBlacklist.contains("-"+brandId+"-")) {
				LOG.info("withdraw:request is forbidden,brandid in blacklist,brandId="+brandId);
				return ResultWrap.err(LOG, CommonConstants.FALIED, requestError);
			}*/
			//根据贴牌名和秘钥判定
			if(brandName==null||brandName==""||secretKey==null||secretKey==""||!SCRECTKEY.equals(secretKey)||brandIdBlacklist.contains("-"+brandName+"-")) {
				LOG.info("withdraw:request is forbidden,brandName in blacklist,brandName="+brandName);
				return ResultWrap.err(LOG, CommonConstants.FALIED, requestError);
			}
		}
		
		String extra = "";
		try {
			extra = parameterJSON.getString("extra");
			parameterJSON.discard("extra");
		} catch (Exception e1) {

		}
		PaymentRequestParameter bean = (PaymentRequestParameter) JSONObject.toBean(parameterJSON, PaymentRequestParameter.class);
		LOG.info("value=========="+bean);
		bean.setExtra(extra);
		redisUtil.savePaymentRequestParameter(orderCode, bean);
		//提现订单处理
//		if(channelType.equals("2")){
//			BigDecimal accountLong=new BigDecimal(realAmount);
//			List<ChannelDetail> ChannelDetails= channelDetailBusiness.findByChannelType(channelType);
//			for(ChannelDetail channelDetail:ChannelDetails){
//				if(channelDetail.getChannelSelurl()!=null&&channelDetail.getChannelSelurl().length()>0){
//					/*URI uri = util.getServiceUrl("paymentgateway", "error url request!");
//					String url = uri.toString() + channelDetail.getChannelSelurl();
//					 *//**根据的渠道标识或去渠道的相关信息*//*
//					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
//					requestEntity.add("channel_tag", channelTag);
//					RestTemplate restTemplate=new RestTemplate();
//					String resultObjx = restTemplate.postForObject(url, requestEntity, String.class);
//					LOG.info("resultObjx"+uri+resultObjx);
//					JSONObject ForObject = JSONObject.fromObject(resultObjx);
//					BigDecimal channelaccount = new BigDecimal(ForObject.getLong("result"));*/
//					WithdrawRequestBusiness WithdrawRequest=paymentFactory.getWithdrawRequest(channelDetail.getChannelTag());
//					//BigDecimal channelaccount=WithdrawRequest.CheckBalanceRequest();
//					/*if(channelaccount.compareTo(accountLong)==1){
//						channelTag=channelDetail.getChannelTag();
//						break;
//					}*/
//					channelTag=channelDetail.getChannelTag();
//				}
//			}
//		}

		LOG.info("channelTag==================================" + channelTag);
		TopupRequestBusiness topupRequest = paymentFactory.getTopupRequest(channelTag);
		LOG.info("topupRequest==================================" + channelTag);
		Map<String, Object> params = new HashMap<>();
		params.put("request", request);
		params.put("orderCode", orderCode);
		params.put("paymentRequestParameter", bean);
		Map<String, Object> topupRequestMap = null;
		try {
			topupRequestMap = topupRequest.topupRequest(params);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(parameterJSON+"",e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求支付失败");
		}
		return topupRequestMap;
	}

	@RequestMapping(value="/pay/test")
	public @ResponseBody Object payGatewayTest(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value="redisKey")String redisKey
			){
		PaymentRequestParameter paymentRequestParameter = redisUtil.getPaymentRequestParameter(redisKey);
		String extra = paymentRequestParameter.getExtra();
		JSONObject fromObject = JSONObject.fromObject(extra);
		JSONObject xfTask1 = fromObject.getJSONObject("XFTask1");
		JSONObject XFTask2 = fromObject.getJSONObject("XFTask2");
		JSONObject HKTask = fromObject.getJSONObject("HKTask");
		String string = xfTask1.getString("consumeTaskId");
		System.out.println(string);
		return ResultWrap.init(CommonConstants.SUCCESS, "获取成功!",redisUtil.getPaymentRequestParameter(redisKey));
	}

}
