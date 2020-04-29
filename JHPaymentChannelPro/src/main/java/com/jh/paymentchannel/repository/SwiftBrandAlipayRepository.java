package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.AlipayLifeNo;
import com.jh.paymentchannel.pojo.SwiftBrandAlipayMerchant;

@Repository
public interface SwiftBrandAlipayRepository extends JpaRepository<SwiftBrandAlipayMerchant,String>,JpaSpecificationExecutor<SwiftBrandAlipayMerchant>{

	
	@Query("select brandalipay from  SwiftBrandAlipayMerchant brandalipay where brandalipay.brandid =:brandid and brandalipay.activestatus ='0'")
	public SwiftBrandAlipayMerchant	getSwiftBrandAlipayMerchantByAppid(@Param("brandid") String brandid);
	
	
}
