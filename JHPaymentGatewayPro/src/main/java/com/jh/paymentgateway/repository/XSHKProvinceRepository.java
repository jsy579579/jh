package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.XSHKProvince;

@Repository
public interface XSHKProvinceRepository extends JpaRepository<XSHKProvince, Long>,JpaSpecificationExecutor<XSHKProvince>{

	@Query("select xsHKProvince from XSHKProvince xsHKProvince group by xsHKProvince.province")
	List<XSHKProvince> findXSHKProvince();

	List<XSHKProvince> findByProvince(String province);

}
