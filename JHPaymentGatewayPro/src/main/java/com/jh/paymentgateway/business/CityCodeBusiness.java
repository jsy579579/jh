package com.jh.paymentgateway.business;

import com.jh.paymentgateway.pojo.tl.TLDHXCity;

import java.util.List;

public interface CityCodeBusiness {
    List<TLDHXCity> getTLDHXCitybyPid(String pid);
}
