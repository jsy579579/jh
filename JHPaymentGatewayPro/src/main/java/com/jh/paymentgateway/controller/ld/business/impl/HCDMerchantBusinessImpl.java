package com.jh.paymentgateway.controller.ld.business.impl;

import com.jh.paymentgateway.controller.ld.business.HCDMerchantBusiness;
import com.jh.paymentgateway.controller.ld.dao.HCDMerchantRepository;
import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HCDMerchantBusinessImpl implements HCDMerchantBusiness {

    @Autowired
    private HCDMerchantRepository hcdMerchantRepository;

    @Override
    public List<HCDMerchant> queryAll() {
        return hcdMerchantRepository.queryAll();
    }

    @Override
    public void save(HCDMerchant data) {
        hcdMerchantRepository.save(data);
    }

    @Override
    public List<HCDMerchant> queryProvince() {
        return hcdMerchantRepository.queryProvince();
    }
}
