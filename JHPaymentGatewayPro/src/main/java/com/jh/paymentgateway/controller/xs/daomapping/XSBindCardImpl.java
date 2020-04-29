package com.jh.paymentgateway.controller.xs.daomapping;

import com.jh.paymentgateway.controller.xs.pojo.XSXEBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XSBindCardImpl extends JpaRepository<XSXEBindCard, Long>, JpaSpecificationExecutor<XSXEBindCard> {

    @Query("select xs from XSXEBindCard xs where xs.phone= :phone")//预留
    XSXEBindCard queryByPhone(@Param("phone") String phone);

    @Query("select xs from XSXEBindCard xs where xs.idCard= :idcard")
    XSXEBindCard queryByBankCard(@Param("idcard") String idcard);
}
