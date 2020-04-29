package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.LFQuickRegister;



@Repository
public interface LFQuickRegisterRepository extends JpaRepository<LFQuickRegister, String>, JpaSpecificationExecutor<LFQuickRegister>{
	
	@Query("select lf from LFQuickRegister lf where lf.idCard=:idCard")
	public LFQuickRegister getLFQuickRegisterByIdCard(@Param("idCard") String idCard);
	
	@Query("select lf.bankCard from LFQuickRegister lf where lf.phone=:phone")
	public String getLFRegisterByPhone(@Param("phone") String phone);
	
}
