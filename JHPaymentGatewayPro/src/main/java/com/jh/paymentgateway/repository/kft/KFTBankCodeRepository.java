package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTBankCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KFTBankCodeRepository extends JpaRepository<KFTBankCode,Long> {
    @Query("select kftbankcode.bankCode from KFTBankCode kftbankcode where kftbankcode.bankName like CONCAT('%',?1,'%') ")
    List<String> getKFTBankCodeByName(String name);
}
