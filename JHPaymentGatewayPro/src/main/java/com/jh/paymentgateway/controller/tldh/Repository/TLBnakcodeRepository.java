package com.jh.paymentgateway.controller.tldh.Repository;

import com.jh.paymentgateway.controller.tldh.pojo.TLBankcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 文件名: juhepayment
 * 包名: com.jh.paymentgateway.controller.tldh.Repository
 * 说明:
 * 创建人: -゛Exclusive 〆QZ
 * 创建时间: 2019/9/26 0026  11:19
 * 版本信息: V1.0.1
 * 版权所有:慕翡工业科技(上海)有限公司版权所有
 * 备注:
 */
@Repository
public interface TLBnakcodeRepository extends JpaRepository<TLBankcode,Long>, JpaSpecificationExecutor<TLBankcode> {
    @Query("select tl from TLBankcode tl where tl.bankName=:bankName")
    public TLBankcode getTLBankcodeByBankName(@Param("bankName") String bankName);
}
