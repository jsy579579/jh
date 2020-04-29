package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFRegister;

@Repository
public interface JFRegisterRepository extends JpaRepository<JFRegister, String>, JpaSpecificationExecutor<JFRegister> {
	@Query("select jf from JFRegister jf where jf.idCard=:idCard")
	public JFRegister getJFRegisterByIdCard(@Param("idCard") String idCard);

}
