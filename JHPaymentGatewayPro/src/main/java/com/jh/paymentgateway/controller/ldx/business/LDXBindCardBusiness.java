package com.jh.paymentgateway.controller.ldx.business;

import com.jh.paymentgateway.controller.ldx.pojo.LDXBindCard;

public interface LDXBindCardBusiness {

    LDXBindCard queryByBankCard(String bankCard);

    void create(LDXBindCard ldxBindCard);
}
