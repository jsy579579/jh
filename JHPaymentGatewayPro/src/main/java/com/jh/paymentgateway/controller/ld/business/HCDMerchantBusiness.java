package com.jh.paymentgateway.controller.ld.business;

import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;

import java.awt.print.Pageable;
import java.util.List;

public interface HCDMerchantBusiness {
    List<HCDMerchant> queryAll();

    void save(HCDMerchant data);

    List<HCDMerchant> queryProvince();
}
