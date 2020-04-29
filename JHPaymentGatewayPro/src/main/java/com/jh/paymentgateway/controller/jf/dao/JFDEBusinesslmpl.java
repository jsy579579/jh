package com.jh.paymentgateway.controller.jf.dao;

import com.jh.paymentgateway.controller.jf.pojo.JFDEArea;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMcc;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;


@Service
public class JFDEBusinesslmpl implements JFDEBusiness{

    @Autowired
    EntityManager em;

    @Autowired
    JFDEMccRepository jfdeMccRepository;

    @Autowired
    JFDEMenchantRepository jfdeMenchantRepository;

    @Autowired
    JFDEAreaRepository jfdeAreaRepository;

    @Override
    public JFDEArea getAllByArea(String area) {
        em.clear();
        JFDEArea result=jfdeAreaRepository.getAllByArea(area);
        return result;
    }

    @Override
    public List<JFDEMerchant> getAllProvincial() {
        em.clear();
        List<JFDEMerchant> result=jfdeMenchantRepository.getAllProvincial();
        return result;
    }

    @Override
    public List<JFDEMerchant> getAllByProvincial(String provincial) {
        em.clear();

        List<JFDEMerchant> result=jfdeMenchantRepository.getAllByProvincial(provincial);

        return result;
    }

    @Override
    public List<JFDEMcc> getAllMcc() {
        em.clear();
        List<JFDEMcc> result=jfdeMccRepository.getAllMcc();
        return result;
    }

    @Override
    public JFDEArea getmccBycity(String city) {
        em.clear();
        JFDEArea result=jfdeAreaRepository.getmccBycity(city);
        return result;
    }

    @Transactional
    @Override
    public JFDEMerchant deleteByUserAndPointIndecs(String city) {
        jfdeMenchantRepository.deleteByUserAndPointIndecs(city);
        return null;
    }
}
