package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.GHTCityMerchant;





@Repository
public interface GHTCityMerchantRepository extends JpaRepository<GHTCityMerchant, String>, JpaSpecificationExecutor<GHTCityMerchant>{
	
	@Query("select ght.province from GHTCityMerchant ght group by ght.province")
	public List<String> getGHTCityMerchantProvince();
	
	@Query("select ght.city from GHTCityMerchant ght where ght.province=:province")
	public List<String> getGHTCityMerchantCityByProvince(@Param("province") String province);
	
	@Query(value = "select * from t_ght_city_merchant ght where ght.province=:province and ght.city=:city order by ght.id desc limit 300", nativeQuery = true)
	List<GHTCityMerchant> getGHTCityMerchantByProvinceAndCity(@Param("province") String province, @Param("city") String city);
	
	@Query("select ght.merchantCode from GHTCityMerchant ght where ght.province=:province and ght.city=:city")
	public List<String> getGHTCityMerchantCodeByProvinceAndCity(@Param("province") String province, @Param("city") String city);

	public GHTCityMerchant getGHTCityMerchantByMerchantCode(String merchantCode);

	public GHTCityMerchant getGHTCityMerchantByMerchantName(String merchantName);
	
}
