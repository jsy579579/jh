package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.LMRegister;

@Repository
public interface LMRegisterRepository extends JpaRepository<LMRegister, String>, JpaSpecificationExecutor<LMRegister> {
	
	@Query("select lm from LMRegister lm where lm.idCard=:idCard")
	public LMRegister getLMRegisterByIdCard(@Param("idCard") String idCard);

}
