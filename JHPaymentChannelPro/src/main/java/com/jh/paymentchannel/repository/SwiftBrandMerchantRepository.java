package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.SwiftBrandMerchant;

@Repository
public interface SwiftBrandMerchantRepository extends JpaRepository<SwiftBrandMerchant,String>,JpaSpecificationExecutor<SwiftBrandMerchant>{

	@Query("select brandmerchant from  SwiftBrandMerchant brandmerchant where brandmerchant.brand_id =:brandid")
	public SwiftBrandMerchant	getSwiftBrandMerchant(@Param("brandid") String brandid);
	
	@Query("select brandmerchant from  SwiftBrandMerchant brandmerchant where brandmerchant.subMerchantid =:subMerchantid")
	public SwiftBrandMerchant getSwiftBrandMerchantByMchId(@Param("subMerchantid") String subMerchantid);

}
