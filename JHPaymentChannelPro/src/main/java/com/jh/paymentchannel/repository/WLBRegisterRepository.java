package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WLBRegister;


@Repository
public interface WLBRegisterRepository extends JpaRepository<WLBRegister, String>, JpaSpecificationExecutor<WLBRegister>{
	
	@Query("select wlb from WLBRegister wlb where wlb.idCard=:idCard")
	public WLBRegister getWLBRegisterByIdCard(@Param("idCard") String idCard);
	
	@Query("select wlb.bankCard from WLBRegister wlb where wlb.phone=:phone")
	public String getWLBRegisterByPhone(@Param("phone") String phone);
	
}
