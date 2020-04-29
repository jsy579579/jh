package com.jh.paymentchannel.service;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.pojo.JPBindCard;
import com.jh.paymentchannel.pojo.JPRegister;
import com.jh.paymentchannel.pojo.KYRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;

import net.sf.json.JSONObject;

@Service
public class KYTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(KYTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private KYpageRequest kypageRequest;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String channelTag = paymentOrder.getChannelTag();
		String userId = paymentOrder.getUserid() + "";
		
		Map<String, String> map = new HashMap<String, String>();
		
		

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String idCard = fromObject.getString("idcard");

		String arriveAccountType = null;
		if("KY_QUICK".equalsIgnoreCase(channelTag)) {
			LOG.info("有积分======");
			arriveAccountType = "32";
		}else if("KY_QUICK1".equalsIgnoreCase(channelTag)) {
			LOG.info("无积分======");
			arriveAccountType = "35";
		}
		
		
		KYRegister kyRegister = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);
		
		if(kyRegister == null) {
			
			map = (Map<String, String>) kypageRequest.kyRegister(request, ordercode, arriveAccountType);
			
			return map;
			
		}else {
			
			map = (Map<String, String>) kypageRequest.kyFastPay(request, ordercode, arriveAccountType);
			
			return map;
			
		}
		
	}

	
	

}
