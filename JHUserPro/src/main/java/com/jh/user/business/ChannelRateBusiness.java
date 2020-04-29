package com.jh.user.business;

import java.math.BigDecimal;
import java.util.List;


import com.jh.user.pojo.Channel;
import com.jh.user.pojo.ChannelRate;

public interface ChannelRateBusiness {

	
	public Channel  mergeChannel(Channel channel);
	
	
	public Channel  findChannelByTag(String channelTag);
	
	
	public Channel  findChannelById(long channelid);
	
	
	public List<Channel> findAllChannel();
	
	
	public List<Channel> findAllChannelByBrandid(long brandid);
 	
	
	public ChannelRate mergeChannelRate(ChannelRate channelRate);
	
	
	public ChannelRate findChannelRateByUserid(long userid,  long channelid);
	
	public ChannelRate findChannelRateByUserid(long manageid, long channelId, long brandId);
	
	public  List<ChannelRate>  findChannelRateByBrandid(long brandid,  long channelid);
	
	public List<ChannelRate>  findChannelRates();
	
	public List<Channel> findChannelByChannelNo(String channelNo);
	
	public List<String> findChannelTagByChannelNo(String channelNo);
	
	//通过brandid和channelid查询ChannelRate,把minrate的值赋给rate
	public void  updChannelRateBybrandidAndChannelidanduserId(BigDecimal minrate, long brandid,  long channelid,long userid);

	public List<ChannelRate> findChannelRateByChannelId(long channelid,BigDecimal costRate);

	public Channel findByChannelId(long channelId);

	public List<Channel> findChannelByChannelNoAndStatusAndPaymentStatus(String channelNo, String autoClearing, String status, String paymentStatus);
	
}
