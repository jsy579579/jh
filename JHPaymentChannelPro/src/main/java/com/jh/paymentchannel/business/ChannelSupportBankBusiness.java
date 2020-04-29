package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.ChannelSupportBank;



public interface ChannelSupportBankBusiness {
	//以通道标识和支持银行卡的名字去查询，该通道是否支持
	public ChannelSupportBank querySupportBankByTagAndNameAndType(String channelTag,String supprortBankName); 
	//以通道标识去查询所有该通道支持的银行卡列表
	public List<ChannelSupportBank> querySupportBankByTag(String channelTag);
	
	public List<ChannelSupportBank> querySupportBankByName(String supportBankName, String supportBankType, String[] channelTag);
	
	public ChannelSupportBank getSupportBankByTagAndNameAndType(String channelTag, String supportBankName, String type);
	
}
