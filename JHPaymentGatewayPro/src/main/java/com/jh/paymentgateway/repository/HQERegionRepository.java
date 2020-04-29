package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQERegion;

@Repository
public interface HQERegionRepository extends JpaRepository<HQERegion, String>, JpaSpecificationExecutor<HQERegion> {

	@Query("select hq from HQERegion hq where hq.parentId=:parentId")
	public List<HQERegion> getHQERegionByParentId(@Param("parentId") String parentId);

	@Query("select hq from HQERegion hq where hq.regionName LIKE CONCAT('%',:name,'%')")
	public List<HQERegion> getHQERegionByParentName(@Param("name") String name);

}
