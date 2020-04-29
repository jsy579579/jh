package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.LogisticsInfo;

public interface LogisticsInfoDao {
	
	/**
	 * 添加物流信息
	 * goodsId:商品id
	 * orderId:订单号
	 * logisticsName:物流名称
	 * logisticsNum:物流单号
	 * userId:用户ID
	 * userAddr:收件人地址
	 * userPhone:收件人电话
	 * userProvinceId:收件人省份
	 * userCityId:收件人城市
	 * userAreasId:收件人区域/镇
	 * businessName:寄件人姓名
	 * businessPhone:寄件人电话
	 * businessProvinceId:寄件人省份
	 * businessCityId:寄件人城市
	 * businessAreasId:寄件人区域/镇 
	 * */
	public int addLogisticsInfo(LogisticsInfo logisticsInfo);
	
	/**
	 * 商家完善用户物流信息
	 * */
	public int updateLogisticsInfoBusiness(LogisticsInfo logisticsInfo);

	/**
	 * 查询所有物流信息
	 * */
	public List<LogisticsInfo> listAllLogisticsInfo(@Param("offset") int offset,@Param("limit") int limit);
	
	/**
	 * 根据物流号和userid查询具体单号的物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByUserIdAndOrderId(@Param("userId") Integer userId,@Param("orderId") String orderId);
	
	/**
	 * 根据userid或者手机号或者订单号查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByUserId(@Param("userId") Integer userId,@Param("userPhone") String userPhone,@Param("orderId") String orderId);
	
	/**
	 * 根据物流号或者userid查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByNumOrUserId(String NumOrUserId);
	
	/**
	 * 根据物流号和userid更新物流信息
	 * */
	public int updateLogisticsInfoByUserIdAndOrderId(LogisticsInfo logisticsInfo);
	
	/**
	 * 根据物流号或者userid删除物流信息
	 * */
	public int deleteLogisticsInfoByUserIdAndOrderId(LogisticsInfo logisticsInfo);
	
	/**
	 * 模糊查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoBlur(@Param("userId") Integer userId,@Param("userPhone") String userPhone,@Param("orderId") String orderId);
	/**
	 * 根据订单id查询物流信息
	 * @Author sy
	 * @param orderId
	 * @return
	 */
	public List<LogisticsInfo> listLogisticsInfoByOrderId(Integer orderId);
}
