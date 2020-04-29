package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.WFRegister;





@Repository
public interface WFRegisterRepository extends JpaRepository<WFRegister, String>, JpaSpecificationExecutor<WFRegister>{
	
	@Query("select wf from WFRegister wf where wf.idCard=:idCard")
	public WFRegister getWFRegisterByIdCard(@Param("idCard") String idCard);
	
}
