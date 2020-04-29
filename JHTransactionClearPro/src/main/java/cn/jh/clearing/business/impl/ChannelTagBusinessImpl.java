package cn.jh.clearing.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jh.clearing.business.ChannelTagBusiness;
import cn.jh.clearing.repository.ChannelTagRepository;
@Service
public class ChannelTagBusinessImpl implements ChannelTagBusiness {

	@Autowired
	private ChannelTagRepository channelTagRepository;

	@Autowired
	private EntityManager em;
	
	@Override
	public List<String> getChannelTag() {
		em.clear();
		List<String> result = channelTagRepository.getChannelTag();
		return result;
	}
	
}
