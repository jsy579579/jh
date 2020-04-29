package com.jh.paymentgateway.controller.tldh.dao;


import com.jh.paymentgateway.controller.tldh.pojo.TLAree;
import com.jh.paymentgateway.controller.tldh.pojo.TLBankcode;
import com.jh.paymentgateway.controller.tldh.pojo.TLBindCard;
import com.jh.paymentgateway.controller.tldh.pojo.TLRegister;

import java.util.List;

public interface TLRegisterBusiness {

    public TLRegister findTLRegisterByIdcard(String idcard);

    public TLBindCard findTLBindCardByBankCard(String bankCard);

    public void createRegister(TLRegister tlRegister);
    public void createBindCard(TLBindCard tlBindCard);
    public TLBankcode findTLBankcodeByBankName(String bankName);



    List<TLAree> findAllProvince();

    List<TLAree> findCityByProvince(String province);
    List<TLAree> findCityCodeByCity(String city);
}
