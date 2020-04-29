package com.jh.paymentgateway.controller.hc.dao;

import com.jh.paymentgateway.controller.hc.pojo.HCDEBankBranch;
import com.jh.paymentgateway.controller.hc.pojo.HCDEBindCard;
import com.jh.paymentgateway.controller.hc.pojo.HCDERegister;
import com.jh.paymentgateway.controller.hc.pojo.HCDEmerchant;

import java.util.List;

/**
 * 文件名: baiyete
 * 包名: com.jh.paymentgateway.controller.qysh.dao
 * 说明:
 * 创建人: -゛Exclusive 〆QZ
 * 创建时间: 2019/9/3 0003  11:02
 * 版本信息: V1.0.1
 * 版权所有:慕翡工业科技(上海)有限公司版权所有
 * 备注:
 */
public interface HCDERegisterBusiness {
    public HCDERegister create(HCDERegister qyshRegister);

    public HCDERegister getHCDERegisterByIdCard(String idcard);

    public HCDEBindCard getHCDEBindCardByBankCard(String bankcard);

    public HCDEBindCard create(HCDEBindCard qyshBindCard);
    //获取银行联行号
    public HCDEBankBranch getHCDEbanbranch(String bankname);
    //获取省
    public List<HCDEmerchant> getHCMerchantProvince();
    //获取市区
    public List<HCDEmerchant> getHCMerchantCity(String province);

    List<HCDEmerchant> getMerchantByCity(String city);
}
