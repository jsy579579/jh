package com.jh.paymentgateway.controller.xs.daomapping;

import com.jh.paymentgateway.controller.xs.pojo.XsXeRegistr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XsxeRegisterlmpl extends JpaRepository<XsXeRegistr, Long>, JpaSpecificationExecutor<XsXeRegistr> {

    @Query(value = "select xs from XsXeRegistr xs where xs.bankCard= :BankCard and xs.status= 1")
    XsXeRegistr queryByBankCard(@Param("BankCard") String BankCard);

}
