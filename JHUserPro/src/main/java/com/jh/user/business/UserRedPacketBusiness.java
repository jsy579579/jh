package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.UserRedPacket;

public interface UserRedPacketBusiness {
	/**
	 * 查询所有抽奖信息
	 * */
	public List<UserRedPacket> findUserRedPacket();
	
	/**
	 * 获取单个奖项信息
	 * */
	public UserRedPacket findUserRedPacketById(long id);
	
	/**
	 * 获取贴牌所设置信息
	 * **/
	public List<UserRedPacket> findUserRedPacketByBid(long brand_id);
	/**
	 * 添加/修改红包信息
	 * **/
	public UserRedPacket saveUserRedPacket(UserRedPacket urp);

	
}
