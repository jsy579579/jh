package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKXWKCityMerchant;





@Repository
public interface WMYKXWKCityMerchantRepository extends JpaRepository<WMYKXWKCityMerchant, String>, JpaSpecificationExecutor<WMYKXWKCityMerchant>{
	
	@Query("select wmyk from WMYKXWKCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<WMYKXWKCityMerchant> getWMYKXWKCityMerchantByCityCode(@Param("cityCode") String cityCode);
	
	@Query("select wmyk.merchantCode from WMYKXWKCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<String> getWMYKXWKCityMerchantCodeByCityCode(@Param("cityCode") String cityCode);
	
	@Query("select wmyk from WMYKXWKCityMerchant wmyk where wmyk.merchantName=:merchantName")
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantName(@Param("merchantName") String merchantName);
	
	@Query("select wmyk from WMYKXWKCityMerchant wmyk where wmyk.merchantCode=:merchantCode")
	public WMYKXWKCityMerchant getWMYKXWKCityMerchantByMerchantCode(@Param("merchantCode") String merchantCode);
	
}
