package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankCard4Auth;

@Repository
public interface BankCard4AuthRepository  extends JpaRepository<BankCard4Auth,String>,JpaSpecificationExecutor<BankCard4Auth>{

	@Query("select bankCard4 from  BankCard4Auth bankCard4 where bankCard4.mobile=:mobile ")
	BankCard4Auth findBankCard4AuthByMobile(@Param("mobile") String mobile);
	
	
	@Query("select bankCard4 from  BankCard4Auth bankCard4 where bankCard4.bankcard=:bankcard ")
	BankCard4Auth findBankCard4AuthByCard(@Param("bankcard") String bankcard);
	
	
	@Query("select bankCard4 from  BankCard4Auth bankCard4 where bankCard4.resCode=1 ")
	List<BankCard4Auth> findBankCard4AuthSuccess(Pageable page);
	
}
