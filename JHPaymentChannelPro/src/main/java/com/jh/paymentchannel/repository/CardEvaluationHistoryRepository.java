package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CardEvaluationHistory;





@Repository
public interface CardEvaluationHistoryRepository extends JpaRepository<CardEvaluationHistory, String>, JpaSpecificationExecutor<CardEvaluationHistory>{
	
	@Query("select ce from CardEvaluationHistory ce where ce.userId=:userId")
	public List<CardEvaluationHistory> getCardEvaluationHistoryByUserId(@Param("userId") String userId);
	
}
