package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankCard4AuthRoute;

@Repository
public interface BankCard4AuthRouteRepository  extends JpaRepository<BankCard4AuthRoute,String>,JpaSpecificationExecutor<BankCard4AuthRoute>{ 

	
	@Query("select bankCard4Route from  BankCard4AuthRoute bankCard4Route where bankCard4Route.activeStatus='1' ")
	public BankCard4AuthRoute	getCurActiveBankCard4Channel();
	
}
