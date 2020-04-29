package com.jh.paymentgateway.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.jh.paymentgateway.business.OrderParameterBusiness;
import com.jh.paymentgateway.pojo.KQRegister;
import com.jh.paymentgateway.pojo.OrderParameter;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import net.sf.json.JSONObject;

@Component
public class RedisUtil {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private OrderParameterBusiness orderParameterBusiness;
	
	public void savePaymentRequestParameter(String key,PaymentRequestParameter value){
		redisTemplate.opsForValue().set(key, value,60*60, TimeUnit.SECONDS);
		
		JSONObject orderJson = JSONObject.fromObject(value);
		OrderParameter orderParameter = new OrderParameter();
		orderParameter.setOrderCode(key);
		orderParameter.setOrderJson(orderJson.toString());
		orderParameterBusiness.save(orderParameter);
	}
	
	public PaymentRequestParameter getPaymentRequestParameter(String orderCode){
		PaymentRequestParameter result = (PaymentRequestParameter) redisTemplate.opsForValue().get(orderCode);
		if (result == null) {
			OrderParameter orderParameter = orderParameterBusiness.findByOrderCode(orderCode);
			if (orderParameter == null) {
				return result;
			}
			String orderJson = orderParameter.getOrderJson();
			JSONObject jsonObject = JSONObject.fromObject(orderJson);
			return (PaymentRequestParameter) JSONObject.toBean(jsonObject, PaymentRequestParameter.class);
		}else {
			return result;
		}
	}

	public Object get(String key) {
		Object result = false;
		try {
			redisTemplate.opsForValue().get(key);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean Setkey(String key, Object value) {
		boolean result = false;
		try {
			redisTemplate.opsForValue().set(key, value, 60 * 10, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 写入缓存
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, KQRegister value) {
		boolean result = false;
		try {
			redisTemplate.opsForValue().set(key, value, 60 * 2, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取缓存
	 * 
	 * @param key
	 * @return
	 */
	public List<KQRegister> getKq(String key) {
		List<KQRegister> result = (List<KQRegister>) redisTemplate.opsForValue().get(key);
		if (result == null) {
			return null;
		}
		return result;
	}
	
}
