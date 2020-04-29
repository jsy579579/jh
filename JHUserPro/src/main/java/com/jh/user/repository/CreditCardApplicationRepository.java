package com.jh.user.repository;

import com.jh.user.pojo.CreditCardApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditCardApplicationRepository extends JpaRepository<CreditCardApplication,String>,JpaSpecificationExecutor<CreditCardApplication> {

	List<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatus(int brandId, int status);
	
	CreditCardApplication getCreditCardApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status);
	
	@Query("select la from CreditCardApplication la where la.brandId=:brandId and la.id in (:id) and la.status=:status")
	List<CreditCardApplication>  getCreditCardApplicationByBrandIdAndIdsAndStatus(@Param("brandId")int brandId, @Param("id")long[] id, @Param("status")int status);
	
	@Query("select la from  CreditCardApplication la where la.brandId=:brandId and la.status=:status")
	Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatusAndPage(@Param("brandId")int brandId, @Param("status")int status, Pageable pageAble);

	@Query("select la from CreditCardApplication la where la.brandId=:brandId and la.title like %:title% and la.status=:status")
	Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndTitleAndStatusAndPage(@Param("brandId")int brandId, @Param("title")String title, @Param("status")int status, Pageable pageable);

}
