package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKCityMerchant;





@Repository
public interface WMYKCityMerchantRepository extends JpaRepository<WMYKCityMerchant, String>, JpaSpecificationExecutor<WMYKCityMerchant>{
	
	@Query("select wmyk.merchantName from WMYKCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<String> getWMYKCityMerchantNameByCityCode(@Param("cityCode") String cityCode);
	
	@Query("select wmyk.merchantCode from WMYKCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<String> getWMYKCityMerchantCodeByCityCode(@Param("cityCode") String cityCode);
	
}
