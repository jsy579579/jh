package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jh.mircomall.bean.BusinessOrder;
import com.jh.mircomall.bean.Order;

public interface BusinessOrderDao {
	int addBusinessOrder(Order order);

	List<Order> businessOrderPage(@Param("businessId") int businessId, @Param("offset") int offset,
			@Param("limit") int limit);

	int updateBusinessOrder(Map map);

	int deleteBusinessOrder(int id);

}
