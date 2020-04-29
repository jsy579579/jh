package com.jh.good.business.impl;

import com.jh.good.business.OrderBusiness;
import com.jh.good.pojo.Order;
import com.jh.good.repository.OrderItemRepository;
import com.jh.good.repository.OrderRepository;
import com.jh.good.util.GoodConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现
 */
@Service
public class OrderBusinessImpl implements OrderBusiness {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;
    /**
     * 保存订单
     */
    @Transactional
    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    // 根据token查询订单
    @Override
    public List<Order> findByToken(Long userId,String status) {
        Specification spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("userId"), userId));
                if (!"0".equals(status)) {
                    predicates.add(cb.equal(root.get("status"), status));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return orderRepository.findAll(spec);
    }

    // 修改订单状态，以及时间
    @Transactional
    @Override
    public void update(String ordercode, String status) {
        Specification<Order> spect = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder cb) {
                //根据id查询
                return cb.equal(root.get("id"), ordercode);
            }
        };
        Order order = orderRepository.findOne(spect);
        if (order != null) {
            order.setStatus(status);
            // 完成支付
            if (status.equals(GoodConstants.ORDER_PAID)) {
                order.setPaymentTime(new Date());
                order.setUpdateTime(new Date());
            }
            // 完成发货
            if (status.equals(GoodConstants.ORDER_SNIPPED)) {
                order.setConsignTime(new Date());
                order.setUpdateTime(new Date());
            }
            // 完成交易
            if (status.equals(GoodConstants.ORDER_SUCCESSFUL_TRADE)) {
                order.setEndTime(new Date());
                order.setUpdateTime(new Date());
            }
            // 交易关闭
            if (status.equals(GoodConstants.ORDER_TRANSACTION_CLOSE)) {
                order.setCloseTime(new Date());
                order.setUpdateTime(new Date());
            }
            orderRepository.save(order);
        }
    }
    // 分页 带订单搜索
    @Override
    public Page<Order> searchGoods(int page, int size, Order order) {
        Pageable pageable = new PageRequest(page,size);
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (order != null) {
                    if (order.getId()!= null && !"".equals(order.getId())) {
                        predicates.add(cb.equal(root.get("id"), order.getId()));
                    }
                    if (order.getReceiverMobile() != null && !"".equals(order.getReceiverMobile())) {
                        predicates.add(cb.equal(root.get("receiver_mobile"), order.getReceiverMobile()));
                    }
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<Order> pageList = orderRepository.findAll(specification, pageable);
        return pageList;
    }

    @Override
    public Order fingById(String orderCode) {
        Specification<Order> spec = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder cb) {
                //根据id查询
                return cb.equal(root.get("id"), orderCode);
            }
        };
        return orderRepository.findOne(spec);
    }

}
