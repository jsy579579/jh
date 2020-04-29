package com.jh.mircomall.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.User;
import com.jh.mircomall.bean.UserAccount;
import com.jh.mircomall.dao.UserAccountDao;
import com.jh.mircomall.dao.UserDao;
@Service
public class UserAccountServiceImpl implements UserAccountService{
	@Autowired
	private  UserAccountDao userAccountDao;
	@Autowired
	private UserDao userDao;
	@Override
	public  Map<Object, Object> getUserInfo(int userId) {
		//查询信息
		User user = userDao.getUserInfo(userId);
		UserAccount userCoin = userAccountDao.selectUserCoin(userId);
		Map<Object, Object> map=new HashMap<>();
		map.put("nickName", user.getNickName());
		map.put("phone", user.getPhone());
		map.put("coin", userCoin.getCoin());
		map.put("balance", userCoin.getBalance());
		return map;
	}
	@Override
	public int modifyUserAccount(UserAccount userAccount) {
	
		return userAccountDao.updateUserAccount(userAccount);
	}

}
