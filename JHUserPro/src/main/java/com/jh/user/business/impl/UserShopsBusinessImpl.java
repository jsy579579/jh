package com.jh.user.business.impl;



import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserShopsBusiness;
import com.jh.user.pojo.UserShops;
import com.jh.user.repository.ResourceRepository;
import com.jh.user.repository.UserShopsRepository;



@Service
public class UserShopsBusinessImpl implements UserShopsBusiness{
	
	
	
	
	@Autowired
	private EntityManager em;
	/*
	 * 商铺管理
	 */
	@Autowired
	private UserShopsRepository  userShopRepository;

	@Transactional
	@Override
	public UserShops addUserShops(UserShops us) {
		// TODO Auto-generated method stub
		UserShops result =  userShopRepository.save(us);
		em.flush();
		return result;
	}

	@Override
	public UserShops findUserShopsById(long id) {
		// TODO Auto-generated method stub
		return userShopRepository.findUserShopsById(id);
		
	}
	
	@Override
	public List<UserShops> findUserShops() {
		// TODO Auto-generated method stub
		
		return userShopRepository.findUserShops();
	}
	
	@Override
	public UserShops findUserShopsByUid(long userid) {
		// TODO Auto-generated method stub
		return userShopRepository.findUserShopsByUid(userid);
	}
	
	/**
	 * 修改商户审核状态
	 * **/
	@Override
	public UserShops updateUserShopsStatusByUid(long userid,String status){
		
		return userShopRepository.updateUserShopsByUid(userid, status) ;
	}

	//获取shops表中随机的userid
	@Override
	public String[] queryRandomUseridByAll() {
		return userShopRepository.queryRandomUseridByAll();
	}

}
