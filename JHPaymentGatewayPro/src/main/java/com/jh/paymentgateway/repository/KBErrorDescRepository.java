package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KBErrorDesc;

@Repository
public interface KBErrorDescRepository extends JpaRepository<KBErrorDesc, String>, JpaSpecificationExecutor<KBErrorDesc>{
	
	@Query("select kb.errorMsg from KBErrorDesc kb where kb.errorCode=:errorCode")
	public String getKBErrorDescByErrorCode(@Param("errorCode") String errorCode);
	
}
