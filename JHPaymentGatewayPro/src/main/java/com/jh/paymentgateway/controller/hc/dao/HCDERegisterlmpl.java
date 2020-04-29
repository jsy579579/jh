package com.jh.paymentgateway.controller.hc.dao;


import com.jh.paymentgateway.controller.hc.pojo.HCDEBankBranch;
import com.jh.paymentgateway.controller.hc.pojo.HCDEBindCard;
import com.jh.paymentgateway.controller.hc.pojo.HCDERegister;
import com.jh.paymentgateway.controller.hc.pojo.HCDEmerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class HCDERegisterlmpl implements HCDERegisterBusiness {

    @Autowired
    HCDERegisterRepository qyshRegisterRepository;

    @Autowired
    HCDEBindcardRepository qyshBindcardRepository;

    @Autowired
    HCDEBankBranchRepository qyshBankBranchRepository;

    @Autowired
    HCDEmerchantRepository qysHmerchantRepository;

    @Autowired
    EntityManager em;


    @Transactional
    @Override
    public HCDERegister create(HCDERegister qyshRegister) {
        HCDERegister result=qyshRegisterRepository.save(qyshRegister);
        em.flush();
        return result;
    }

    @Override
    public HCDERegister getHCDERegisterByIdCard(String idcard) {
        em.clear();
        HCDERegister register=qyshRegisterRepository.getHCDERegisterByIdCard(idcard);
        return register;
    }

    @Override
    public HCDEBindCard getHCDEBindCardByBankCard(String bankcard) {
        em.clear();
        HCDEBindCard result=qyshBindcardRepository.getHCDEBindCardByBankCard(bankcard);
        return result;
    }
    @Transactional
    @Override
    public HCDEBindCard create(HCDEBindCard qyshBindCard) {
        HCDEBindCard result=qyshBindcardRepository.save(qyshBindCard);
        em.flush();
        return result;
    }

    @Override
    public List<HCDEmerchant> getMerchantByCity(String city) {
        em.clear();
        List<HCDEmerchant> result=qysHmerchantRepository.getMerchantByCity(city);
        return result;
    }

    @Override
    public HCDEBankBranch getHCDEbanbranch(String bankname) {
        em.clear();
        HCDEBankBranch result=qyshBankBranchRepository.getBankBranchNo(bankname);
        return result;
    }

    @Override
    public List<HCDEmerchant> getHCMerchantProvince() {
        em.clear();
        List<HCDEmerchant> list=qysHmerchantRepository.getAllProvince();
        return list;
    }

    @Override
    public List<HCDEmerchant> getHCMerchantCity(String province) {
        em.clear();
        List<HCDEmerchant> result=qysHmerchantRepository.getAllCityByProvince(province);
        return result;
    }

}
