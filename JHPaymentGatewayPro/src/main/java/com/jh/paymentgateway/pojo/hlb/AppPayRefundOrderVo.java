package com.jh.paymentgateway.pojo.hlb;


public class AppPayRefundOrderVo {


    private String P1_bizType;
    private String P2_orderId;
    private String P3_customerNumber;
    private String P4_refundOrderId;
    private String P5_amount;
    private String P6_callbackUrl;
    private String P7_desc;
    private String P8_orderSerialNumber;
    private String sign;

    public String getP1_bizType() {
        return P1_bizType;
    }

    public void setP1_bizType(String p1_bizType) {
        P1_bizType = p1_bizType;
    }

    public String getP2_orderId() {
        return P2_orderId;
    }

    public void setP2_orderId(String p2_orderId) {
        P2_orderId = p2_orderId;
    }

    public String getP3_customerNumber() {
        return P3_customerNumber;
    }

    public void setP3_customerNumber(String p3_customerNumber) {
        P3_customerNumber = p3_customerNumber;
    }

    public String getP4_refundOrderId() {
        return P4_refundOrderId;
    }

    public void setP4_refundOrderId(String p4_refundOrderId) {
        P4_refundOrderId = p4_refundOrderId;
    }

    public String getP5_amount() {
        return P5_amount;
    }

    public void setP5_amount(String p5_amount) {
        P5_amount = p5_amount;
    }

    public String getP6_callbackUrl() {
        return P6_callbackUrl;
    }

    public void setP6_callbackUrl(String p6_callbackUrl) {
        P6_callbackUrl = p6_callbackUrl;
    }

    public String getP7_desc() {
        return P7_desc;
    }

    public void setP7_desc(String p7_desc) {
        P7_desc = p7_desc;
    }

    public String getP8_orderSerialNumber() {
        return P8_orderSerialNumber;
    }

    public void setP8_orderSerialNumber(String p8_orderSerialNumber) {
        P8_orderSerialNumber = p8_orderSerialNumber;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayRefundOrderVo{" +
                "P1_bizType='" + P1_bizType + '\'' +
                ", P2_orderId='" + P2_orderId + '\'' +
                ", P3_customerNumber='" + P3_customerNumber + '\'' +
                ", P4_refundOrderId='" + P4_refundOrderId + '\'' +
                ", P5_amount='" + P5_amount + '\'' +
                ", P6_callbackUrl='" + P6_callbackUrl + '\'' +
                ", P7_desc='" + P7_desc + '\'' +
                ", P8_orderSerialNumber='" + P8_orderSerialNumber + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
