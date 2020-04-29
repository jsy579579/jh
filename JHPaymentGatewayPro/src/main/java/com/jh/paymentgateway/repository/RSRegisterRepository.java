package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.RSRegister;

@Repository
public interface RSRegisterRepository extends JpaRepository<RSRegister, String>, JpaSpecificationExecutor<RSRegister> {

	@Query("select rs from RSRegister rs where rs.idCard=:idCard")
	public RSRegister getRSRegisterByIdCard(@Param("idCard") String idCard);

}
