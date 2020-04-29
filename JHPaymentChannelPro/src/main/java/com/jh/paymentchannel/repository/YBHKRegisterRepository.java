package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YBHKRegister;




@Repository
public interface YBHKRegisterRepository extends JpaRepository<YBHKRegister, String>, JpaSpecificationExecutor<YBHKRegister>{
	
	@Query("select yb from  YBHKRegister yb where yb.idCard=:idCard")
	public YBHKRegister getYBHKRegisterByIdCard(@Param("idCard") String idCard);
	
	
}
