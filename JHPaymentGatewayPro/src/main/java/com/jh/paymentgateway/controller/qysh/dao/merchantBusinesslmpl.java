package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class merchantBusinesslmpl implements merchantBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    merchantRepository merchantRepository;


    @Override
    public List<merchant_copy> getAll() {
        em.clear();
        List<merchant_copy> result=merchantRepository.getAll();
        return result;
    }

    @Transactional
    @Override
    public merchant_copy create(merchant_copy a) {
        merchant_copy result=merchantRepository.save(a);
        em.flush();
        return result;
    }

    @Override
    public List<merchant_copy> getAllByProvinceAndCity(String province,String city) {
        em.clear();
        List<merchant_copy> result=merchantRepository.getAllByProvinceAndCity(province,city);
        return result;
    }

    @Override
    public List<merchant_copy> getAllProvince() {
        em.clear();
        List<merchant_copy> result=merchantRepository.getAllProvince();
        return result;
    }

    @Override
    public List<merchant_copy> getAllByCity(String province) {
        em.clear();
        List<merchant_copy> result=merchantRepository.getAllByCity(province);
        return result;
    }

    @Override
    public List<merchant_copy> getByCityAndCounty(String city, String county) {
        em.clear();
        List<merchant_copy> result=merchantRepository.getByCityAndCounty(city,county);
        return result;
    }

    @Override
    public List<merchant_copy> getByProvinceAndCounty(String province, String county) {
        em.clear();
        List<merchant_copy> result=merchantRepository.getByProvinceAndCounty(province,county);
        return result;
    }

    @Override
    public List<merchant_copy> getByProvinceAndCityLike(String province, String city) {
        em.clear();
        List<merchant_copy> result=merchantRepository.getByProvinceAndCityLike(province,city);
        return result;
    }

    @Transactional
    @Override
    public void del(merchant_copy c) {
        merchantRepository.delete(c);
        em.flush();
    }
}
