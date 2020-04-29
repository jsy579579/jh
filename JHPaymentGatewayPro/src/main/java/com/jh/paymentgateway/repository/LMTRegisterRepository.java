package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.LMTRegister;

@Repository
public interface LMTRegisterRepository extends JpaRepository<LMTRegister, String>, JpaSpecificationExecutor<LMTRegister> {
	
	@Query("select lmt from LMTRegister lmt where lmt.idCard=:idCard")
	public LMTRegister getLMTRegisterByIdCard(@Param("idCard") String idCard);

}
