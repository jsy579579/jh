package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandRebateRatio;

@Repository
public interface BrandRebateRatioRepository  extends JpaRepository<BrandRebateRatio,String>,JpaSpecificationExecutor<BrandRebateRatio>{

	List<BrandRebateRatio> getBrandRebateRatioByBrandId(int brandId);

	BrandRebateRatio getBrandRebateRatioByBrandIdAndGrade(int brandId, int grade);

	BrandRebateRatio getBrandRebateRatioByBrandIdAndId(int brandId, long id);
	
	@Query("select n from BrandRebateRatio n where n.brandId=:brandId and n.id in (:id)")
	List<BrandRebateRatio>  getBrandRebateRatioByBrandIdAndId(@Param("brandId")int brandId, @Param("id")long[] id);
	
}
