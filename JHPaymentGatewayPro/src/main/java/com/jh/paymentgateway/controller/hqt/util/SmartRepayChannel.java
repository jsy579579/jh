package com.jh.paymentgateway.controller.hqt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 小额聚合
 * <p>
 * 实际调用的接口在抽象类里面。
 */

public class SmartRepayChannel extends AbstractChannel {

    public static final String postUrl = "http://pay.huanqiuhuiju.com/authsys/api/large/channel/pay/execute.do"; // 请求地址

    public static final String transcode = "050";// 小额的交易码

    private static final Logger LOG = LoggerFactory.getLogger(SmartRepayChannel.class);

    public SmartRepayChannel() {

        super(transcode, postUrl);
        // TODO Auto-generated constructor stub
    }
}
