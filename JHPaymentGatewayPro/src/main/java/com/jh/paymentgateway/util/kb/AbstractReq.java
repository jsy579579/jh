package com.jh.paymentgateway.util.kb;


import java.io.Serializable;
import java.util.Date;

/**
 * api通用请求类
 *
 * @author yly
 * @date 2018/5/31.
 */
public abstract class AbstractReq implements Serializable {
    /**
     * 机构号
     */
    protected String merchantId;

    /**
     * 机构流水号
     */
    protected String tradeNo;

    /**
     * 交易时间
     */
    protected Date tradeTime;

    /**
     * GPS经度 longitude
     */
    protected double lng;

    /**
     * GPS纬度 latitude
     */
    protected double lat;

    protected String deviceId;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
