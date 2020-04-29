package com.jh.paymentgateway.controller.ld.business.impl;


import com.jh.paymentgateway.controller.ld.business.LDMerchantBusiness;
import com.jh.paymentgateway.controller.ld.dao.LDMerchantRepository;
import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;
import com.jh.paymentgateway.controller.ld.pojo.LDmerchant;
import com.jh.paymentgateway.controller.qysh.dao.merchantRepository;

import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class LDMerchantBusinesslmpl implements LDMerchantBusiness {

    @Autowired
    EntityManager em;

    @Autowired
    private LDMerchantRepository ldMerchantRepository;

    @Override
    public List<LDmerchant> getHCMerchantProvince() {
        return ldMerchantRepository.getHCMerchantProvince();
    }

    @Override
    public List<merchant_copy> getByCityAndCounty(String city, String county) {
        em.clear();
        List<merchant_copy> result= ldMerchantRepository.getByCityAndCounty(city,county);
        return result;
    }

    @Override
    public List<merchant_copy> getByProvinceAndCounty(String province, String county) {
        em.clear();
        List<merchant_copy> result=ldMerchantRepository.getByProvinceAndCounty(province,county);
        return result;
    }

    @Override
    public List<merchant_copy> getByProvinceAndCityLike(String province, String city) {
        em.clear();
        List<merchant_copy> result=ldMerchantRepository.getByProvinceAndCityLike(province,city);
        return result;
    }

    @Override
    public List<merchant_copy> getAllByProvinceAndCity(String province, String city) {
        em.clear();
        List<merchant_copy> result=ldMerchantRepository.getAllByProvinceAndCity(province,city);
        return result;
    }

    @Override
    public List<HCDMerchant> getAllProvince() {
        em.clear();
        List<HCDMerchant> result=ldMerchantRepository.getAllProvince();
        return result;
    }

    @Override
    public List<HCDMerchant> getAllByCity(String province) {
        em.clear();
        List<HCDMerchant> result=ldMerchantRepository.getAllByCity(province);
        return result;
    }

    @Override
    public List<HCDMerchant> queryByCity(String extra) {
        return ldMerchantRepository.queryByCity(extra);
    }
}
