package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YBQuickRegister;




@Repository
public interface YBQuickRegisterRepository extends JpaRepository<YBQuickRegister, String>, JpaSpecificationExecutor<YBQuickRegister>{
	
	@Query("select yb from  YBQuickRegister yb where yb.idCard=:idCard")
	public YBQuickRegister getYBQuickRegister(@Param("idCard") String idCard);
	
	
	@Query("select yb from  YBQuickRegister yb where yb.phone=:phone")
	public YBQuickRegister getYBQuickRegisterByPhone(@Param("phone") String phone);
	
}
