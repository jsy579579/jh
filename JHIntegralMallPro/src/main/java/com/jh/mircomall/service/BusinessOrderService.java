package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Order;

public interface BusinessOrderService {
	/**
	 * 生成商户订单
	 * 
	 * @param map
	 * @return
	 */
	int addBusinessOrder(Order order);

	/**
	 * 商户订单分页
	 * 
	 * @param businessId
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<Order> getBusinessOrderPage(int businessId, int currentPage, int pageSize);

	/**
	 * 商户修改订单
	 * 
	 * @param map
	 * @return
	 */
	int modifyBusinessOrder(Map map);

	/**
	 * 商户删除订单
	 * 
	 * @return
	 */
	int removeBusinessOrder(int id);

	/**
	 * 查询订单总条数
	 * 
	 * @param businessId
	 * @return
	 */
	int getBusinessOrderCount(int businessId);
}
