package com.jh.paymentgateway.controller.ld.business;

import com.jh.paymentgateway.controller.ld.pojo.LDBindCard;

public interface LDBindCardBusiness {

    LDBindCard queryByBankCard(String bankCard);

    void create(LDBindCard ldBindCard);
}
