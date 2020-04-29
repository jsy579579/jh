package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHGHTCityMerchant;





@Repository
public interface MHGHTCityMerchantRepository extends JpaRepository<MHGHTCityMerchant, String>, JpaSpecificationExecutor<MHGHTCityMerchant>{
	
	@Query("select ght.province from MHGHTCityMerchant ght group by ght.province")
	public List<String> getMHGHTCityMerchantProvince();
	
	@Query("select ght.city from MHGHTCityMerchant ght where ght.province=:province")
	public List<String> getMHGHTCityMerchantCityByProvince(@Param("province") String province);
	
	@Query(value = "select * from t_mh_ght_city_merchant ght where ght.province=:province and ght.city=:city order by ght.id desc limit 300", nativeQuery = true)
	List<MHGHTCityMerchant> getMHGHTCityMerchantByProvinceAndCity(@Param("province") String province, @Param("city") String city);
	
	@Query("select ght.merchantCode from MHGHTCityMerchant ght where ght.province=:province and ght.city=:city")
	public List<String> getMHGHTCityMerchantCodeByProvinceAndCity(@Param("province") String province, @Param("city") String city);

	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantCode(String merchantCode);

	public MHGHTCityMerchant getMHGHTCityMerchantByMerchantName(String merchantName);
	
}
