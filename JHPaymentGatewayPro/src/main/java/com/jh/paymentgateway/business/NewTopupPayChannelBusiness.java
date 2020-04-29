package com.jh.paymentgateway.business;


import com.jh.paymentgateway.pojo.hxdhd.HXDCity;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDBindCard;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDRegister;
import com.jh.paymentgateway.pojo.tldhx.TLDHXHHBindCard;
import com.jh.paymentgateway.pojo.tldhx.TLDHXHHRegister;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;



public interface NewTopupPayChannelBusiness {
    //保存环迅大额的用户信息
    HXDHDRegister createHXDHDRegister(HXDHDRegister hxdhdRegister);

    //保存环迅大额的绑卡信息
    HXDHDBindCard createHXDHDBindCard(HXDHDBindCard hxdhdBindCard);

    //通过身份证查环迅进件信息
    HXDHDRegister getHXDHDRegisterByIdCard(String idCard);

    //通过银行卡号查环迅绑卡信息
    HXDHDBindCard getHXDHDBindCardByBankCard(String bankCard);

    //城市列表查询
    List<HXDCity> listAreaInfo(int id);

    //通过银行卡查环迅进件信息
    HXDHDRegister getRegisterByBankCard(String accountNumber);

    HXDHDRegister getHXDHDRegisterByBankCard(String bankCard);

    //通联小额查注册信息
    TLDHXHHRegister getTLDHXRegisterByIdCard(String idCard);

    //通联小额查绑卡信息
    TLDHXHHBindCard getTLDHXBindCardByBankCard(String bankCard);

    //通联小额创建注册信息
    void createTLDHXRegister(TLDHXHHRegister tldhxhhRegister);

    void createTLDHXBindCard(TLDHXHHBindCard tldhxhhBindCard);
}
