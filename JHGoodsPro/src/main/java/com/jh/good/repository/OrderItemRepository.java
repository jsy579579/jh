package com.jh.good.repository;

import com.jh.good.pojo.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 订单
 */
public interface OrderItemRepository extends JpaRepository<OrderItem,String>, JpaSpecificationExecutor<OrderItem> {

}
