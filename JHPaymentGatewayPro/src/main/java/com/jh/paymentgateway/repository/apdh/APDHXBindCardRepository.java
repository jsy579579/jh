package com.jh.paymentgateway.repository.apdh;

import com.jh.paymentgateway.pojo.apdh.APDHXBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface APDHXBindCardRepository extends JpaRepository<APDHXBindCard, Long>, JpaSpecificationExecutor<APDHXBindCard> {
    @Query("select bq from APDHXBindCard bq where bq.bankCard=:bankCard and bq.bindId is not null and bq.bindId <> ''")
    public APDHXBindCard getBQRegisterByBankCard(@Param("bankCard") String bankCard);

   // @Query("select bq from APDHXBindCard bq where bq.bankCard=:bankCard order by bq.createTime ")
    @Query(nativeQuery=true, value = "select * from t_ap_bind_card where bank_card =?1 order by create_time desc limit 1")
    public APDHXBindCard getBQRegisterByBankCard1(@Param("bankCard") String bankCard);
}
