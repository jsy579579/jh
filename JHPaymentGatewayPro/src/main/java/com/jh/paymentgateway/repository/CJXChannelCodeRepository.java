package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.CJXChannelCode;

@Repository
public interface CJXChannelCodeRepository extends JpaRepository<CJXChannelCode, String>, JpaSpecificationExecutor<CJXChannelCode>{
	
	@Query("select cjxcd from CJXChannelCode cjxcd where cjxcd.bankName=:bankName")
	public CJXChannelCode getCJXChannelCode(@Param("bankName") String bankName);
	
}
