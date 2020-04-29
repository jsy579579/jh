package com.jh.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.SpecialManageMent;

@Repository
public interface SpecialManageMentRepository  extends JpaRepository<SpecialManageMent,String>,JpaSpecificationExecutor<SpecialManageMent>{

	
	@Query("select sm from SpecialManageMent sm where sm.brandId=:brandId")
	public SpecialManageMent  getSpecialManageMentByBrandId(@Param("brandId")String brandId);
	
	
}
