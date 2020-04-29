package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.SignCalc;

@Repository
public interface SignCalcRepository  extends JpaRepository<SignCalc,String>,JpaSpecificationExecutor<SignCalc>{

	
	@Query("select sc from SignCalc sc where sc.userId=:userId")
	SignCalc getSignCalcByUserId(@Param("userId")String userId);
		
	@Query("select sc from SignCalc sc")
	List<SignCalc> getSignCalcAll();
	
	
}
