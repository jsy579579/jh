package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.HQDHRegister;


public interface HQDHRegisterRepository extends JpaRepository<HQDHRegister, String>,JpaSpecificationExecutor<HQDHRegister>{
	
	@Query("select hq from HQDHRegister hq where hq.idCard=:idCard")
	public HQDHRegister getHQDHRegisterrByIdCard(@Param("idCard") String idCard);

	
	/*@Query("select hq from HQRegister hq where hq.merchantOrder=:merchantOrder")
	public HQRegister getHQRegisterByMerchantOrder(@Param("merchantOrder") String merchantOrder);*/

	

}
