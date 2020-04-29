package com.jh.paymentgateway.controller.ldx.business.impl;

import com.jh.paymentgateway.controller.ldx.business.LDXAreaBusiness;
import com.jh.paymentgateway.controller.ldx.dao.LDXAreaRepository;
import com.jh.paymentgateway.controller.ldx.pojo.LDXArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LDXAreaBusinessImpl implements LDXAreaBusiness {

    @Autowired
    private LDXAreaRepository ldxAreaRepository;

    @Override
    public List<LDXArea> queryAll() {
        return ldxAreaRepository.queryAll();
    }

    @Override
    public List<LDXArea> getAllByCity(String city) {
        return ldxAreaRepository.getAllByCity(city);
    }

    @Override
    public List<LDXArea> getAllByProvince(String province) {
        return ldxAreaRepository.getAllByProvince(province);
    }
}
