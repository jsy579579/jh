package com.jh.paymentgateway.business;

import java.util.List;

import com.jh.paymentgateway.pojo.ChannelDetail;

public interface ChannelDetailBusiness {

	ChannelDetail findByChannelTag(String channelTag);
	
	List<ChannelDetail> findByChannelType(String channelType);

}
