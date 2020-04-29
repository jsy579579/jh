package com.jh.paymentgateway.controller.ldx.business;

import com.jh.paymentgateway.controller.ldx.pojo.LDXArea;

import java.util.List;

public interface LDXAreaBusiness {

    List<LDXArea> queryAll();

    List<LDXArea> getAllByCity(String province);

    List<LDXArea> getAllByProvince(String province);
}
