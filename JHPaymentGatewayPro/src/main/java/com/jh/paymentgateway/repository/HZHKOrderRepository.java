package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HZHKOrder;
import com.jh.paymentgateway.pojo.HZHKRegister;

@Repository
public interface HZHKOrderRepository extends JpaRepository<HZHKOrder, String>,JpaSpecificationExecutor<HZHKOrder>{
	
	@Query("select hzhk from HZHKOrder hzhk where hzhk.orderCode=:orderCode")
	public HZHKOrder getHZHKOrderByorderCode(@Param("orderCode") String orderCode);

}
