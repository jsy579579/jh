package com.jh.notice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.BrandSMSCount;

@Repository
public interface BrandSMSCountRepository extends  PagingAndSortingRepository<BrandSMSCount, String>{

	
	
	@Query("select BrandSMSCount from  BrandSMSCount BrandSMSCount where BrandSMSCount.brandId=:brandId ")
	BrandSMSCount findBrandSMSCount(@Param("brandId") String brandId);
	
}
