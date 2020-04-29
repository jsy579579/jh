package cn.jh.clearing.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jh.clearing.business.ChannelCostRateBusiness;
import cn.jh.clearing.pojo.ChannelBankRate;
import cn.jh.clearing.pojo.ChannelCostRate;
import cn.jh.clearing.repository.ChannelBankRateRepository;
import cn.jh.clearing.repository.ChannelCostRateRepository;
import cn.jh.clearing.service.ProfitService;
import cn.jh.clearing.util.Util;

@Service
public class ChannelCostRateBusinessImpl implements ChannelCostRateBusiness{
	private static final Logger LOG = LoggerFactory.getLogger(ProfitService.class);
	@Autowired
	Util util;
	
	@Autowired
	private EntityManager em;

	@Autowired
	private ChannelCostRateRepository channelCostRateRepository;
	
	@Autowired
	private ChannelBankRateRepository channelBankRateRepository;
	
	@Override
	public ChannelCostRate getChannelCostRateByChannelTag(String channelTag) {
		em.clear();
		ChannelCostRate result = channelCostRateRepository.getChannelCostRateByChannelTag(channelTag);
		return result;
	}

	@Override
	public List<ChannelCostRate> getAllChannelCostRate() {
		em.clear();
		List<ChannelCostRate> result = channelCostRateRepository.getAllChannelCostRate();
		return result;
	}
	
	@Override
	public ChannelBankRate getChannelBankRateByChannelTagAndBankName(String channelTag, String bankName) {
		em.clear();
		ChannelBankRate result = channelBankRateRepository.getChannelBankRateByChannelTagAndBankName(channelTag, bankName);
		return result;
	}

	@Override
	public List<ChannelBankRate> getChannelBankRateByChannelTag(String channelTag) {
		em.clear();
		List<ChannelBankRate> result = channelBankRateRepository.getChannelBankRateByChannelTag(channelTag);
		return result;
	}
	
	
}
