package com.jh.paymentgateway.controller.ld.dao;

import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;
import com.jh.paymentgateway.controller.ld.pojo.LDmerchant;


import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LDMerchantRepository extends JpaRepository<HCDMerchant, Long>, JpaSpecificationExecutor<HCDMerchant> {

    //获取所有的省
    @Query("select distinct qy.province from QYSHmerchant qy")
    public List<LDmerchant> getHCMerchantProvince();

    @Query("select me from merchant_copy me where me.province=:province and me.city=:city")
    public List<merchant_copy> getAllByProvinceAndCity(@Param("province")String province, @Param("city")String city);

    @Query("select m from merchant_copy m where m.city=:city and m.county like %:county%")
    List<merchant_copy> getByCityAndCounty(@Param("city") String city, @Param("county") String county);

    @Query("select m from merchant_copy m where m.province=:province and m.county like %:county%")
    List<merchant_copy> getByProvinceAndCounty(@Param("province") String province, @Param("county") String county);

    @Query("select m from merchant_copy m where m.province=:province and m.city like %:city%")
    List<merchant_copy> getByProvinceAndCityLike(@Param("province") String province, @Param("city") String city);

    @Query("select me from  HCDMerchant me where me.city is not null group by me.province")
    public List<HCDMerchant> getAllProvince();

    @Query("select me from  HCDMerchant me where me.province=:province and me.city is not null group by me.city")
    public List<HCDMerchant> getAllByCity(@Param("province")String province);

    @Query("select m from HCDMerchant m where m.city=:city")
    List<HCDMerchant> queryByCity(@Param("city") String city);
}
