package com.jh.paymentgateway.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.HQGRegister;


public interface HQGRegisterRepository extends JpaRepository<HQGRegister, String>,JpaSpecificationExecutor<HQGRegister>{
	
	@Query("select hqg from HQGRegister hqg where hqg.idCard=:idCard")
	public HQGRegister getHQGRegisterByIdCard(@Param("idCard") String idCard);

	@Query("select hqg from HQRegister hqg where hqg.merchantOrder=:merchantOrder")
	public HQGRegister getHQGRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);

}
