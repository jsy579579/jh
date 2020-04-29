package com.jh.paymentgateway.controller.yxe.dao;

import com.jh.paymentgateway.controller.yxe.pojo.YXEAddress;
import com.jh.paymentgateway.controller.yxe.pojo.YXEBankBin;
import com.jh.paymentgateway.controller.yxe.pojo.YXERegister;

import java.util.List;

public interface YXEBusiness {
    // 获取用户绑卡信息
    YXERegister getYXERegisterByIdCard(String idCard, String bankCard);
    // 保存绑卡信息
    void createYXERegister(YXERegister yxeRegister);

    YXEAddress getYXEAddressByCityName(String cityName);

    YXEBankBin getYXEBankBinByBankName(String creditCardBankName);

    YXERegister getYXERegisterByIdCard(String bankCard);

    List<YXEAddress> findByCity(String provinceId);
}
