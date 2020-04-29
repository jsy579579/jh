package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.Province;

@Repository
public interface ProvinceRepository  extends JpaRepository<Province,String>,JpaSpecificationExecutor<Province>{

	
	
	@Query("select c.province from Province c")
	List<String>  getProvince();
	
	@Query("select c from Province c where c.province=:province")
	Province  getProvinceByProvince(@Param("province")String province);
	
	
}
