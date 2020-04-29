package com.jh.paymentgateway.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.HQRegister;


public interface HQRegisterRepository extends JpaRepository<HQRegister, String>,JpaSpecificationExecutor<HQRegister>{
	
	@Query("select hq from HQRegister hq where hq.idCard=:idCard")
	public HQRegister getHQRegisterByIdCard(@Param("idCard") String idCard);

	
	@Query("select hq from HQRegister hq where hq.merchantOrder=:merchantOrder")
	public HQRegister getHQRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);

	

}
