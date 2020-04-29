package com.jh.paymentgateway.controller.ldd.business.impl;

import com.jh.paymentgateway.controller.ldd.business.LDDAreaBusiness;
import com.jh.paymentgateway.controller.ldd.dao.LDDAreaRepository;
import com.jh.paymentgateway.controller.ldd.pojo.LDDArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LDDAreaBusinessImpl implements LDDAreaBusiness {

    @Autowired
    private LDDAreaRepository lddAreaRepository;

    @Override
    public List<LDDArea> queryAll() {
        return lddAreaRepository.queryAll();
    }

    @Override
    public List<LDDArea> getAllByCity(String city) {
        return lddAreaRepository.getAllByCity(city);
    }

    @Override
    public List<LDDArea> getAllByProvince(String province) {
        return lddAreaRepository.getAllByProvince(province);
    }
}
