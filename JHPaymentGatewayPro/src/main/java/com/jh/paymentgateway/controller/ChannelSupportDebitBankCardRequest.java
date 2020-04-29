package com.jh.paymentgateway.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.pojo.ChannelSupportDebitBankCard;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class ChannelSupportDebitBankCardRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(ChannelSupportDebitBankCardRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/channelsupportdebitbankcard/getbychanneltag/andbankname")
	public @ResponseBody Object getByChannelTagAndBankName(HttpServletRequest request,
			@RequestParam(value = "channelTag") String channelTag, 
			@RequestParam(value = "bankName") String bankName
			) throws Exception {
		
		ChannelSupportDebitBankCard result = null;
		try {
			result = topupPayChannelBusiness.getChannelSupportDebitBankCardByChannelTagAndBankName(channelTag, bankName);
		} catch (Exception e) {
			LOG.error("查询到账卡支持信息异常======",e);
			
			return ResultWrap.init(CommonConstants.FALIED, "查询异常");
		}
		
		if(result != null) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", result);
		}else {
			
			List<String> channelSupportDebitBankCardByChannelTag = topupPayChannelBusiness.getChannelSupportDebitBankCardByChannelTag(channelTag);
			
			LOG.info("channelSupportDebitBankCardByChannelTag=======" + channelSupportDebitBankCardByChannelTag);
			
			return ResultWrap.init("888888", "查询成功!", channelSupportDebitBankCardByChannelTag);
		}
		
	}
	
}
