package cn.jh.clearing.business;

import java.util.List;

import cn.jh.clearing.pojo.ChannelBankRate;
import cn.jh.clearing.pojo.ChannelCostRate;

public interface ChannelCostRateBusiness {
	
	ChannelCostRate getChannelCostRateByChannelTag(String channelTag);
	
	public List<ChannelCostRate> getAllChannelCostRate();
	
	public ChannelBankRate getChannelBankRateByChannelTagAndBankName(String channelTag, String bankName);
	
	public List<ChannelBankRate> getChannelBankRateByChannelTag(String channelTag);
	
}
