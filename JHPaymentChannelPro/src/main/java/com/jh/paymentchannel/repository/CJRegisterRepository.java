package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CJRegister;





@Repository
public interface CJRegisterRepository extends JpaRepository<CJRegister, String>, JpaSpecificationExecutor<CJRegister>{
	
	@Query("select cj from CJRegister cj where cj.idCard=:idCard")
	public CJRegister getCJRegisterByIdCard(@Param("idCard") String idCard);
	
}
