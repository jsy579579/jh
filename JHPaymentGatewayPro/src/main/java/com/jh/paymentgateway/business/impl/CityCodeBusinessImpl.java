package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.business.CityCodeBusiness;
import com.jh.paymentgateway.pojo.tl.TLDHXCity;
import com.jh.paymentgateway.repository.tl.TLDHXCityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CityCodeBusinessImpl implements CityCodeBusiness {
    @Autowired
    private TLDHXCityRepository tldhxCityRepository;

    @Override
    public List<TLDHXCity> getTLDHXCitybyPid(String pid) {
        List<TLDHXCity> tldhxCitys= tldhxCityRepository.getTLDHXCitysByPid(pid);
        return tldhxCitys;
    }
}
