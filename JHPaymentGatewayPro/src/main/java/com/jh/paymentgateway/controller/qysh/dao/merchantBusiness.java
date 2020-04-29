package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;

import java.util.List;

public interface merchantBusiness {

    public List<merchant_copy> getAll();

    public merchant_copy create(merchant_copy a);

    public void del(merchant_copy c);

    public List<merchant_copy> getAllByProvinceAndCity(String province,String city);

    public List<merchant_copy> getAllProvince();

    List<merchant_copy> getAllByCity(String province);

    List<merchant_copy> getByCityAndCounty(String province, String city);

    List<merchant_copy> getByProvinceAndCounty(String province, String city);

    List<merchant_copy> getByProvinceAndCityLike(String province, String city);
}
