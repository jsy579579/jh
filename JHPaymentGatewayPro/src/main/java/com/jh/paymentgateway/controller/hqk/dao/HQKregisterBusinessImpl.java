package com.jh.paymentgateway.controller.hqk.dao;


import com.jh.paymentgateway.controller.hqk.Repository.HQKadreeRepository;
import com.jh.paymentgateway.controller.hqk.Repository.HQKbindCardRepository;
import com.jh.paymentgateway.controller.hqk.Repository.HQKregisterRepository;
import com.jh.paymentgateway.controller.hqk.pojo.HQKRegister;
import com.jh.paymentgateway.controller.hqk.pojo.HQKadree;
import com.jh.paymentgateway.controller.hqk.pojo.HQKbindCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class HQKregisterBusinessImpl implements HQKregisterBusiness{

    @Autowired
    EntityManager em;

    @Autowired
    HQKbindCardRepository hqKbindCardRepository;

    @Autowired
    HQKregisterRepository hqKregisterRepository;

    @Autowired
    HQKadreeRepository hqKadreeRepository;

    @Override
    public HQKbindCard getHQKbindCardByBankCard(String bankcard) {
        em.clear();
        HQKbindCard result=hqKbindCardRepository.getHQKbindCardByBankCard(bankcard);
        return result;
    }

    @Override
    public HQKbindCard getHQKbindCardByDsorderid(String dsorderid) {
        em.clear();
        HQKbindCard result=hqKbindCardRepository.getHQKbindCardByDsorderid(dsorderid);
        return result;
    }

    @Transactional
    @Override
    public void createBindcard(HQKbindCard hqKbindCard) {
        hqKbindCardRepository.save(hqKbindCard);
        em.flush();
    }

    @Transactional
    @Override
    public void createRegister(HQKRegister hqkRegister) {
        hqKregisterRepository.save(hqkRegister);
        em.flush();

    }

    @Override
    public HQKRegister getHQKRegisterByIdCard(String idcard) {
        em.clear();
        HQKRegister result=hqKregisterRepository.getHQKRegisterByIdCard(idcard);
        return result;
    }

    @Transactional
    @Override
    public void createAdree(HQKadree hqKadree) {
        hqKadreeRepository.save(hqKadree);
        em.flush();
    }

//    @Override
//    public List<Object[]> findCityByProvince(String province) {
//        em.clear();
//        List<Object[]> result=hqKadreeRepository.findCityByProvince(province);
//        return result;
//    }
    @Override
    public List<HQKadree> findCityByProvince(String province) {
    em.clear();
    List<HQKadree> result=hqKadreeRepository.findCityByProvince(province);
    return result;
}

    @Override
    public HQKadree findProvinceCodeByCity(String city) {
        em.clear();
        HQKadree result=hqKadreeRepository.findProvinceCodeByCity(city);
        return result;
    }

    @Override
    public List<HQKadree> findAllAdree() {
        em.clear();
        List<HQKadree> result=hqKadreeRepository.findAllAdree();
        return result;
    }
}
