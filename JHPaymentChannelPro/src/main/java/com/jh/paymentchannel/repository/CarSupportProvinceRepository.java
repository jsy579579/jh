package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CarSupportProvince;

@Repository
public interface CarSupportProvinceRepository  extends JpaRepository<CarSupportProvince,String>,JpaSpecificationExecutor<CarSupportProvince>{

	
	
	@Query("select c from CarSupportProvince c")
	List<CarSupportProvince>  getCarSupportProvince();
	
	@Query("select c from CarSupportProvince c where c.province=:province")
	CarSupportProvince  getCarSupportProvinceByProvince(@Param("province")String province);
	
	
}
