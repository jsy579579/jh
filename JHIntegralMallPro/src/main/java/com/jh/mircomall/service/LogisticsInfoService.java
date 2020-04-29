package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.LogisticsInfo;
@SuppressWarnings("all")
public interface LogisticsInfoService {
	
	/**
     * Json方式 查询订单物流轨迹
     * @param expCode 快递公司编号
     * @param expNo 快递单号
	 * @throws Exception 
     */
	String getOrderTracesByJson(String expCode, String expNo) throws Exception;

	/**
	 * 添加物流信息
	 * */
	public int addLogisticsInfo(LogisticsInfo logisticsInfo);
	
	/**
	 * 商家完善用户物流信息
	 * */
	public int updateLogisticsInfoBusiness(LogisticsInfo logisticsInfo);

	/**
	 * 查询所有物流信息
	 * */
	public List<LogisticsInfo> listAllLogisticsInfo(int pageindex,int pageSize);
	
	/**
	 * 根据物流号或者userid查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByNumOrUserId(String NumOrUserId);
	
	/**
	 * 根据物流号和userid查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByUserIdAndOrderId(Integer userId,String orderId);
	
	/**
	 * 根据userid或者订单号或者手机号查询物流信息
	 * */
	public List<LogisticsInfo> listLogisticsInfoByUserId(Integer userId,String userPhone,String orderId);
	
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
	public List<LogisticsInfo> listLogisticsInfoBlur(Integer userId,String userPhone, String orderId);
	/**
	 * 根据订单id查询物流信息
	 * @Author sy
	 * @param orderId
	 * @return
	 */
	public List<LogisticsInfo> listLogisticsInfoByOrderId(Integer orderId);



	
	
}
