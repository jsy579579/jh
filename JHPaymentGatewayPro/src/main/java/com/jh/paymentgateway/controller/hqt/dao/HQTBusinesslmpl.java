package com.jh.paymentgateway.controller.hqt.dao;

import com.jh.paymentgateway.controller.hqt.pojo.HQTRegister;
import com.jh.paymentgateway.controller.hqt.pojo.HQTbindCard;
import com.jh.paymentgateway.controller.hqt.repository.HQTRegisterRepository;
import com.jh.paymentgateway.controller.hqt.repository.HQTbindCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


@Service
public class HQTBusinesslmpl implements HQTBusiness{

    @Autowired
    EntityManager em;

    @Autowired
    HQTbindCardRepository hqTbindCardRepository;

    @Autowired
    HQTRegisterRepository hqtRegisterRepository;

    @Override
    public HQTbindCard findHQTbindCardByBankCard(String bankCard) {
        em.clear();
        HQTbindCard result=hqTbindCardRepository.getHQTbindCardByBankCard(bankCard);
        return result;
    }

    @Transactional
    @Override
    public void createHQTRegister(HQTRegister hqtRegister) {
        hqtRegisterRepository.save(hqtRegister);
        em.flush();
    }

    @Override
    public HQTbindCard getHQXBindCardByOrderId(String orderId) {
        em.clear();
        HQTbindCard result=hqTbindCardRepository.getHQXBindCardByOrderId(orderId);
        return result;
    }

    @Transactional
    @Override
    public void createHQTbindCard(HQTbindCard hqbBindCard) {
        hqTbindCardRepository.save(hqbBindCard);
        em.flush();
    }

    @Override
    public HQTRegister findHQTRegisterByIdCard(String idCard) {
        em.clear();
        HQTRegister result=hqtRegisterRepository.getHQTRegisterByIdCard(idCard);
        return result;
    }
}
