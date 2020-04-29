package com.jh.good.repository;

import com.jh.good.pojo.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 订单
 */
public interface OrderRepository extends JpaRepository<Order,String>, JpaSpecificationExecutor<Order> {
}
