package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.FFZCRegister;

@Repository
public interface FFZCRegisterRepository extends JpaRepository<FFZCRegister, String>, JpaSpecificationExecutor<FFZCRegister> {

	@Query("select ffzc from FFZCRegister ffzc where ffzc.idCard=:idCard")
	public FFZCRegister getFFZCRegisterByIdCard(@Param("idCard") String idCard);

}
