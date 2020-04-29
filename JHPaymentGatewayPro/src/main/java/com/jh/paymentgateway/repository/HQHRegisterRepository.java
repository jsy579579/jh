package com.jh.paymentgateway.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.HQHRegister;


public interface HQHRegisterRepository extends JpaRepository<HQHRegister, String>,JpaSpecificationExecutor<HQHRegister>{
	
	@Query("select hqh from HQHRegister hqh where hqh.idCard=:idCard")
	public HQHRegister getHQHRegisterByIdCard(@Param("idCard") String idCard);

	@Query("select hqh from HQHRegister hqh where hqh.merchantOrder=:merchantOrder")
	public HQHRegister getHQGRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);

}
