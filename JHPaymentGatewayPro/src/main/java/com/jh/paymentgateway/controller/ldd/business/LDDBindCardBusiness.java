package com.jh.paymentgateway.controller.ldd.business;

import com.jh.paymentgateway.controller.ldd.pojo.LDDBindCard;

public interface LDDBindCardBusiness {

    LDDBindCard queryByBankCard(String bankCard);

    void create(LDDBindCard ldxBindCard);
}
