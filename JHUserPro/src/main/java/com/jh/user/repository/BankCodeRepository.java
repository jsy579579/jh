package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BankCode;

@Repository
public interface BankCodeRepository extends JpaRepository<BankCode, String>, JpaSpecificationExecutor<BankCode>{
	
	@Query(" select bc.code from BankCode bc where bc.name = :name")
	public String getCodeByName(@Param("name") String name);
	
	@Query(" select bc from BankCode bc where bc.name = :name")
	public BankCode getBankCode(@Param("name") String name);
	
}
