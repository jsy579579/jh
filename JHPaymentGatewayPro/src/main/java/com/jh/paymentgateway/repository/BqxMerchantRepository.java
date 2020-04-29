package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BqxMerchant;

@Repository
public interface BqxMerchantRepository extends JpaRepository<BqxMerchant, Long>,JpaSpecificationExecutor<BqxMerchant>{
	
	@Query("select bq from BqxMerchant bq")
	public List<BqxMerchant> findBqxMerchant();

}
