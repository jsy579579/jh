package com.jh.paymentgateway.controller.ldx.business.impl;


import com.jh.paymentgateway.controller.ldx.business.LDXRegisterBusiness;
import com.jh.paymentgateway.controller.ldx.dao.LDXRegisterRepository;
import com.jh.paymentgateway.controller.ldx.pojo.LDXRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class LDXRegisterBusinesslmpl implements LDXRegisterBusiness {

    @Autowired
    LDXRegisterRepository ldxRegisterRepository;

    @Autowired
    EntityManager em;

    @Override
    public LDXRegister queryByIdCard(String idCard) {
        return ldxRegisterRepository.queryByIdCard(idCard);
    }

    @Override
    public void create(LDXRegister ldxRegister) {
        ldxRegisterRepository.save(ldxRegister);
    }
}
