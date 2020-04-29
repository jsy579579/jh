package com.juhe.creditcardapplyfor.business;


import com.juhe.creditcardapplyfor.entity.AmountDisRatioConfig;

public interface AmountDisRatioConfigService {

    AmountDisRatioConfig queryConfigByBrandId(String brandId);
}
