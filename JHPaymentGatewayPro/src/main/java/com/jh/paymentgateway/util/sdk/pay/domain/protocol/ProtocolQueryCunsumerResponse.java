package com.jh.paymentgateway.util.sdk.pay.domain.protocol;

import com.jh.paymentgateway.util.sdk.domain.Response;

public class ProtocolQueryCunsumerResponse extends Response {

    private String customerCode;
    private String areaCode;
    private String memberId;
    private String mccCode;
    private String outTradeNo;
    private String subCodeList;

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMccCode() {
        return mccCode;
    }

    public void setMccCode(String mccCode) {
        this.mccCode = mccCode;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getSubCodeList() {
        return subCodeList;
    }

    public void setSubCodeList(String subCodeList) {
        this.subCodeList = subCodeList;
    }
}
