package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.HLJCRegister;





@Repository
public interface HLJCRegisterRepository extends JpaRepository<HLJCRegister, String>, JpaSpecificationExecutor<HLJCRegister>{
	
	@Query("select hljc from HLJCRegister hljc where hljc.bankCard=:bankCard")
	public HLJCRegister getHLJCRegisterByBankCard(@Param("bankCard") String bankCard);
	
}
