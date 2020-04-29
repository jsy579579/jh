package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YHQuickRegister;


@Repository
public interface YHRegisterRepository extends JpaRepository<YHQuickRegister, String>, JpaSpecificationExecutor<YHQuickRegister>{
	
	@Query("select yh from YHQuickRegister yh where yh.idCard=:idCard")
	public YHQuickRegister getYHQuickRegisterByIdCard(@Param("idCard") String idCard);
	
	@Query("select yh.bankCard from YHQuickRegister yh where yh.phone=:phone")
	public String getYHRegisterByPhone(@Param("phone") String phone);
	
}
