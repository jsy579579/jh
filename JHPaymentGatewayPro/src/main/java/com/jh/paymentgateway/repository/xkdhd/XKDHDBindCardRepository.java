package com.jh.paymentgateway.repository.xkdhd;

import com.jh.paymentgateway.pojo.xkdhd.XKDHDBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface XKDHDBindCardRepository extends JpaRepository<XKDHDBindCard, String>, JpaSpecificationExecutor<XKDHDBindCard> {

    @Query("select t from XKDHDBindCard  t where t.bankCard=?1")
    XKDHDBindCard getXKDHDBindCardByBankCard(String bankCard);
}
