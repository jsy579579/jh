package com.jh.paymentgateway.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.RYTRegister;


public interface RYTRegisterRepository extends JpaRepository<RYTRegister, String>,JpaSpecificationExecutor<RYTRegister>{
	
	@Query("select hq from RYTRegister hq where hq.idCard=:idCard")
	public RYTRegister getRYTRegisterByIdCard(@Param("idCard") String idCard);

}
