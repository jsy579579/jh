package com.jh.paymentgateway.controller.ldd.business;

import com.jh.paymentgateway.controller.ldd.pojo.LDDRegister;

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
public interface LDDRegisterBusiness {

    LDDRegister queryByIdCard(String idCard);

    void create(LDDRegister ldxRegister);
}
