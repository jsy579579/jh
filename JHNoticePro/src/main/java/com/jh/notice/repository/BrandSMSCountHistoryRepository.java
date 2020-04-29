package com.jh.notice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.BrandSMSCountHistory;

@Repository
public interface BrandSMSCountHistoryRepository extends  PagingAndSortingRepository<BrandSMSCountHistory, String>{

	@Query("select BrandSMSCountHistory from  BrandSMSCountHistory BrandSMSCountHistory where BrandSMSCountHistory.brandId=:brandId ")
	BrandSMSCountHistory findBrandSMSCountHistory(@Param("brandId") String brandId);
	
}
