package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKNewProvince;





@Repository
public interface WMYKNewProvinceRepository extends JpaRepository<WMYKNewProvince, String>, JpaSpecificationExecutor<WMYKNewProvince>{
	
	@Query("select wmyk from WMYKNewProvince wmyk")
	public List<WMYKNewProvince> getWMYKNewProvince();
	
}
