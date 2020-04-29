package com.jh.paymentgateway.controller.tldhx.dao;


import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TLDHXMenchantRepository extends JpaRepository<TLDHXMerchant,Long>, JpaSpecificationExecutor<TLDHXMerchant> {
    //查询所有的省份
    @Query("select distinct me.provincial from TLDHXMerchant me")
    public  List<TLDHXMerchant> getAllProvincial();
//
//    @Query("select  me from TLDHXMerchant me")
//    public  List<TLDHXMerchant> getAllProvincial();

    //查询相应省份的市区
    @Query("SELECT distinct me.city from TLDHXMerchant me where me.provincial=:provincial")
    public List<TLDHXMerchant> getAllByProvincial(@Param("provincial") String provincial);

    @Modifying
    @Query(value = "delete from TLDHXMerchant me where me.city=:city")
    void deleteByUserAndPointIndecs(@Param("city") String city);


}
