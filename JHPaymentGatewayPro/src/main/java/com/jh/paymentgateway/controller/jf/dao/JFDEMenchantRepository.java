package com.jh.paymentgateway.controller.jf.dao;


import com.jh.paymentgateway.controller.jf.pojo.JFDEMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JFDEMenchantRepository extends JpaRepository<JFDEMerchant,Long>, JpaSpecificationExecutor<JFDEMerchant> {
    //查询所有的省份
    @Query("select distinct me.provincial from JFDEMerchant me")
    public  List<JFDEMerchant> getAllProvincial();
//
//    @Query("select  me from JFDEMerchant me")
//    public  List<JFDEMerchant> getAllProvincial();

    //查询相应省份的市区
    @Query("SELECT distinct me.city from JFDEMerchant me where me.provincial=:provincial")
    public List<JFDEMerchant> getAllByProvincial(@Param("provincial")String provincial);

    @Modifying
    @Query(value = "delete from JFDEMerchant me where me.city=:city")
    void deleteByUserAndPointIndecs(@Param("city") String city);


}
