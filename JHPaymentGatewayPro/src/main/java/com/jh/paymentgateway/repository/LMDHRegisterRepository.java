package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.LMDHRegister;

@Repository
public interface LMDHRegisterRepository extends JpaRepository<LMDHRegister, String>, JpaSpecificationExecutor<LMDHRegister> {
	
	@Query("select lmdh from LMDHRegister lmdh where lmdh.idCard=:idCard")
	public LMDHRegister getlmdhRegisterByidCard(@Param("idCard") String idCard);

}
