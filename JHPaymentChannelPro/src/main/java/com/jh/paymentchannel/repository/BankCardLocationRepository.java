package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankCardLocation;

@Repository
public interface BankCardLocationRepository extends JpaRepository<BankCardLocation,String>,JpaSpecificationExecutor<BankCardLocation>{ 

	
	@Query("select bankCardLocation from  BankCardLocation bankCardLocation where bankCardLocation.cardid=:cardid ")
	public BankCardLocation	getCurBankCardLocation(@Param("cardid") String cardid);
	
	
}
