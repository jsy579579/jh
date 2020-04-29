package com.jh.paymentgateway.business.apdh;

import com.jh.paymentgateway.pojo.apdh.APDHCityCode;
import com.jh.paymentgateway.pojo.apdh.APDHIps;
import com.jh.paymentgateway.pojo.apdh.APDHXBindCard;
import com.jh.paymentgateway.pojo.apdh.APDHXRegister;

import java.util.Date;
import java.util.List;

public interface APDHXRequestBusiness {
    APDHXRegister findAPDHXRegisterByIdCard(String idCard);

    APDHXBindCard findAPDHXBindCardByBankdCard(String bankCard);

    APDHXBindCard findAPDHXBindCardByBankdCard1(String bankCard);

    List<APDHCityCode> findAPDHCityCode();

    List<APDHCityCode> findAPDHCityCodeByCode(String code);

    APDHXRegister save(String bankCard, Date date, String idCard, String merchantCode, String phone, String userName);

    APDHXBindCard saveAPDHXBindCard(String bankCard, Date date, String idCard, String phone, String userName, String bindSerialNo);

    void saveAPDHXBindCard(APDHXBindCard apdhxBindCard);

    APDHXBindCard saveAPDHXBindCardContainsBindId(String bankCard, Date date, String idCard, String phone, String userName, String bindSerialNo, String bindId);

    List<APDHIps> findIpsByCity(String s);

    List<APDHIps> findIpsByProvince(String province);
}
