package com.jh.paymentgateway.pojo.hlb;


public class AppPayRefundOrderResponseVo {


    private String rt1_bizType;
    private String rt2_retCode;
    private String rt3_retMsg;
    private String rt4_customerNumber;
    private String rt5_orderId;
    private String rt6_refundOrderNum;
    private String rt7_serialNumber;
    private String rt8_amount;
    private String rt9_currency;
    private String sign;

    public String getRt1_bizType() {
        return rt1_bizType;
    }

    public void setRt1_bizType(String rt1_bizType) {
        this.rt1_bizType = rt1_bizType;
    }

    public String getRt2_retCode() {
        return rt2_retCode;
    }

    public void setRt2_retCode(String rt2_retCode) {
        this.rt2_retCode = rt2_retCode;
    }

    public String getRt3_retMsg() {
        return rt3_retMsg;
    }

    public void setRt3_retMsg(String rt3_retMsg) {
        this.rt3_retMsg = rt3_retMsg;
    }

    public String getRt4_customerNumber() {
        return rt4_customerNumber;
    }

    public void setRt4_customerNumber(String rt4_customerNumber) {
        this.rt4_customerNumber = rt4_customerNumber;
    }

    public String getRt5_orderId() {
        return rt5_orderId;
    }

    public void setRt5_orderId(String rt5_orderId) {
        this.rt5_orderId = rt5_orderId;
    }

    public String getRt6_refundOrderNum() {
        return rt6_refundOrderNum;
    }

    public void setRt6_refundOrderNum(String rt6_refundOrderNum) {
        this.rt6_refundOrderNum = rt6_refundOrderNum;
    }

    public String getRt7_serialNumber() {
        return rt7_serialNumber;
    }

    public void setRt7_serialNumber(String rt7_serialNumber) {
        this.rt7_serialNumber = rt7_serialNumber;
    }

    public String getRt8_amount() {
        return rt8_amount;
    }

    public void setRt8_amount(String rt8_amount) {
        this.rt8_amount = rt8_amount;
    }

    public String getRt9_currency() {
        return rt9_currency;
    }

    public void setRt9_currency(String rt9_currency) {
        this.rt9_currency = rt9_currency;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayRefundOrderResponseVo{" +
                "rt1_bizType='" + rt1_bizType + '\'' +
                ", rt2_retCode='" + rt2_retCode + '\'' +
                ", rt3_retMsg='" + rt3_retMsg + '\'' +
                ", rt4_customerNumber='" + rt4_customerNumber + '\'' +
                ", rt5_orderId='" + rt5_orderId + '\'' +
                ", rt6_refundOrderNum='" + rt6_refundOrderNum + '\'' +
                ", rt7_serialNumber='" + rt7_serialNumber + '\'' +
                ", rt8_amount='" + rt8_amount + '\'' +
                ", rt9_currency='" + rt9_currency + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
