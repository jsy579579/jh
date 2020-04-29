package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKCity;





@Repository
public interface WMYKCityRepository extends JpaRepository<WMYKCity, String>, JpaSpecificationExecutor<WMYKCity>{
	
	@Query("select wmyk from WMYKCity wmyk where wmyk.provinceCode=:provinceCode")
	public List<WMYKCity> getWMYKCityByProvinceCode(@Param("provinceCode") String provinceCode);
	
	@Query("select wmyk from WMYKCity wmyk")
	public List<WMYKCity> getWMYKCity();
	
}
