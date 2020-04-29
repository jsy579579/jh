/**
 * Epaygg.com Inc.
 * Copyright (c) 2016-2017 All Rights Reserved.
 */
package com.jh.paymentchannel.business.impl;

import org.springframework.stereotype.Service;

import com.epayplusplus.api.DefaultEpayppClient;
import com.epayplusplus.api.EpayppClient;
import com.epayplusplus.api.enums.FormatEnum;
import com.epayplusplus.api.enums.SignMethodEnum;
import com.jh.paymentchannel.business.RSBusiness;
import com.jh.paymentchannel.util.EpayppEnvironmentData;

@Service
public class RSBusinessImpl implements RSBusiness {

    /**
     * @see com.epayplusplus.EpayppTest#getEpayppClient()
     */
    @Override
    public EpayppClient getEpayppClient() {
        // 调用易支付epaypp-sdk-java交易订单创建接口
        return new DefaultEpayppClient(EpayppEnvironmentData.getServerUrl(),
            EpayppEnvironmentData.getPartnerId(), EpayppEnvironmentData.getPKCS8PrivateKey(),
            FormatEnum.JSON, "UTF-8", EpayppEnvironmentData.getPKCS8PublicKey(),
            SignMethodEnum.RSA);
    }

}
