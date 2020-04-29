package com.jh.user.business;

import com.jh.user.pojo.CreditCardRatio;

import java.util.List;

public interface CreditCardRatioBusiness {

    List<CreditCardRatio> queryCreditCardRatioByBrandId(String brandId);
}
