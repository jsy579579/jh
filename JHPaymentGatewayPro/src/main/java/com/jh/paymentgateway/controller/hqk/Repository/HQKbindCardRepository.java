package com.jh.paymentgateway.controller.hqk.Repository;


import com.jh.paymentgateway.controller.hqk.pojo.HQKbindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQKbindCardRepository extends JpaRepository<HQKbindCard,Long>, JpaSpecificationExecutor<HQKbindCard> {

    @Query("select hqnew from HQKbindCard hqnew where hqnew.bankCard=:bankCard")
    HQKbindCard getHQKbindCardByBankCard(@Param("bankCard")String bankCard);

    @Query("select hqnew from HQKbindCard hqnew where hqnew.dsorderid=:dsorderid")
    HQKbindCard getHQKbindCardByDsorderid(@Param("dsorderid")String dsorderid);

}
