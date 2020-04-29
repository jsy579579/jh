package com.jh.paymentgateway.controller.ld.business.impl;

import com.jh.paymentgateway.controller.ld.business.LDBindCardBusiness;
import com.jh.paymentgateway.controller.ld.dao.LDBindCardRepository;
import com.jh.paymentgateway.controller.ld.pojo.LDBindCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LDBindCardBusinessImpl implements LDBindCardBusiness {

    @Autowired
    private LDBindCardRepository ldBindCardRepository;

    @Override
    public LDBindCard queryByBankCard(String bankCard) {
        return ldBindCardRepository.queryByBankCard(bankCard);
    }

    @Override
    public void create(LDBindCard ldBindCard) {
        ldBindCardRepository.save(ldBindCard);
    }
}
