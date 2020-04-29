package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKProvince;





@Repository
public interface WMYKProvinceRepository extends JpaRepository<WMYKProvince, String>, JpaSpecificationExecutor<WMYKProvince>{
	
	@Query("select wmyk from WMYKProvince wmyk")
	public List<WMYKProvince> getWMYKProvince();
	
}
