package com.jh.paymentgateway.controller.qysh.dao;

import com.jh.paymentgateway.controller.qysh.pojo.*;
import org.springframework.data.repository.query.Param;

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
public interface QYSHRegisterBusiness {
    public QYSHRegister create(QYSHRegister qyshRegister);

    public QYSHRegister getQYSHRegisterByIdCard(String idcard);

    public QYSHBindCard getQYSHBindCardByBankCard(String bankcard);

    public QYSHBindCard create(QYSHBindCard qyshBindCard);
    //获取银行联行号
    public QYSHBankBranch getQYSHbanbranch(String bankname);
    //获取省
    public List<QYSHmerchant> getHCMerchantProvince();
    //获取市区
    public List<QYSHmerchant> getHCMerchantCity(String province);
    //获取区
    public List<QYSHmerchant> getHCMerchantcounty(String city);
    //获取商户
    public List<QYSHmerchant> getHCMerchantmerabbreviation(String county);
    //获取商户号
    public QYSHmerchant merchantmerabbreviation(String merchantmerabbreviation);
    //获取消费类型
    public List<QYSHmerchant> getindustryType(String county);

    //获取所有消费类型的商户号
    public List<QYSHmerchant> getAllconsumeType(String county, String type);




    //获取所有的MERCHANT对象
    public List<QYSHmerchant> getAll();

    //获取对应的地区
    public List<QYSHmerchant> gettang(String businesslicense);

    public void deleteByUserAndPointIndecs(QYSHmerchant qysHmerchant);
    public void delet(ABC c);
}
