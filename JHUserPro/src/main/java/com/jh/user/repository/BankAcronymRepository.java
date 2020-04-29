package com.jh.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BankAcronym;

@Repository
public interface BankAcronymRepository extends JpaRepository<BankAcronym,String>,JpaSpecificationExecutor<BankAcronym>{

	//bankName
	@Query("select bankAcronym from  BankAcronym bankAcronym where bankAcronym.bankName=:bankName")
	BankAcronym queryBankNumberByBankName(@Param("bankName") String bankName);
	
}
