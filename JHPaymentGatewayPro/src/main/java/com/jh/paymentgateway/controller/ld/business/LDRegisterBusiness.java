package com.jh.paymentgateway.controller.ld.business;

import com.jh.paymentgateway.controller.ld.pojo.LDQuickRegister;

public interface LDRegisterBusiness {

    LDQuickRegister queryByIdCard(String idCard);

    void create(LDQuickRegister ykRegister);
}
