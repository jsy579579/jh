package com.jh.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BankNumber;

@Repository
public interface BankNumberRepository extends JpaRepository<BankNumber,String>,JpaSpecificationExecutor<BankNumber>{

	//bankName
	@Query("select bankNumber from  BankNumber bankNumber where bankNumber.bankName like %:bankName%")
	BankNumber queryBankNumberByBankName(@Param("bankName") String bankName);
	
}
