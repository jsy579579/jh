package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ThirdLevelRebateRatioNew;
import com.jh.user.pojo.ThirdLevelRebateRatioNew2;

@Repository
public interface ThirdLevelRebateRatioNew2Repository extends JpaRepository<ThirdLevelRebateRatioNew2,String>,JpaSpecificationExecutor<ThirdLevelRebateRatioNew2>{

	List<ThirdLevelRebateRatioNew2> findByBrandIdAndThirdLevelId(long brandid, Integer thirdLevelId);

	ThirdLevelRebateRatioNew2 findByBrandIdAndPreLevelAndThirdLevelId(Long brandid,String preLevel, Integer thirdLevelId);

	List<ThirdLevelRebateRatioNew2> findByBrandId(Long brandid);

	ThirdLevelRebateRatioNew2 findById(Long id);
	
	@Modifying
	@Query("delete from ThirdLevelRebateRatioNew2 thirdLevelRebateRatioNew where thirdLevelRebateRatioNew.preLevel = :grade and thirdLevelRebateRatioNew.brandId = :brandId")
	void deleteThirdLevelRebate2ByBrandIdAndGrade1(@Param("grade")String grade, @Param("brandId")Long brandId);

	@Modifying
	@Query("delete from ThirdLevelRebateRatioNew2 thirdLevelRebateRatioNew where thirdLevelRebateRatioNew.thirdLevelId in (select thirdlevel.id from ThirdLevelDistribution thirdlevel where thirdlevel.grade = :grade and thirdlevel.brandId = :brandId)")
	void deleteThirdLevelRebate2ByBrandIdAndGrade2(@Param("grade")Integer grade, @Param("brandId")Long brandId);

}
