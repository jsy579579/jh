package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.ConsigneeAddress;

public interface ConsigneeAddressDao {
	/**
	 * 新增收件人地址
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param consigneeAddress
	 * @return
	 */
	int insertConsigneeAddress(ConsigneeAddress consigneeAddress);
	/**
	 * 根据用户地址ID删除地址
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param consignAddressId
	 * @return
	 */
	int deleteConsigneeAddress(@Param("id")int consignAddressId);
	/**
	 * 根据用户Id查询地址数量
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param userId
	 * @return
	 */
	int countConsigneeAddressByUserId(@Param("userid") int userId);
/**
 * 查询用户所有地址
 *@Author ChenFan
 *@Date 2018年5月8日
 * @param userId 
 * @param isTimeOrderBy  0：根据是否默认地址排序
 * 											1：按照最新时间排序
 * @return
 */
	List<ConsigneeAddress> selectAllConsigneeAddress(@Param("userid")int userId,@Param("istimeorderby") int isTimeOrderBy);
	/**
	 * 根据用户ID以及默认地址进行查询
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param userId
	 * @param defaultAddr
	 * @return
	 */
	ConsigneeAddress selectConsigneeAddress(@Param("userid") int userId,@Param("defalutaddr") int defaultAddr);
	/**
	 * 修改用户地址
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param consigneeAddress
	 * @return
	 */
	int updateConsigneeAddress(ConsigneeAddress consigneeAddress);
	/**
	 * 根据地址ID查询数据
	 *@Author ChenFan
	 *@Date 2018年5月8日
	 * @param id
	 * @return
	 */
	ConsigneeAddress selectConsigneeAddressById(@Param("id") int id);
}
