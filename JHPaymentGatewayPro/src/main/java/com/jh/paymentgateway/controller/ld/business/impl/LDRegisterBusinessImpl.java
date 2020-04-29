package com.jh.paymentgateway.controller.ld.business.impl;

import com.jh.paymentgateway.controller.ld.business.LDRegisterBusiness;
import com.jh.paymentgateway.controller.ld.dao.LDQuickRegisterRepository;
import com.jh.paymentgateway.controller.ld.pojo.LDQuickRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LDRegisterBusinessImpl implements LDRegisterBusiness {

    @Autowired
    private LDQuickRegisterRepository ldRegisterRepository;

    @Override
    public LDQuickRegister queryByIdCard(String idCard) {
        return ldRegisterRepository.queryByIdCard(idCard);
    }

    @Override
    public void create(LDQuickRegister ykRegister) {
        ldRegisterRepository.save(ykRegister);
    }
}
