package com.jh.paymentgateway.controller.ldd.business;

import com.jh.paymentgateway.controller.ldd.pojo.LDDArea;

import java.util.List;

public interface LDDAreaBusiness {

    List<LDDArea> queryAll();

    List<LDDArea> getAllByCity(String province);

    List<LDDArea> getAllByProvince(String province);
}
