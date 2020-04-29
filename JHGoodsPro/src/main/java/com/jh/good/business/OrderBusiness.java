package com.jh.good.business;

import com.jh.good.pojo.Order;
import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderBusiness {
    // 保存订单
    void save(Order order);
    // 获取用户的订单
    List<Order> findByToken(Long userId,String status);
    // 修改订单的状态
    void update(String ordercode, String status);
    // 分页 带订单搜索
    Page<Order> searchGoods(int page, int size, Order order);
    // 获取单个订单
    Order fingById(String orderCode);
}
