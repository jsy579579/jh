package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQGProvinceCity;





@Repository
public interface HQGProvinceCityRepository extends JpaRepository<HQGProvinceCity, String>, JpaSpecificationExecutor<HQGProvinceCity>{
	
	@Query("select hqg from HQGProvinceCity hqg GROUP BY hqg.hkProvinceCode")
	public List<HQGProvinceCity> getHQGProvinceCityByHkProvinceCode();
	
	@Query("select hqg from HQGProvinceCity hqg where hqg.cityCode like :cityCode% GROUP BY hqg.hkCityCode")
	public List<HQGProvinceCity> getHQGProvinceCityGroupByCity(@Param("cityCode") String cityCode);
}
