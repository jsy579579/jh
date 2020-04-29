package com.jh.paymentgateway.controller.ldx.business.impl;

import com.jh.paymentgateway.controller.ldx.business.LDXBindCardBusiness;
import com.jh.paymentgateway.controller.ldx.dao.LDXBindCardRepository;
import com.jh.paymentgateway.controller.ldx.pojo.LDXBindCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LDXBindCardBusinessImpl implements LDXBindCardBusiness {

    @Autowired
    private LDXBindCardRepository ldxBindCardRepository;

    @Override
    public LDXBindCard queryByBankCard(String bankCard) {
        return ldxBindCardRepository.queryByBankCard(bankCard);
    }

    @Override
    public void create(LDXBindCard ldxBindCard) {
        ldxBindCardRepository.save(ldxBindCard);
    }
}
