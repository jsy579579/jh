package com.jh.paymentgateway.controller.hc.dao;


import com.jh.paymentgateway.controller.hc.pojo.HCDEmerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HCDEmerchantRepository extends JpaRepository<HCDEmerchant, Long>, JpaSpecificationExecutor<HCDEmerchant> {

//获取所有的省
    @Query("select distinct qy.province from HCDEmerchant qy")
    public List<HCDEmerchant> getAllProvince();


    //获取省份下的市区
    @Query("select distinct qy.city from HCDEmerchant qy where qy.province=:province")
    public List<HCDEmerchant> getAllCityByProvince(@Param("province") String province);

    //通过城市获取到所有的商户
    @Query("select  qy from HCDEmerchant qy where qy.city=:city")
    public List<HCDEmerchant> getMerchantByCity(@Param("city") String city);

}
