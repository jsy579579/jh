package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKNewCityMerchant;





@Repository
public interface WMYKNewCityMerchantRepository extends JpaRepository<WMYKNewCityMerchant, String>, JpaSpecificationExecutor<WMYKNewCityMerchant>{
	
	@Query("select wmyk from WMYKNewCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<WMYKNewCityMerchant> getWMYKNewCityMerchantByCityCode(@Param("cityCode") String cityCode);
	
	@Query("select wmyk.merchantCode from WMYKNewCityMerchant wmyk where wmyk.cityCode=:cityCode")
	public List<String> getWMYKNewCityMerchantCodeByCityCode(@Param("cityCode") String cityCode);
	
}
