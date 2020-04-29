package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CardEvaluation;





@Repository
public interface CardEvaluationRepository extends JpaRepository<CardEvaluation, String>, JpaSpecificationExecutor<CardEvaluation>{
	
	@Query("select ce from CardEvaluation ce where ce.bankCard=:bankCard")
	public CardEvaluation getCardEvaluationByBankCard(@Param("bankCard") String bankCard);
	
}
