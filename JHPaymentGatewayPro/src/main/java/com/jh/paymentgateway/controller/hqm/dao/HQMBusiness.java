package com.jh.paymentgateway.controller.hqm.dao;


import com.jh.paymentgateway.controller.hqm.pojo.HQMRegister;
import com.jh.paymentgateway.controller.hqm.pojo.HQMbindCard;

public interface HQMBusiness {

    HQMbindCard findHQMbindCardByBankCard(String bankCard);
    HQMRegister findHQMRegisterByIdCard(String idCard);
    void createHQMRegister(HQMRegister hqtRegister);
    void createHQMbindCard(HQMbindCard hqbBindCard);
    HQMbindCard getHQXBindCardByOrderId(String orderId);
}
