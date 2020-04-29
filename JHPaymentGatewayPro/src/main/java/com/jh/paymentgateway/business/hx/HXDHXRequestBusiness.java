package com.jh.paymentgateway.business.hx;

import com.jh.paymentgateway.pojo.hx.HXDHAddress;

import java.util.List;

public interface HXDHXRequestBusiness {
    List<HXDHAddress> getHXDHAddressby0();

    List<HXDHAddress> getHXDHAddressbyProvince(String province);

    List<HXDHAddress> getHXDHAddressbyCity(String city);
}
