package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandAutoUpgrade;

@Repository
public interface BrandAutoUpgradeRepository extends JpaRepository<BrandAutoUpgrade,String>,JpaSpecificationExecutor<BrandAutoUpgrade>{

	
	@Query("select brandAuto from  BrandAutoUpgrade brandAuto where brandAuto.brandId=:brandid")
	List<BrandAutoUpgrade> findBrandAutoUpgradeBybrandid(@Param("brandid") long brandid);
	
	@Query("select brandAuto from  BrandAutoUpgrade brandAuto where brandAuto.brandId=:brandid and brandAuto.channelId=:channelId")
	BrandAutoUpgrade findBrandAutoUpgradeBybrandidAndchannelId(@Param("brandid") long brandid,@Param("channelId") long channelId);
	
}
