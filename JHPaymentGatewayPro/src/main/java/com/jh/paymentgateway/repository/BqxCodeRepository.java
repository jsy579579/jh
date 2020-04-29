package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BQBankCard;
import com.jh.paymentgateway.pojo.BqxCode;

@Repository
public interface BqxCodeRepository extends JpaRepository<BqxCode, Long>,JpaSpecificationExecutor<BqxCode>{
	
	@Query("select bq from BqxCode bq where bq.provinceId=000000 and bq.grade=1")
	public List<BqxCode> getbqxCodeRepository();
	
	@Query("select bq from BqxCode bq where bq.provinceId=:provinceId")
	public List<BqxCode> findBqxCodeCity(@Param("provinceId") String provinceId);
	
}
