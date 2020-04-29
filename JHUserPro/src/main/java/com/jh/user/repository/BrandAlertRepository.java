package com.jh.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandAlert;

@Repository
public interface BrandAlertRepository  extends JpaRepository<BrandAlert,String>,JpaSpecificationExecutor<BrandAlert>{

	@Query("select ba from BrandAlert ba where ba.brandId=:brandId and ba.btype=:type")
	BrandAlert getBrandAlertByBrandIdAndType(@Param("brandId")String brandId, @Param("type")String type);

}
