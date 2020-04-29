package com.jh.paymentgateway.controller.ld.business;

import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;
import com.jh.paymentgateway.controller.ld.pojo.LDmerchant;
import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;


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
public interface LDMerchantBusiness {

    //获取省
    public List<LDmerchant> getHCMerchantProvince();

    List<merchant_copy> getByCityAndCounty(String province, String city);

    List<merchant_copy> getByProvinceAndCounty(String province, String city);

    List<merchant_copy> getByProvinceAndCityLike(String province, String city);

    List<merchant_copy> getAllByProvinceAndCity(String province, String city);

    List<HCDMerchant> getAllProvince();

    List<HCDMerchant> getAllByCity(String province);

    List<HCDMerchant> queryByCity(String extra);
}
