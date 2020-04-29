package com.jh.paymentgateway.controller.ldd.business.impl;

import com.jh.paymentgateway.controller.ldd.business.LDDBindCardBusiness;
import com.jh.paymentgateway.controller.ldd.dao.LDDBindCardRepository;
import com.jh.paymentgateway.controller.ldd.pojo.LDDBindCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LDDBindCardBusinessImpl implements LDDBindCardBusiness {

    @Autowired
    private LDDBindCardRepository lddBindCardRepository;

    @Override
    public LDDBindCard queryByBankCard(String bankCard) {
        return lddBindCardRepository.queryByBankCard(bankCard);
    }

    @Override
    public void create(LDDBindCard ldxBindCard) {
        lddBindCardRepository.save(ldxBindCard);
    }
}
