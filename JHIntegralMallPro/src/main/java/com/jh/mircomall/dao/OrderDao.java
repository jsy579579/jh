package com.jh.mircomall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.Order;

public interface OrderDao {
	/**
	 * 新增订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月9日
	 * @param order
	 * @return
	 */
	public int insertOrder(Order order);

	/**
	 * 修改订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月9日
	 * @param order
	 * @return
	 */
	public int updateOrder(Order order);

	/**
	 * 得到用户对应的订单 按照时间降序
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月10日
	 * @param userId
	 *            用户ID
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<Order> selectOrderList(@Param("userid") int userId, @Param("offset") int offset,
			@Param("limit") int limit);

	/**
	 * 根据商品ID查询订单信息
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param goodsId
	 * @return
	 */
	public List<Order> selectOrderByGoodsId(@Param("goodsid") int goodsId);

	/**
	 * 根据状态查询订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param status
	 * @return
	 */
	public List<Order> selectOrderByStatus(@Param("userId") int userId, @Param("status") int status,
			@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * 根据订单ID查询订单
	 * 
	 * @Author ChenFan
	 * @param id
	 * @return
	 */
	public List<Order> selectOrderById(@Param("id") int id);

	/**
	 * 商户订单分页
	 * 
	 * @Author lirui
	 * @param businessId
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<Order> businessOrderPage(@Param("businessId") int businessId, @Param("offset") int offset,
			@Param("limit") int limit);

	/**
	 * 获取用户订单总条数
	 * 
	 * @author lirui
	 * @param userId
	 * @return
	 */
	int selectOrderCount(int userId);

	/**
	 * 获取用戶不同状态下订单总条数
	 * 
	 * @param userId
	 * @param status
	 * @return
	 */
	int selectOrderCountByStatus(@Param("userId") int userId, @Param("status") int status);

	/**
	 * 获取商户订单总条数
	 * 
	 * @param businessId
	 * @return
	 */
	int selectBusinessOrderCount(@Param("businessId") int businessId);

	/**
	 * 根据订单号查询订单
	 * 
	 * @param orderCode
	 * @return
	 */
	public List<Order> selectOrderByOrderCode(@Param("orderCode") String orderCode);

}
