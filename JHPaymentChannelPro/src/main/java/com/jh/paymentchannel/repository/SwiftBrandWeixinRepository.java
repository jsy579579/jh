package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.SwiftBrandAlipayMerchant;
import com.jh.paymentchannel.pojo.SwiftBrandWeixinMerchant;

@Repository
public interface SwiftBrandWeixinRepository extends JpaRepository<SwiftBrandWeixinMerchant,String>,JpaSpecificationExecutor<SwiftBrandWeixinMerchant>{

	
	@Query("select brandweixin from  SwiftBrandWeixinMerchant brandweixin where brandweixin.brandid =:brandid and brandweixin.activestatus ='0'")
	public SwiftBrandAlipayMerchant	getSwiftBrandAlipayMerchantByAppid(@Param("brandid") String brandid);
	
	
}

