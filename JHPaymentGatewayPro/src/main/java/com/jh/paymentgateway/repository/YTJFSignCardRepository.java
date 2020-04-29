package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.LDRegister;
import com.jh.paymentgateway.pojo.YTJFSignCard;

@Repository
public interface YTJFSignCardRepository extends JpaRepository<YTJFSignCard, String>, JpaSpecificationExecutor<YTJFSignCard>{
	
	
	@Query("from YTJFSignCard ld where ld.idCard=:idCard")
	public YTJFSignCard getYTJFSignCardByIdCard(@Param("idCard") String idCard);

	@Query("from YTJFSignCard ld where ld.bankCard=:bankCard")
	public YTJFSignCard getYTJFSignCardByBankCard(@Param("bankCard") String bankCard);
	
}
