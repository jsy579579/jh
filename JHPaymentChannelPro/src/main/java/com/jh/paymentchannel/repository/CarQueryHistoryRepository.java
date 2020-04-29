package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CarQueryHistory;
import com.jh.paymentchannel.pojo.City;

@Repository
public interface CarQueryHistoryRepository  extends JpaRepository<CarQueryHistory,String>,JpaSpecificationExecutor<CarQueryHistory>{

	@Query("select c from CarQueryHistory c where c.userId=:userId")
	List<CarQueryHistory>  getCarQueryHistoryByUserId(@Param("userId")String userId);
	
	@Query("select c from CarQueryHistory c where c.userId=:userId and c.id=:id")
	CarQueryHistory  getCarQueryHistoryByUserIdAndId(@Param("userId")String userId, @Param("id")long id);
	
}
