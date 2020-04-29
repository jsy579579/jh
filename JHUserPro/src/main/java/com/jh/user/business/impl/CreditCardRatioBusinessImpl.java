package com.jh.user.business.impl;

import com.jh.user.business.CreditCardRatioBusiness;
import com.jh.user.pojo.CreditCardRatio;
import com.jh.user.repository.CreditCardRatioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditCardRatioBusinessImpl implements CreditCardRatioBusiness {

    @Autowired
    private CreditCardRatioRepository creditCardRatioRepository;

    @Override
    public List<CreditCardRatio> queryCreditCardRatioByBrandId(String brandId) {
        return creditCardRatioRepository.queryCreditCardRatioByBrandId(brandId);
    }
}
