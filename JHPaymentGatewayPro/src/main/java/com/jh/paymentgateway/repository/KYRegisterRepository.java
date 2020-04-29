package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KYRegister;
@Repository
public interface KYRegisterRepository extends JpaRepository<KYRegister, String>, JpaSpecificationExecutor<KYRegister> {
	@Query("select ky from KYRegister ky where ky.idCard=:idCard")
	public KYRegister getKYRegisterByIdCard(@Param("idCard") String idCard);

}
