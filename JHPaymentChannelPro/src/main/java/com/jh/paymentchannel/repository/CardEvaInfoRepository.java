package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CardEvaInfo;





@Repository
public interface CardEvaInfoRepository extends JpaRepository<CardEvaInfo, String>, JpaSpecificationExecutor<CardEvaInfo>{
	
	@Query("select ce from CardEvaInfo ce where ce.userId=:userId")
	public CardEvaInfo getCardEvaInfoByUserId(@Param("userId") long userId);
	
}
