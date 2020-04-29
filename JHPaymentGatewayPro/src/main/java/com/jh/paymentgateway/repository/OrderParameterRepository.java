package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.OrderParameter;

@Repository
public interface OrderParameterRepository extends JpaRepository<OrderParameter, Long>,JpaSpecificationExecutor<OrderParameter> {

	OrderParameter findByOrderCode(String orderCode);

}
