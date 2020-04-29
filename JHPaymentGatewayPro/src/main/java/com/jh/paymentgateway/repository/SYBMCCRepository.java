package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.SYBMCC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SYBMCCRepository extends JpaRepository<SYBMCC, String>, JpaSpecificationExecutor<SYBMCC> {

	@Query("select syb from SYBMCC syb where syb.id != null")
	public List<SYBMCC> findAllMCC();

}
