package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KBRegister;

@Repository
public interface KBRegisterRepository extends JpaRepository<KBRegister, String>, JpaSpecificationExecutor<KBRegister>{
	
	@Query("select kb from KBRegister kb where kb.idCard=:idCard")
	public KBRegister getKBRegisterByIdCard(@Param("idCard") String idCard);
	
}
