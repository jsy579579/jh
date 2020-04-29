package com.jh.paymentgateway.repository.xkdhd;

import com.jh.paymentgateway.pojo.xkdhd.XKBankType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface XKBankTypeRepository extends JpaRepository<XKBankType, String> , JpaSpecificationExecutor<XKBankType> {

   @Query("select t from XKBankType  t where t.bankName=?1")
    XKBankType getXKBankTypeByBankName(String bankName);
}
