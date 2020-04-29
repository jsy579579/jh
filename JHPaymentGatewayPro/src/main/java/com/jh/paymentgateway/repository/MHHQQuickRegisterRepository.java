package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQQuickRegister;





@Repository
public interface MHHQQuickRegisterRepository extends JpaRepository<MHHQQuickRegister, String>, JpaSpecificationExecutor<MHHQQuickRegister>{
	
	@Query("select hq from MHHQQuickRegister hq where hq.idCard=:idCard")
	public MHHQQuickRegister getMHHQQuickRegisterByIdCard(@Param("idCard") String idCard);
	
}
