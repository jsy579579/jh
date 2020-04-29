package com.jh.paymentgateway.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.MHHQGRegister;


public interface MHHQGRegisterRepository extends JpaRepository<MHHQGRegister, String>,JpaSpecificationExecutor<MHHQGRegister>{
	
	@Query("select hqg from MHHQGRegister hqg where hqg.idCard=:idCard")
	public MHHQGRegister getMHHQGRegisterByIdCard(@Param("idCard") String idCard);

	@Query("select hqg from MHHQRegister hqg where hqg.merchantOrder=:merchantOrder")
	public MHHQGRegister getMHHQGRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);

}
