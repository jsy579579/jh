package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandAutoUpdateConfig;

@Repository
public interface BrandAutoUpgradeConfigRepository extends JpaRepository<BrandAutoUpdateConfig,String>,JpaSpecificationExecutor<BrandAutoUpdateConfig>{

	
	@Query("select brandAuto from  BrandAutoUpdateConfig brandAuto where brandAuto.brandId=:brandid and brandAuto.grade=:grade and brandAuto.status='0'")
	BrandAutoUpdateConfig findBrandAutoUpgradeConfigBybrandidAndgrade(@Param("brandid") long brandid,@Param("grade") long grade);
	
	@Query("select brandAuto from  BrandAutoUpdateConfig brandAuto where brandAuto.brandId=:brandid and brandAuto.grade=:grade")
	BrandAutoUpdateConfig findBrandAutoUpgradeConfigBybrandidAndgradeNostatus(@Param("brandid") long brandid,@Param("grade") long grade);
	
	@Query("select brandAuto from  BrandAutoUpdateConfig brandAuto where brandAuto.brandId=:brandid ")
	List<BrandAutoUpdateConfig> findBrandAutoUpgradeConfigBybrandidNostutas(@Param("brandid") long brandid);
	
	@Query("select brandAuto from  BrandAutoUpdateConfig brandAuto where brandAuto.brandId=:brandid and brandAuto.status='0'")
	List<BrandAutoUpdateConfig> findBrandAutoUpgradeConfigBybrandid(@Param("brandid") long brandid);
	
	
	@Query("select brandAuto from  BrandAutoUpdateConfig brandAuto where brandAuto.status='0'")
	List<BrandAutoUpdateConfig> findBrandAutoUpgradeConfig();
	
	
	
}
