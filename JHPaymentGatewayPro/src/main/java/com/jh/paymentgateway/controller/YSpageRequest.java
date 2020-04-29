package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class YSpageRequest extends BaseChannel {

	private static final Logger log = LoggerFactory.getLogger(YSpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	/**
	 * 交易回调
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ys/payCallback")
	public void payCallback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		log.info("银商交易回调回来了-----------------");
		
		// 获取支付报文参数
		Map<String, String[]> params = request.getParameterMap();
		log.info("params================" + params);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : params.keySet()) {
			String[] values = params.get(key);
			if (values.length > 0) {
				map.put(key, values[0]);
			}
		}
		log.info("map================" + map);
		

		String respCode =map.get("billStatus");
		String sysTraceNum = map.get("billNo");//请求的订单号
		String orderId = map.get("notifyId");//返回的订单号
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(sysTraceNum);
		
		if ("PAID".equals(respCode)) {
			log.info("交易订单号：" + sysTraceNum);
			
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			log.info("*********************交易成功***********************");
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", sysTraceNum);
			requestEntity.add("third_code", orderId);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("",e);
			}

			log.info("订单状态修改成功===================" + sysTraceNum + "====================" + result);

			log.info("订单已交易成功!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			log.info("交易订单号：" + sysTraceNum);
			this.addOrderCauseOfFailure(sysTraceNum, "交易失败", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}

	}
}
