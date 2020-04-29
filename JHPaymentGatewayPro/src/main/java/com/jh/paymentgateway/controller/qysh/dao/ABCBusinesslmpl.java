package com.jh.paymentgateway.controller.qysh.dao;

import com.jh.paymentgateway.controller.qysh.pojo.ABC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class ABCBusinesslmpl implements ABCBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    ABCRepository ABCRepository;

    @Override
    public List<ABC> getAll() {
        em.clear();
        List<ABC> result= ABCRepository.getAll();
        return result;
    }

    @Override
    public ABC create(ABC a) {
        ABC result= ABCRepository.save(a);
        return result;
    }

    @Transactional
    @Override
    public void del(ABC c) {
        ABCRepository.delete(c);
        em.flush();
    }
}
