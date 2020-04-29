package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.LMTAddress;

@Repository
public interface LMTProvinceRepository extends JpaRepository<LMTAddress, String>, JpaSpecificationExecutor<LMTAddress> {
	
	@Query("select lmt from LMTAddress lmt where lmt.pid=0")
	public List<LMTAddress> findLMTProvince();

	@Query("select lmt from LMTAddress lmt where lmt.pid=:provinceId")
	public List<LMTAddress> findLMTCityByProvinceId(@Param("provinceId") String  provinceId);

	@Query("select lmt from LMTAddress lmt where lmt.pid=:cityId")
	public List<LMTAddress> findLMTCityByCityId(@Param("cityId") String cityId);

	@Query("select lmt from LMTAddress lmt where lmt.id=:id")
	public LMTAddress getLMTProvinceCode(@Param("id") Long  id);

	@Query("select lmt from LMTAddress lmt where lmt.name=:provinceOfBank")
	public LMTAddress getLMTProvinceCode(@Param("provinceOfBank") String provinceOfBank);

	@Query("select lmt from LMTAddress lmt where lmt.name=:provinceOfBank and lmt.pid=:ciCode")
	public LMTAddress getLMTProvinceCode(@Param("provinceOfBank") String provinceOfBank, @Param("ciCode") String ciCode);

}
