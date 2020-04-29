package com.jh.paymentgateway.controller.hqm.dao;

import com.jh.paymentgateway.controller.hqm.pojo.HQMRegister;
import com.jh.paymentgateway.controller.hqm.pojo.HQMbindCard;
import com.jh.paymentgateway.controller.hqm.repository.HQMRegisterRepository;
import com.jh.paymentgateway.controller.hqm.repository.HQMbindCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


@Service
public class HQMBusinesslmpl implements HQMBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    HQMbindCardRepository hqMbindCardRepository;

    @Autowired
    HQMRegisterRepository hqmRegisterRepository;

    @Override
    public HQMbindCard findHQMbindCardByBankCard(String bankCard) {
        em.clear();
        HQMbindCard result=hqMbindCardRepository.getHQMbindCardByBankCard(bankCard);
        return result;
    }

    @Transactional
    @Override
    public void createHQMRegister(HQMRegister hqmRegister ) {
        hqmRegisterRepository.save(hqmRegister);
        em.flush();
    }

    @Override
    public HQMbindCard getHQXBindCardByOrderId(String orderId) {
        em.clear();
        HQMbindCard result=hqMbindCardRepository.getHQMBindCardByOrderId(orderId);
        return result;
    }

    @Transactional
    @Override
    public void createHQMbindCard(HQMbindCard hqMbindCard) {
        hqMbindCardRepository.save(hqMbindCard);
        em.flush();
    }

    @Override
    public HQMRegister findHQMRegisterByIdCard(String idCard) {
        em.clear();
        HQMRegister result=hqmRegisterRepository.getHQTRegisterByIdCard(idCard);
        return result;
    }
}
