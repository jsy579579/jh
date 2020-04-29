package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHGHTXwkCityMerchant;


@Repository
public interface MHGHTXwkCityMerchantRepository extends JpaRepository<MHGHTXwkCityMerchant, String>, JpaSpecificationExecutor<MHGHTXwkCityMerchant>{
	
	@Query(value = "select * from t_mh_ghtxwk_city_merchant ght where ght.province=:province and ght.city=:city order by ght.id desc limit 300", nativeQuery = true)
	List<MHGHTXwkCityMerchant> getMHGHTXwkCityMerchantByProvinceAndCity(@Param("province") String province, @Param("city") String city);
	
	@Query("select ght.merchantCode from MHGHTXwkCityMerchant ght where ght.province=:province and ght.city=:city")
	public List<String> getMHGHTXwkCityMerchantCodeByProvinceAndCity(@Param("province") String province, @Param("city") String city);

	MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantCode(String merchantCode);

	MHGHTXwkCityMerchant getMHGHTXwkCityMerchantByMerchantName(String merchantName);
	
}
