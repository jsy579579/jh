package com.jh.paymentgateway.controller.tldhx.dao;

import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXArea;
import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMcc;
import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;


@Service
public class TLDHXBusinesslmpl implements TLDHXBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    TLDHXMccRepository jfdeMccRepository;

    @Autowired
    TLDHXMenchantRepository jfdeMenchantRepository;

    @Autowired
    TLDHXAreaRepository jfdeAreaRepository;

    @Override
    public TLDHXArea getAllByArea(String area) {
        em.clear();
        TLDHXArea result=jfdeAreaRepository.getAllByArea(area);
        return result;
    }

    @Override
    public List<TLDHXMerchant> getAllProvincial() {
        em.clear();
        List<TLDHXMerchant> result=jfdeMenchantRepository.getAllProvincial();
        return result;
    }

    @Override
    public List<TLDHXMerchant> getAllByProvincial(String provincial) {
        em.clear();

        List<TLDHXMerchant> result=jfdeMenchantRepository.getAllByProvincial(provincial);

        return result;
    }

    @Override
    public List<TLDHXMcc> getAllMcc() {
        em.clear();
        List<TLDHXMcc> result=jfdeMccRepository.getAllMcc();
        return result;
    }

    @Override
    public TLDHXArea getmccBycity(String city) {
        em.clear();
        TLDHXArea result=jfdeAreaRepository.getmccBycity(city);
        return result;
    }

    @Transactional
    @Override
    public TLDHXMerchant deleteByUserAndPointIndecs(String city) {
        jfdeMenchantRepository.deleteByUserAndPointIndecs(city);
        return null;
    }
}
