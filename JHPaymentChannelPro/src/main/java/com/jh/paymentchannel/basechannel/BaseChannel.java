package com.jh.paymentchannel.basechannel;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

public class BaseChannel {

	@Autowired
	private Util util;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private BranchbankBussiness branchbankBussiness;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	// 查询订单号
	public Map<String, Object> queryOrdercode(String orderCode) {

		Map<String, Object> map = new HashMap<>();

		String url = "http://transactionclear/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/transactionclear/payment/query/ordercode====RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询订单信息出错======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, jsonObject);

		return map;
	}

	// 查询用户的默认结算卡
	public Map<String, Object> queryBankCardByUserId(String userId) {

		Map<String, Object> map = new HashMap<String, Object>();

		String url = "http://user/v1.0/user/bank/default/userid";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("user_id", userId);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);

		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			String respCode = jsonObject.getString("resp_code");
			if("000000".equals(respCode)) {
				resultObj = jsonObject.getJSONObject("result");
			}else {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", "该通道需绑定默认提现借记卡!");
				return map;
			}
			
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resultObj);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");

		return map;
	}

	// 查询用户的充值卡信息
	public Map<String, Object> queryBankCardByCardNo(String bankCard, String type) {

		Map<String, Object> map = new HashMap<String, Object>();

		String url = "http://user/v1.0/user/bank/default/cardno";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("cardno", bankCard);
		multiValueMap.add("type", type);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno====RESULT=========" + result);

		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resultObj);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");

		return map;
	}

	
	//通过userId和卡号查询充值卡信息
	public Map<String, Object> queryBankCardByCardNoAndUserId(String bankCard, String type, String userId) {
		
		
		Map<String, Object> map = new HashMap<String, Object>();

		String url = "http://user/v1.0/user/bank/default/cardnoand/type";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("cardno", bankCard);
		multiValueMap.add("type", type);
		multiValueMap.add("userId", userId);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardnoand/type====RESULT=========" + result);

		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resultObj);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");

		return map;
	}
	
	
	// 根据银行名称获取银行编码
	public Map<String, Object> getBankCodeByBankName(String bankName) {

		Map<String, Object> map = new HashMap<String, Object>();

		String url = "http://user/v1.0/user/bankcode/getcodebyname";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("name", bankName);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bankcode/getcodebyname====RESULT=========" + result);

		JSONObject fromObject;
		String code;
		try {
			fromObject = JSONObject.fromObject(result);
			code = fromObject.getString("result");
		} catch (Exception e) {
			LOG.error("查询银行编号出错======");
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, code);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");

		return map;
	}

	
	//获取银行总行联行号
	public Map<String, Object> getBankUnitNo(String bankName){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo = null;
		try {
			findByBankName = branchbankBussiness.findByBankName(bankName);
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, inBankUnitNo);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	
	//随机获取shops表中的userid
	public Map<String, Object> getRandomUserId(){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		String url = "http://user/v1.0/user/query/randomuserid";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/query/randomuserid====RESULT=========" + result);
		
		JSONObject fromObject;
		String shopUserId;
		try {
			fromObject = JSONObject.fromObject(result);
			shopUserId = fromObject.getString("result");
		} catch (Exception e) {
			LOG.error("查询用户ID出错！！！！");
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "没有查询到用户ID,请稍后重试!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, shopUserId);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	
	//查询商铺信息
	public Map<String, Object> getShopsByUserId(String userId){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		String url = "http://user/v1.0/user/shops/query/uid";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("userid", userId);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		
		JSONObject fromObject;
		JSONObject resultObj = null;
		try {
			fromObject = JSONObject.fromObject(result);
			resultObj = fromObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询商铺信息出错");
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询商铺信息有误,请稍后重试!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resultObj);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		
		return map;
	}
	
	
	public void addOrderCauseOfFailure(String orderCode, Object remark){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		String url = "http://transactionclear/v1.0/transactionclear/payment/update/remark";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("ordercode", orderCode);
		multiValueMap.add("remark", remark.toString());
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		
	}
	
	
	// 将信用卡有效期格式转换为MMYY
	public String expiredTimeToMMYY(String expiredTime) {

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + before;
			} else {
				expiredTime = before + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			e.printStackTrace();
			return expiredTime;
		}

		return expiredTime;
	}

	// 将信用卡有效期格式转换为YYMM
	public String expiredTimeToYYMM(String expiredTime) {

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = before + after;
			} else {
				expiredTime = after + before;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			e.printStackTrace();
			return expiredTime;
		}

		return expiredTime;
	}

	
	//修改订单状态
	public Map<String, Object> updateOrderCode(String orderCode, String orderStatus, String thirdOrderCode){
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		String url = "http://transactionclear/v1.0/transactionclear/payment/update";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("order_code", orderCode);
		multiValueMap.add("status", orderStatus);
		multiValueMap.add("third_code", thirdOrderCode);
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		
		JSONObject fromObject;
		JSONObject resultObj = null;
		try {
			fromObject = JSONObject.fromObject(result);
			resultObj = fromObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("修改订单信息出错======");
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "修改订单信息出错!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, resultObj);
		map.put(CommonConstants.RESP_MESSAGE, "修改成功");
		
		return map;
		
	}
	
	
	// 修改订单状态的方法
	public void updateOrderCodeStatus(String orderCode) {

		RestTemplate restTemplate = new RestTemplate();

		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/updateordercode";

		// **根据的用户手机号码查询用户的基本信息*//*
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);

	}
	
}
