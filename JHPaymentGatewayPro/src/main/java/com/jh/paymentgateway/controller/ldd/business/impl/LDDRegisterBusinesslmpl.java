package com.jh.paymentgateway.controller.ldd.business.impl;


import com.jh.paymentgateway.controller.ldd.business.LDDRegisterBusiness;
import com.jh.paymentgateway.controller.ldd.dao.LDDRegisterRepository;
import com.jh.paymentgateway.controller.ldd.pojo.LDDRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class LDDRegisterBusinesslmpl implements LDDRegisterBusiness {

    @Autowired
    LDDRegisterRepository lddRegisterRepository;

    @Autowired
    EntityManager em;

    @Override
    public LDDRegister queryByIdCard(String idCard) {
        return lddRegisterRepository.queryByIdCard(idCard);
    }

    @Override
    public void create(LDDRegister ldxRegister) {
        lddRegisterRepository.save(ldxRegister);
    }
}
