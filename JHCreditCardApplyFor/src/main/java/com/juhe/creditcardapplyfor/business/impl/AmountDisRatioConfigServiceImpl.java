package com.juhe.creditcardapplyfor.business.impl;



import com.juhe.creditcardapplyfor.business.AmountDisRatioConfigService;
import com.juhe.creditcardapplyfor.entity.AmountDisRatioConfig;
import com.juhe.creditcardapplyfor.repository.AmountDisRatioConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AmountDisRatioConfigServiceImpl implements AmountDisRatioConfigService {

    @Autowired
    private AmountDisRatioConfigRepository amountDisRatioConfigRepository;
    @Override
    public AmountDisRatioConfig queryConfigByBrandId(String brandId) {
        return amountDisRatioConfigRepository.queryConfigByBrandId(brandId);
    }
}
