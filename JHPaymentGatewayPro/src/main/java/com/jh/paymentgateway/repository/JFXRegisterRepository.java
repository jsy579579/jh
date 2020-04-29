package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.JFXRegister;

@Repository
public interface JFXRegisterRepository extends JpaRepository<JFXRegister, String>, JpaSpecificationExecutor<JFXRegister> {
	
	@Query("select jfx from JFXRegister jfx where jfx.idCard=:idCard")
	public JFXRegister getJFXRegisterByIdCard(@Param("idCard") String idCard);

}
