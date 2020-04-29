package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jh.paymentgateway.business.ChannelDetailBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.business.WithdrawRequestBusiness;
import com.jh.paymentgateway.pojo.ChannelDetail;
import com.jh.paymentgateway.util.SpringContextUtil;

@Component
public class PaymentFactory {
	
	@Autowired
	private ChannelDetailBusiness channelDetailBusiness;
	
	private static Map<String,TopupRequestBusiness>  topRequestMap = new HashMap<>();
	
	private static Map<String,WithdrawRequestBusiness>  withdrawRequestMap = new HashMap<>();
	
	public TopupRequestBusiness getTopupRequest(String channelTag){
		if (topRequestMap.containsKey(channelTag)) {
			return topRequestMap.get(channelTag);
		}
		ChannelDetail channelDetail = channelDetailBusiness.findByChannelTag(channelTag.replace("\n","").trim());
		if (channelDetail != null) {
			TopupRequestBusiness bean = (TopupRequestBusiness) SpringContextUtil.getBeanOfType(channelDetail.getBeanName());
			topRequestMap.put(channelTag, bean);
			return bean;
		}else {
			return null;
		}
	}
	
	public WithdrawRequestBusiness getWithdrawRequest(String channelTag){
		if (withdrawRequestMap.containsKey(channelTag)) {
			return withdrawRequestMap.get(channelTag);
		}
		ChannelDetail channelDetail = channelDetailBusiness.findByChannelTag(channelTag);
		if (channelDetail != null) {
			WithdrawRequestBusiness bean = (WithdrawRequestBusiness) SpringContextUtil.getBeanOfType(channelDetail.getChannelSelurl());
			withdrawRequestMap.put(channelTag, bean);
			return bean;
		}else {
			return null;
		}
	}
	
}
