package com.jh.paymentgateway.controller.tldh.dao;

import com.jh.paymentgateway.controller.tldh.Repository.TLAreeRepository;
import com.jh.paymentgateway.controller.tldh.Repository.TLBindCardRepostory;
import com.jh.paymentgateway.controller.tldh.Repository.TLBnakcodeRepository;
import com.jh.paymentgateway.controller.tldh.Repository.TLRegisterRepostory;
import com.jh.paymentgateway.controller.tldh.pojo.TLAree;
import com.jh.paymentgateway.controller.tldh.pojo.TLBankcode;
import com.jh.paymentgateway.controller.tldh.pojo.TLBindCard;
import com.jh.paymentgateway.controller.tldh.pojo.TLRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class TLRegisterBusinesslmpl implements TLRegisterBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    TLRegisterRepostory tlRegisterRepostory;

    @Autowired
    TLBindCardRepostory tlBindCardRepostory;

    @Autowired
    TLBnakcodeRepository tlBnakcodeRepository;

    @Autowired
    TLAreeRepository tlAreeRepository;

    @Override
    public TLRegister findTLRegisterByIdcard(String idcard) {
        em.clear();
        TLRegister result=tlRegisterRepostory.getTLRegisterByIdcard(idcard);
        return result;
    }
    @Transactional
    @Override
    public void createBindCard(TLBindCard tlBindCard) {
        tlBindCardRepostory.save(tlBindCard);
        em.flush();
    }

    @Override
    public List<TLAree> findAllProvince() {
        em.clear();

        List<TLAree> result=tlAreeRepository.getAllProvince();
        return result;
    }

    @Override
    public List<TLAree> findCityCodeByCity(String city) {
        em.clear();
        List<TLAree> result=tlAreeRepository.getCityCodeByCity(city);
        return result;
    }

    @Override
    public List<TLAree> findCityByProvince(String province) {
        em.clear();

        List<TLAree> result=tlAreeRepository.getCityByProvince(province);
        return result;
    }

    @Override
    public TLBankcode findTLBankcodeByBankName(String bankName) {
        em.clear();
        TLBankcode result=tlBnakcodeRepository.getTLBankcodeByBankName(bankName);
        return result;
    }

    @Transactional
    @Override
    public void createRegister(TLRegister tlRegister) {
        tlRegisterRepostory.save(tlRegister);
        em.flush();
    }

    @Override
    public TLBindCard findTLBindCardByBankCard(String bankCard) {
        em.clear();
        TLBindCard result=tlBindCardRepostory.getTLBindCardByBankCard(bankCard);
        return result;
    }

}
