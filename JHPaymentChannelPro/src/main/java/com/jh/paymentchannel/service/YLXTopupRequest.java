package com.jh.paymentchannel.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;

import okhttp3.MediaType;

@Service
public class YLXTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(YLXTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	@Autowired
	private YLXpageRequest ylXpageRequest;
	
	private String appId = "7aee88cd4b654e2692664851b9603acf";
	
	private String customerNo = "gl00024678";
	
	private String key = "123456";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		
		Map<String,String> map = new HashMap<String, String>();
		
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		
		String ordercode = paymentOrder.getOrdercode();
		
		map = (Map<String, String>) ylXpageRequest.ylxFastPay(ordercode);
		
		return map;
	}

}
