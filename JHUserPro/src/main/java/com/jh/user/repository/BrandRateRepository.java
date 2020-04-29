package com.jh.user.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandRate;


@Repository
public interface BrandRateRepository extends JpaRepository<BrandRate,String>,JpaSpecificationExecutor<BrandRate>{

	
	@Query("select brandRate from  BrandRate brandRate where brandRate.brandId=:brandid  and brandRate.channelId=:channelid ")
	BrandRate findBrandRateBybrandidAndChannelid(@Param("brandid") long brandid, @Param("channelid") long channelid);
	
	@Query("select brandRate from  BrandRate brandRate where brandRate.brandId=:brandid ")
	List<BrandRate>  findBrandRateBybrandid(@Param("brandid") long brandid);
	//lx 
	@Query("select brandRate from  BrandRate brandRate where brandRate.channelId=:channelid and brandRate.minrate < :costRate")
	List<BrandRate> findMinRateByChannelId(@Param("channelid")long channelid,@Param("costRate")BigDecimal costRate);
	
}
