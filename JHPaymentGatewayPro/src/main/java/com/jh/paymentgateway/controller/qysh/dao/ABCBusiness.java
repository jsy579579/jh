package com.jh.paymentgateway.controller.qysh.dao;

import com.jh.paymentgateway.controller.qysh.pojo.ABC;


import java.util.List;

/**
 * 文件名: juhepayment
 * 包名: com.jh.paymentgateway.controller.qysh.dao
 * 说明:
 * 创建人: -゛Exclusive 〆QZ
 * 创建时间: 2019/9/10 0010  17:19
 * 版本信息: V1.0.1
 * 版权所有:慕翡工业科技(上海)有限公司版权所有
 * 备注:
 */
public interface ABCBusiness {

    List<ABC> getAll();

    ABC create(ABC a);

    void del(ABC c);
}
