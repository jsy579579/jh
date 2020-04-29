package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.ChannelRateBusiness;
import com.jh.user.pojo.BrandRate;
import com.jh.user.pojo.Channel;
import com.jh.user.pojo.ChannelRate;
import com.jh.user.pojo.User;
import com.jh.user.repository.BrandRateRepository;
import com.jh.user.repository.ChannelRateRepository;
import com.jh.user.repository.ChannelRepository;
import com.jh.user.repository.UserRepository;

@Service
public class ChannelRateBusinessImpl implements ChannelRateBusiness {

	@Autowired
	private ChannelRateRepository channelRateRepository;

	@Autowired
	private ChannelRepository channelRepository;

	@Autowired
	private BrandRateRepository brandRateRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public Channel mergeChannel(Channel channel) {
		Channel result = channelRepository.save(channel);
		em.flush();
		return result;
	}

	@Override
	public Channel findChannelByTag(String channelTag) {
		return channelRepository.findChannelByTag(channelTag);
	}

	@Override
	public Channel findChannelById(long channelid) {

		Channel channel = new Channel();
		channel = channelRepository.findChannelById(channelid);
		return channel;
	}

	@Transactional
	@Override
	// @CachePut(value = "ChannelRate", key = "#channelRate.userId +':'+
	// #channelRate.channelId")
	public ChannelRate mergeChannelRate(ChannelRate channelRate) {
		ChannelRate result = channelRateRepository.save(channelRate);
		em.flush();
		em.clear();
		return result;
	}

	@Override
	// @Cacheable(value = "ChannelRate",key = "#userid +':'+ #channelid")
	public ChannelRate findChannelRateByUserid(long userid, long channelid) {
		User user = userRepository.findUserById(userid);
		ChannelRate channelRate = channelRateRepository.findChannelRateByUserid(userid, user.getBrandId(), channelid);
		em.clear();
		return channelRate;
	}

	@Override
	public ChannelRate findChannelRateByUserid(long userid, long channelid, long brandId) {
		em.clear();
		return channelRateRepository.findChannelRateByUserid(userid, brandId, channelid);
	}

	@Override
	public List<ChannelRate> findChannelRateByBrandid(long brandid, long channelid) {

		return channelRateRepository.findChannelRateByBrandid(brandid, channelid);
	}

	@Override
	public List<Channel> findAllChannel() {
		// TODO Auto-generated method stub
		return channelRepository.findAll();
	}

	// 通过品牌Id查询通道
	@Override
	public List<Channel> findAllChannelByBrandid(long brandid) {

		List<Channel> channels = new ArrayList<Channel>();

		List<BrandRate> brandRates = brandRateRepository.findBrandRateBybrandid(brandid);

		for (BrandRate brandRate : brandRates) {
			if ("1".equals(brandRate.getStatus())) {
				Channel channel = channelRepository.findChannelById(brandRate.getChannelId());
				if (channel != null) {
					if ("1".equals(channel.getStatus()))
						channel.setStatus(brandRate.getStatus());
					channel.setRate(brandRate.getRate());
					channel.setExtraFee(brandRate.getExtraFee());
					channel.setWithdrawFee(brandRate.getWithdrawFee());
					channels.add(channel);
				}
			}
		}
		return channels;
	}

	@Override
	public List<ChannelRate> findChannelRates() {
		return channelRateRepository.findAll();
	}

	// 根据ChannelNo查询通道
	@Override
	public List<Channel> findChannelByChannelNo(String channelNo) {
		return channelRepository.findChannelByChannelNo(channelNo);
	}

	@Transactional
	@Override
	// @CachePut(value = "ChannelRate", key = "#userid +':'+ #channelid")
	public void updChannelRateBybrandidAndChannelidanduserId(BigDecimal minrate, long brandid, long channelid,
			long userid) {
		channelRateRepository.updChannelRateBybrandidAndChannelidanduserId(minrate, brandid, channelid, userid);
		em.flush();
		em.clear();
	}

	@Override
	public List<ChannelRate> findChannelRateByChannelId(long channelid, BigDecimal costRate) {
		return channelRateRepository.findChannelRateByChannelId(channelid, costRate);
	}

	@Override
	public Channel findByChannelId(long channelId) {
		return channelRepository.findById(channelId);
	}

	@Override
	public List<String> findChannelTagByChannelNo(String channelNo) {
		em.clear();
		List<String> result = channelRepository.findChannelTagByChannelNo(channelNo);
		return result;
	}

	@Override
	public List<Channel> findChannelByChannelNoAndStatusAndPaymentStatus(String channelNo, String autoClearing,
			String status, String paymentStatus) {
		em.clear();
		List<Channel> result = channelRepository.findChannelByChannelNoAndStatusAndPaymentStatus(channelNo,
				autoClearing, status, paymentStatus);
		return result;
	}

}
