package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.HCDE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class HCDEbusinessImpl implements HCDEBusiness{

    @Autowired
    HCDERepository hcdeRepository;
    @Autowired
    EntityManager em;


    @Override
    public List<HCDE> getAll() {
        em.clear();
        List<HCDE> result=hcdeRepository.getAllBus();
        return result;
    }

    @Transactional
    @Override
    public void del(HCDE hcde) {
        hcdeRepository.delete(hcde);
        em.flush();
    }
    @Transactional
    @Override
    public void create(HCDE hcde) {
        hcdeRepository.save(hcde);
        em.flush();
    }
}
