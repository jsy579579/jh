package com.jh.paymentgateway.controller.hqk.dao;


import com.jh.paymentgateway.controller.hqk.pojo.HQKRegister;
import com.jh.paymentgateway.controller.hqk.pojo.HQKadree;
import com.jh.paymentgateway.controller.hqk.pojo.HQKbindCard;

import java.util.List;

public interface HQKregisterBusiness {

    HQKbindCard getHQKbindCardByBankCard(String bankcard);

    HQKRegister getHQKRegisterByIdCard(String idcard);

    void createRegister(HQKRegister hqkRegister);
    void createBindcard(HQKbindCard hqKbindCard);
    HQKbindCard getHQKbindCardByDsorderid(String dsorderid);





    List<HQKadree> findAllAdree();
    List<HQKadree> findCityByProvince(String province);
    HQKadree findProvinceCodeByCity(String city);
    void createAdree(HQKadree hqKadree);
}
