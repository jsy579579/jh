package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ThirdLevelRebateRatioNew;

@Repository
public interface ThirdLevelRebateRatioNewRepository extends JpaRepository<ThirdLevelRebateRatioNew,String>,JpaSpecificationExecutor<ThirdLevelRebateRatioNew>{

	List<ThirdLevelRebateRatioNew> findByBrandIdAndThirdLevelId(long brandid, Integer thirdLevelId);

	ThirdLevelRebateRatioNew findByBrandIdAndPreLevelAndThirdLevelId(Long brandid,String preLevel, Integer thirdLevelId);

	List<ThirdLevelRebateRatioNew> findByBrandId(Long brandid);

	ThirdLevelRebateRatioNew findById(Long id);
	
	@Modifying
	@Query("delete from ThirdLevelRebateRatioNew thirdLevelRebateRatioNew where thirdLevelRebateRatioNew.preLevel = :grade and thirdLevelRebateRatioNew.brandId = :brandId")
	void deleteThirdLevelRebateByBrandIdAndGrade1(@Param("grade")String grade, @Param("brandId")Long brandId);

	@Modifying
	@Query("delete from ThirdLevelRebateRatioNew thirdLevelRebateRatioNew where thirdLevelRebateRatioNew.thirdLevelId in (select thirdlevel.id from ThirdLevelDistribution thirdlevel where thirdlevel.grade = :grade and thirdlevel.brandId = :brandId)")
	void deleteThirdLevelRebateByBrandIdAndGrade2(@Param("grade")Integer grade, @Param("brandId")Long brandId);

}
