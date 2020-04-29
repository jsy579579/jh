package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.GHTXwkCityMerchant;





@Repository
public interface GHTXwkCityMerchantRepository extends JpaRepository<GHTXwkCityMerchant, String>, JpaSpecificationExecutor<GHTXwkCityMerchant>{
	
	@Query(value = "select * from t_ghtxwk_city_merchant ght where ght.province=:province and ght.city=:city order by ght.id desc limit 300", nativeQuery = true)
	List<GHTXwkCityMerchant> getGHTXwkCityMerchantByProvinceAndCity(@Param("province") String province, @Param("city") String city);
	
	@Query("select ght.merchantCode from GHTXwkCityMerchant ght where ght.province=:province and ght.city=:city")
	public List<String> getGHTXwkCityMerchantCodeByProvinceAndCity(@Param("province") String province, @Param("city") String city);

	GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantCode(String merchantCode);

	GHTXwkCityMerchant getGHTXwkCityMerchantByMerchantName(String merchantName);
	
}
