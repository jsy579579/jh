package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.BrandResource;
import com.jh.user.pojo.Resource;

@Repository
public interface BrandResourceRepository extends JpaRepository<BrandResource,String>,JpaSpecificationExecutor<BrandResource>{

	
	@Query(value = "select resource.* from  t_brand_resource brandResource, t_resource resource  where brandResource.resource_id=resource.id and brandResource.brand_id=:brand_id and brandResource.status='0'", nativeQuery = true)
	List<Resource> findBrandResourceBybrandid(@Param("brand_id") long brandid);
	
	
	/**删除一个资源*/
	@Modifying
	@Query("update BrandResource set  status='1' where  brandid=:brand_id and resourceid=:resource_id")
	void delResourceByBrandidAndResource(@Param("brand_id") long brandid, @Param("resource_id") long resourceid);
}
