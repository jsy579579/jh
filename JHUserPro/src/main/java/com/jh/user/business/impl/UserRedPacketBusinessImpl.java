package com.jh.user.business.impl;



import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserRedPacketBusiness;
import com.jh.user.pojo.UserRedPacket;
import com.jh.user.repository.ResourceRepository;
import com.jh.user.repository.UserRedPacketRepository;



@Service
public class UserRedPacketBusinessImpl implements UserRedPacketBusiness{
	
	
	
	
	@Autowired
	private EntityManager em;
	/*
	 * 红包
	 */
	@Autowired
	private UserRedPacketRepository  userRedPacketRepository;
	
	/**
	 *获取所有红包数据
	 * **/
	@Override
	public List<UserRedPacket> findUserRedPacket() {
		
		return userRedPacketRepository.findAll();
	}
	@Override
	public UserRedPacket findUserRedPacketById(long id) {
		return userRedPacketRepository.findUserShopsById(id);
	}
	@Override
	public List<UserRedPacket> findUserRedPacketByBid(long brand_id) {
		return userRedPacketRepository.findUserShopsBybrandId(brand_id);
	}
	@Override
	@Transactional
	public UserRedPacket saveUserRedPacket(UserRedPacket urp) {
		
		return userRedPacketRepository.save(urp);
	}

	

}
