package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.XJQuickRegister;



@Repository
public interface XJQuickRegisterRepository extends JpaRepository<XJQuickRegister, String>, JpaSpecificationExecutor<XJQuickRegister>{
	
	@Query("select xj from  XJQuickRegister xj where xj.idCard=:idCard")
	public XJQuickRegister getXJQuickRegister(@Param("idCard") String idCard);
	
	@Query("select xj.bankCard from XJQuickRegister xj where xj.phone=:phone")
	public String getXJRegisterByPhone(@Param("phone") String phone);
	
}
