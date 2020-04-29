package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BQXRegister;

@Repository
public interface BQXRegisterRepository extends JpaRepository<BQXRegister, String>, JpaSpecificationExecutor<BQXRegister> {
	@Query("select bqx from BQXRegister bqx where bqx.idCard=:idCard")
	public BQXRegister getBQXRegisterByIdCard(@Param("idCard") String idCard);
}
