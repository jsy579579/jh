package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BQRegister;

@Repository
public interface BQRegisterRepository extends JpaRepository<BQRegister, Long>,JpaSpecificationExecutor<BQRegister>{
	
	@Query("select bq from BQRegister bq where bq.idNum=:idNum")
	public BQRegister getBQRegisterByIdCard(@Param("idNum") String idNum);//changeOldBQRegister
	
}
