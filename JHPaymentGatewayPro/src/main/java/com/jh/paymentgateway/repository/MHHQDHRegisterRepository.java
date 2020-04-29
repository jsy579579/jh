package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.MHHQDHRegister;


public interface MHHQDHRegisterRepository extends JpaRepository<MHHQDHRegister, String>,JpaSpecificationExecutor<MHHQDHRegister>{
	
	@Query("select hq from MHHQDHRegister hq where hq.idCard=:idCard")
	public MHHQDHRegister getMHHQDHRegisterrByIdCard(@Param("idCard") String idCard);

	
}
