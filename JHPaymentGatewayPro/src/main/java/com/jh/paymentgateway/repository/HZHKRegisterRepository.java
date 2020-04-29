package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJHKRegister;
import com.jh.paymentgateway.pojo.CJQuickBindCard;
import com.jh.paymentgateway.pojo.HZHKBindCard;
import com.jh.paymentgateway.pojo.HZHKRegister;

@Repository
public interface HZHKRegisterRepository extends JpaRepository<HZHKRegister, String>,JpaSpecificationExecutor<HZHKRegister>{
   
	@Query("select hzhk from HZHKRegister hzhk where hzhk.idCard=:idCard")
	public HZHKRegister getHZHKRegisterByidCard(@Param("idCard") String idCard);
	
	
}
