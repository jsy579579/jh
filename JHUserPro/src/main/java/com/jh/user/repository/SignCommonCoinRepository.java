package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.SignCommonCoin;

@Repository
public interface SignCommonCoinRepository  extends JpaRepository<SignCommonCoin, Long>,JpaSpecificationExecutor<SignCommonCoin>{

	
	@Query("select sc from SignCommonCoin sc where sc.brandId=:brandId and sc.grade=:grade")
	SignCommonCoin  getSignCommonCoinByBrandIdAndGrade(@Param("brandId")String brandId, @Param("grade")String grade);
	
	@Query("select sc from SignCommonCoin sc where sc.brandId=:brandId and sc.grade=:grade and sc.bonusCoin=:bonusCoin")
	SignCommonCoin  getSignCommonCoinByBrandIdAndGradeAndCoin(@Param("brandId")String brandId, @Param("grade")String grade, @Param("bonusCoin")String bonusCoin);
	
	@Query("select sc from SignCommonCoin sc where sc.brandId=:brandId")
	List<SignCommonCoin>  getSignCommonCoinByBrandId(@Param("brandId")String brandId);

	@Query("select sc from SignCoin sc where sc.brandId=:brandId and sc.id in (:id)")
	List<SignCommonCoin>  getSignCommonCoinByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long[] id);
	
}
