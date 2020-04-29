package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQQuickRegister;
import com.jh.paymentgateway.pojo.WFRegister;





@Repository
public interface HQQuickRegisterRepository extends JpaRepository<HQQuickRegister, String>, JpaSpecificationExecutor<HQQuickRegister>{
	
	@Query("select hq from HQQuickRegister hq where hq.idCard=:idCard")
	public HQQuickRegister getHQQuickRegisterByIdCard(@Param("idCard") String idCard);
	
}
