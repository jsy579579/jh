package com.jh.paymentgateway.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.RYTProvinceCity;


public interface RYTProvinceCityRepository extends JpaRepository<RYTProvinceCity, String>,JpaSpecificationExecutor<RYTProvinceCity>{
	
	@Query("select hq from RYTProvinceCity hq where hq.number=:number")
	public RYTProvinceCity getRYTProvinceCityByNumber(@Param("number") String number);
	
	@Query("select hq from RYTProvinceCity hq GROUP BY   hq.province")
	public List<RYTProvinceCity> getRYTProvinceCityByprovince();
	
	@Query("select hq from RYTProvinceCity hq where hq.province=:province group by hq.city")
	public List<RYTProvinceCity> getRYTProvinceCityGroupByCity(@Param("province") String province);
	

}
