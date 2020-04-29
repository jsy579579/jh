package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKNewCity;





@Repository
public interface WMYKNewCityRepository extends JpaRepository<WMYKNewCity, String>, JpaSpecificationExecutor<WMYKNewCity>{
	
	@Query("select wmyk from WMYKNewCity wmyk where wmyk.provinceCode=:provinceCode")
	public List<WMYKNewCity> getWMYKNewCityByProvinceCode(@Param("provinceCode") String provinceCode);
	
	@Query("select wmyk from WMYKNewCity wmyk")
	public List<WMYKNewCity> getWMYKNewCity();
	
}
