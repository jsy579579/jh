package com.jh.paymentgateway.controller.xs.daomapping;

import com.jh.paymentgateway.controller.xs.pojo.TSXsxeAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XsXeaddresslmpl extends JpaRepository<TSXsxeAddress, Long>, JpaSpecificationExecutor<TSXsxeAddress> {

    @Query("select xs from TSXsxeAddress xs where xs.province= :province")
    List<TSXsxeAddress> queryTSXsxeAddress(@Param("province") String province);

    @Query("select bq from TSXsxeAddress bq group by bq.province")
    List<TSXsxeAddress> getTSXsxeAddress();


    @Query("select xs from TSXsxeAddress xs where xs.province= :city")
    TSXsxeAddress getTSXsxeAddressMcc(@Param("city") String province);



}
