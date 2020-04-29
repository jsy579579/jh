package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.MHHQRegister;


public interface MHHQRegisterRepository extends JpaRepository<MHHQRegister, String>,JpaSpecificationExecutor<MHHQRegister>{
	
	@Query("select hq from MHHQRegister hq where hq.idCard=:idCard")
	public MHHQRegister getMHHQRegisterByIdCard(@Param("idCard") String idCard);

	
	@Query("select hq from MHHQRegister hq where hq.merchantOrder=:merchantOrder")
	public MHHQRegister getMHHQRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);

	

}
