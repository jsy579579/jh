package com.jh.paymentchannel.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.RepaymentDetailBusiness;
import com.jh.paymentchannel.pojo.RepaymentDetail;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class RepaymentDetailService {
 
	
	private static final Logger LOG = LoggerFactory.getLogger(RepaymentDetailService.class);
	
	@Autowired
	private RepaymentDetailBusiness repaymentDetailBusiness;
	
	@Autowired
	private Util util;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/topup/repaymentdetail/getall")
	public Object getRepaymentDetailAll(HttpServletRequest request
			) {
		
		List<RepaymentDetail> repaymentDetailAll = repaymentDetailBusiness.getRepaymentDetailAll();
		
		if(repaymentDetailAll != null && repaymentDetailAll.size()>0) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", repaymentDetailAll);
		}else {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据!", repaymentDetailAll);
		}
		
	}
	
	
	
	@ResponseBody
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/topup/repaymentdetail/getbyuserId")
	public Object getRepaymentDetailByUserId(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "brandId") String brandId
			) {
		
		boolean hasKey = false;
		String key = "/v1.0/paymentchannel/topup/repaymentdetail/getbyuserId:userId=" + userId + ";brandId=" + brandId;
		ValueOperations<String, Object> operations;
		try {
			operations = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if (hasKey) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", operations.get(key));
			}
		} catch (Exception e1) {
			LOG.info("redis获取数据出错======", e1.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "当前查询人数过多,请稍后重试!");
		}
		
		List<RepaymentDetail> repaymentDetailAll = repaymentDetailBusiness.getRepaymentDetailAll();
		
		JSONObject jsonObject1 = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		if(repaymentDetailAll != null && repaymentDetailAll.size()>0) {
			
			for(RepaymentDetail rd : repaymentDetailAll) {
				
				String version = rd.getVersion();
				
				String url = "http://creditcardmanager/v1.0/creditcardmanager/repayment/getchannel/bybrandidandversion";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("brandId", brandId);
				requestEntity.add("version", version);
				JSONObject postForObject = restTemplate.postForObject(url, requestEntity, JSONObject.class);
				LOG.info("接口/v1.0/creditcardmanager/query/rate/by/userid====RESULT================" + postForObject);
				if(CommonConstants.SUCCESS.equals(postForObject.getString(CommonConstants.RESP_CODE))) {
					JSONObject jsonObject = postForObject.getJSONObject(CommonConstants.RESULT);
					String status = jsonObject.getString("status");
					if("1".equals(status)) {
						String respCode;
						try {
							url = "http://creditcardmanager/v1.0/creditcardmanager/query/rate/by/userid";
							requestEntity = new LinkedMultiValueMap<String, String>();
							requestEntity.add("userId", userId);
							requestEntity.add("brandId", brandId);
							requestEntity.add("version", version);
							String result = restTemplate.postForObject(url, requestEntity, String.class);
							LOG.info("接口/v1.0/creditcardmanager/query/rate/by/userid====RESULT================" + result);
							jsonObject = JSONObject.fromObject(result);
							respCode = jsonObject.getString("resp_code");
							if("000000".equals(respCode)) {
								String serviceCharge = jsonObject.getString("serviceCharge");
								serviceCharge = serviceCharge.substring(0, serviceCharge.indexOf("."));
								String rate = jsonObject.getString("rate");
								
								jsonObject1.put("version", rd.getVersion());
								jsonObject1.put("channelName", rd.getChannelName());
								jsonObject1.put("descriptionOne", rd.getDescriptionOne());
								jsonObject1.put("descriptionTwo", rd.getDescriptionTwo());
								jsonObject1.put("descriptionThree", rd.getDescriptionThree());
								jsonObject1.put("everydayLimit", rd.getEverydayLimit());
								jsonObject1.put("singleLimit", rd.getSingleLimit());
								jsonObject1.put("tradeTime", rd.getTradeTime());
								jsonObject1.put("classes", rd.getClasses());
								jsonObject1.put("onOff", rd.getOnOff());
								jsonObject1.put("status", rd.getStatus());
								jsonObject1.put("sort", rd.getSort());
								jsonObject1.put("recommend", rd.getRecommend());
								jsonObject1.put("rate", rate);
								jsonObject1.put("serviceCharge", serviceCharge);
								
								jsonArray.add(jsonObject1);
								
							}else {
								
								
							}
						} catch (Exception e) {
							LOG.error("查询会员费率出错======" + e.getMessage());
							
							continue;
						}
						
					}
					
				}
				
			}
			
			operations.set(key, jsonArray, 5, TimeUnit.MINUTES);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", jsonArray);
		}else {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据!", jsonArray);
		}
		
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/v1.0/paymentchannel/topup/repaymentdetail/get/by/versions")
	public @ResponseBody Object getRepaymentDetailByVersions(HttpServletRequest request,
			String[] versions
			) {
		List<RepaymentDetail> repaymentDetailAll = repaymentDetailBusiness.findByVersionIn(versions);
		if (repaymentDetailAll.size() < 1) {
			return ResultWrap.init(CommonConstants.FALIED, "暂无可用还款通道");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",repaymentDetailAll);
	}
}
