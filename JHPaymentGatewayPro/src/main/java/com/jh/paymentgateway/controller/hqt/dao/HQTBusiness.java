package com.jh.paymentgateway.controller.hqt.dao;


import com.jh.paymentgateway.controller.hqt.pojo.HQTRegister;
import com.jh.paymentgateway.controller.hqt.pojo.HQTbindCard;

public interface HQTBusiness {

    HQTbindCard findHQTbindCardByBankCard(String bankCard);
    HQTRegister findHQTRegisterByIdCard(String idCard);
    void createHQTRegister(HQTRegister hqtRegister);
    void createHQTbindCard(HQTbindCard hqbBindCard);
    public HQTbindCard getHQXBindCardByOrderId(String orderId);
}
