package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.City;

@Repository
public interface CityRepository  extends JpaRepository<City,String>,JpaSpecificationExecutor<City>{

	
	
	@Query("select c.city from City c where c.provinceid=:provinceid")
	List<String>  getCityByProvinceId(@Param("provinceid")String provinceid);
	
	
}
