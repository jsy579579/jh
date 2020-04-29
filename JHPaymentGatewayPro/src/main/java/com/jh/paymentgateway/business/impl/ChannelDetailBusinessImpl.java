package com.jh.paymentgateway.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.business.ChannelDetailBusiness;
import com.jh.paymentgateway.pojo.ChannelDetail;
import com.jh.paymentgateway.repository.ChannelDetailRepository;

@Service
public class ChannelDetailBusinessImpl implements ChannelDetailBusiness {
	
	@Autowired
	private ChannelDetailRepository channelDetailRepository;

	@Override
	public ChannelDetail findByChannelTag(String channelTag) {
		return channelDetailRepository.findByChannelTag(channelTag);
	}

	@Override
	public List<ChannelDetail> findByChannelType(String channelType) {
		return channelDetailRepository.findByChannelType(channelType);
	}
}
