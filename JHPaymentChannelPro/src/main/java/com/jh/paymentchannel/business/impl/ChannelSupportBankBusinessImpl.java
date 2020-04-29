package com.jh.paymentchannel.business.impl;

import java.util.List;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.ChannelSupportBankBusiness;
import com.jh.paymentchannel.pojo.ChannelSupportBank;
import com.jh.paymentchannel.repository.ChannelSupportBankRepository;
@Service
public class ChannelSupportBankBusinessImpl implements ChannelSupportBankBusiness {
	
	@Autowired
	private  ChannelSupportBankRepository channelsupportbankrepository;
	
	//以通道标识和支持银行卡的名字去查询，该通道是否支持
	@Override
	public ChannelSupportBank querySupportBankByTagAndNameAndType(String channelTag, String supprortBankName) {
		return channelsupportbankrepository.querySupportBankByTagAndNameAndType(channelTag, supprortBankName);
	}

	//以通道标识去查询所有该通道支持的银行卡列表
	@Override
	public List<ChannelSupportBank> querySupportBankByTag(String channelTag) {
		return channelsupportbankrepository.querySupportBankByTag(channelTag);
	}
	
	@Override
	public List<ChannelSupportBank> querySupportBankByName(String supportBankName, String supportBankType, String[] channelTag) {
		return channelsupportbankrepository.querySupportBankByName(supportBankName, supportBankType, channelTag);
	}
	@Override
	public ChannelSupportBank getSupportBankByTagAndNameAndType(String channelTag, String supportBankName,
			String type) {
		return channelsupportbankrepository.getSupportBankByTagAndNameAndType(channelTag, supportBankName, type);
	}
	
	
	
	
}
