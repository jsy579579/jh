package com.jh.paymentgateway.repository.hq;

import com.jh.paymentgateway.pojo.hq.HQNEWBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQNEWBindCardRepository
        extends JpaRepository<HQNEWBindCard, String>, JpaSpecificationExecutor<HQNEWBindCard> {
    @Query("select hqnew from HQNEWBindCard hqnew where hqnew.bankCard=:bankCard and hqnew.bindId is not null and hqnew.bindId <> ''")
    HQNEWBindCard getHQNEWBindCardByBankCard(@Param("bankCard")String bankCard);

    @Query("select hqnew from HQNEWBindCard hqnew where hqnew.dsorderid=:dsorderid")
    HQNEWBindCard getHQNEWBindCardByDsorderid(@Param("dsorderid")String dsorderid);

}
