package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.YFJRBinkCard;

@Repository
public interface YFJRBinkCardRepository extends JpaRepository<YFJRBinkCard, Long>,JpaSpecificationExecutor<YFJRBinkCard>{

	@Query("select yf from YFJRBinkCard yf where yf.idCard=:idCard")
	public YFJRBinkCard getYFJRBinkCardByIdNum(@Param("idCard") String idCard);
	
	@Query("select yf from YFJRBinkCard yf where yf.idCard=:idCard and yf.bankCard=:bankCard  and status=:status")
	public YFJRBinkCard getYFJRBinkCardByIdNum(@Param("idCard") String idCard,@Param("bankCard") String bankCard,@Param("status") String status);
	
	@Query("select yf from YFJRBinkCard yf where yf.appOrderId=:appOrderId")
	public YFJRBinkCard getYFJRBinkCardByAppOrderId(@Param("appOrderId") String appOrderId);
	
}
