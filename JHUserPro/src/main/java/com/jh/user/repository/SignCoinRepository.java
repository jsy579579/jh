package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.SignCoin;

@Repository
public interface SignCoinRepository  extends JpaRepository<SignCoin, String>,JpaSpecificationExecutor<SignCoin>{

	
	@Query("select sc.bonusCoin from SignCoin sc where sc.brandId=:brandId and sc.grade=:grade and sc.continueDays=:continueDays")
	int  getSignCoin(@Param("brandId")String brandId, @Param("grade")String grade, @Param("continueDays") int continueDays);
	
	@Query("select sc from SignCoin sc where sc.brandId=:brandId and sc.grade=:grade and sc.continueDays=:continueDays")
	SignCoin  getSignCoinByBrandIdAndGradeAndContinueDays(@Param("brandId")String brandId, @Param("grade")String grade, @Param("continueDays") int continueDays);
	
	@Query("select sc from SignCoin sc where sc.brandId=:brandId and sc.grade=:grade order by sc.continueDays")
	List<SignCoin>  getSignCoinByBrandIdAndGrade(@Param("brandId")String brandId, @Param("grade")String grade);
	
	@Query("select sc from SignCoin sc where sc.brandId=:brandId and sc.id in (:id)")
	List<SignCoin>  getSignCoinByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long[] id);
	
	@Query("select sc from SignCoin sc where sc.brandId=:brandId")
	List<SignCoin>  getSignCoinByBrandId(@Param("brandId")String brandId);
	
}
