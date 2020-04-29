package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ThirdLevelRebateRatio;

@Repository
public interface ThirdLevelRebateRatioRepository extends JpaRepository<ThirdLevelRebateRatio,String>,JpaSpecificationExecutor<ThirdLevelRebateRatio>{

	
	@Query("select thirdlevelratio from  ThirdLevelRebateRatio thirdlevelratio where thirdlevelratio.brandId = :brandid")
	List<ThirdLevelRebateRatio> findAllThirdLevelRatio(@Param("brandid") long brandid);
	
	
	@Query("select thirdlevelratio from  ThirdLevelRebateRatio thirdlevelratio where thirdlevelratio.brandId = :brandid and thirdlevelratio.preLevel = :prelevel")
	ThirdLevelRebateRatio findAllThirdLevelRatioBybrandidAndlevel(@Param("brandid") long brandid, @Param("prelevel") String preLevel);

}
