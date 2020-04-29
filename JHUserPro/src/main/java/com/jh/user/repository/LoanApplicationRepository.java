package com.jh.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.LoanApplication;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication,String>,JpaSpecificationExecutor<LoanApplication> {

	List<LoanApplication> getLoanApplicationByBrandIdAndStatus(int brandId, int status);
	
	LoanApplication getLoanApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status);
	
	@Query("select la from LoanApplication la where la.brandId=:brandId and la.id in (:id) and la.status=:status")
	List<LoanApplication>  getLoanApplicationByBrandIdAndIdsAndStatus(@Param("brandId")int brandId, @Param("id")long[] id, @Param("status")int status);
	
	@Query("select la from  LoanApplication la where la.brandId=:brandId and la.status=:status")
	Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndPage(@Param("brandId")int brandId, @Param("status")int status, Pageable pageAble);

	@Query("select la from LoanApplication la where la.brandId=:brandId and la.status=:status and la.title like %:title%")
	Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndTitleAndPage(@Param("brandId")int brandId, @Param("status")int status, @Param("title")String title, Pageable pageable);

}
