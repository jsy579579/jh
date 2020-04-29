package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.QYSHmerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QYSHmerchantRepository extends JpaRepository<QYSHmerchant, Long>, JpaSpecificationExecutor<QYSHmerchant> {

    //获取所有的省
    @Query("select distinct qy.province from QYSHmerchant qy")
    public List<QYSHmerchant> getAllProvince();



    //获取省份下的市区
    @Query("select distinct qy.city from QYSHmerchant qy where qy.province=:province")
    public List<QYSHmerchant> getAllCityByProvince(@Param("province") String province);

    //获取区
    @Query("select distinct qy.county from QYSHmerchant qy where qy.city=:city")
    public List<QYSHmerchant> getAllcountyByCity(@Param("city") String city);

    //获取商户
    @Query("select distinct qy.merabbreviation from QYSHmerchant qy where qy.county=:county")
    public List<QYSHmerchant> getAllMerabbreviationByCounty(@Param("county") String county);

    //获取商户ID
    @Query("select qy from  QYSHmerchant qy where  qy.merabbreviation=:merabbreviation")
    QYSHmerchant getMerabbreviation(@Param("merabbreviation") String merabbreviation);

    //获取商户消费类型
    @Query("SELECT distinct qy.industryType from QYSHmerchant qy where qy.county=:county")
    public List<QYSHmerchant> getAllindustryTypebyCounty(@Param("county") String county);

    //获取商户消费类型
    @Query("SELECT qy from QYSHmerchant qy where qy.county=:county and qy.industryType=:industryType")
    public List<QYSHmerchant> getAllindustryType(@Param("county") String county, @Param("industryType") String industryType);




    @Query("select  qy from QYSHmerchant qy")
    public List<QYSHmerchant> getAll();


    @Query("select  qy from QYSHmerchant qy where qy.businesslicense like :businesslicense%")
    public List<QYSHmerchant> gettang(@Param("businesslicense")String businesslicense);




}
