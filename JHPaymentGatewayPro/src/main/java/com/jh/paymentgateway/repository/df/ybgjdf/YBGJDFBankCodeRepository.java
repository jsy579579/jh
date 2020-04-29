package com.jh.paymentgateway.repository.df.ybgjdf;

import com.jh.paymentgateway.pojo.ybgjdf.YbgjdfBankCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YBGJDFBankCodeRepository extends JpaRepository<YbgjdfBankCode, String>, JpaSpecificationExecutor<YbgjdfBankCode> {
    @Query("select ybgjdfBankCode from YbgjdfBankCode ybgjdfBankCode where ybgjdfBankCode.bankName=:bankName")
    public YbgjdfBankCode getCJBindCardByBankName(@Param("bankName") String bankName);
}
