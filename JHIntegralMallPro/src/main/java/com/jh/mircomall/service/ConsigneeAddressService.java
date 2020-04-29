package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.ConsigneeAddress;

public interface ConsigneeAddressService {
	/**
	 * 新增用户地址 当用户新增第一条的时候 就自动设置为默认地址 其余的就不是默认地址
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param consigneeAddress
	 * @return
	 */
	int addConsigneeAddress(ConsigneeAddress consigneeAddress);

	/**
	 * 用户删除地址
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param userId
	 * @param consigneeAddressId
	 * @return
	 */
	int removeConsignAddress(int userId, int consigneeAddressId);

	/**
	 * 根据用户Id查询收件人地址数量
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param userId
	 * @return
	 */
	int getCountConsignAddressByUserId(int userId);

	/**
	 * 根据用户ID查询地址
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param userId
	 * @return
	 */
	List<ConsigneeAddress> getAllVonsigneeAddress(int userId, int isTimeOrderBy);

	/**
	 * 根据用户Id和默认地址进行查询
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param userId
	 * @param default_addr
	 * @return
	 */
	ConsigneeAddress getConsigneeAddress(int userId, int defaultAddr);

	/**
	 * 修改用户地址
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param consigneeAddress
	 * @return
	 */
	int modifyConsigneeAddress(ConsigneeAddress consigneeAddress);

	/**
	 * 根据地址ID查询单一数据
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月8日
	 * @param id
	 * @return
	 */
	ConsigneeAddress getConsigneeAddressById(int id);

	int modifydefaultAddress(ConsigneeAddress consigneeAddress);
}
