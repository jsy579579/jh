package com.jh.mircomall.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.Order;

@SuppressWarnings("all")
public interface OrderService {
	/**
	 * 新增订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月9日
	 * @param order
	 * @return
	 */
	public int addOrder(Order order);

	/**
	 * 用户放弃支付订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月9日
	 * @param order
	 * @return
	 */
	public int giveUpTheOrder(Order order);

	/**
	 * 用户结算订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月9日
	 * @param order
	 * @return
	 */
	public int alreadySettled(Order order, String paypassword, String token);

	/**
	 * 查询用户的订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月10日
	 * @param userId
	 *            用户ID
	 * @param pageNo
	 *            起始页
	 * @param pageSize
	 *            每页数量
	 * @return
	 */
	public List<Order> getOrderListByUser(int userId, int pageNo, int pageSize);

	/**
	 * 根据商品Id查询订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param orderId
	 * @return
	 */
	public Order getOrder(int goodsId);

	/**
	 * 用户等待支付的订单
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param userId
	 * @param brandId
	 * @param status
	 *            0:等待支付 ；1：放弃支付 2：完成支付
	 * @return
	 */
	public List<Order> getOrderWaitingforPayment(int pageIndex, int pageSize, int userId, int status);

	/**
	 * 逻辑删除订单 也就是修改订单的逻辑删除字段
	 * 
	 * @Author ChenFan
	 * @Date 2018年5月11日
	 * @param id
	 * @return
	 */
	public int removeOrder(int id);

	/**
	 * 根据订单ID查询订单信息
	 * 
	 * @Author ChenFan
	 * @param id
	 * @return
	 */
	public List<Order> getOrderById(int id);

	/**
	 * 修改订单
	 * 
	 * @Author ChenFan
	 * @param order
	 * @return
	 */
	public int updateOrder(Order order);

	/**
	 * 根据商品积分和订单数量 计算应付积分
	 * 
	 * @Author ChenFan
	 * @param goodsId
	 * @param orderNum
	 * @return
	 */
	public int calculationIntegral(int goodsId, int orderNum);

	int getOrderCount(int userId);

	int getOrderCountByStatus(int userId, int status);

	/**
	 * 根据订单号查询该订单
	 * 
	 * @param orderCode
	 * @return
	 */
	public List<Order> getOrderByOrderCode(String orderCode);

}
