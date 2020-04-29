package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.KYRegister;





@Repository
public interface KYRegisterRepository extends JpaRepository<KYRegister, String>, JpaSpecificationExecutor<KYRegister>{
	
	@Query("select cj from KYRegister cj where cj.idCard=:idCard")
	public KYRegister getKYRegisterByIdCard(@Param("idCard") String idCard);
	
}
