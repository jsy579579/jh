package com.jh.paymentgateway.pojo.hlb;


public class AppPayRefundQueryVo {


    private String P1_bizType;
    private String P2_refundOrderId;
    private String P3_customerNumber;
    private String P4_serialNumber;
    private String sign;

    public String getP1_bizType() {
        return P1_bizType;
    }

    public void setP1_bizType(String p1_bizType) {
        P1_bizType = p1_bizType;
    }

    public String getP2_refundOrderId() {
        return P2_refundOrderId;
    }

    public void setP2_refundOrderId(String p2_refundOrderId) {
        P2_refundOrderId = p2_refundOrderId;
    }

    public String getP3_customerNumber() {
        return P3_customerNumber;
    }

    public void setP3_customerNumber(String p3_customerNumber) {
        P3_customerNumber = p3_customerNumber;
    }

    public String getP4_serialNumber() {
        return P4_serialNumber;
    }

    public void setP4_serialNumber(String p4_serialNumber) {
        P4_serialNumber = p4_serialNumber;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayRefundQueryVo{" +
                "P1_bizType='" + P1_bizType + '\'' +
                ", P2_refundOrderId='" + P2_refundOrderId + '\'' +
                ", P3_customerNumber='" + P3_customerNumber + '\'' +
                ", P4_serialNumber='" + P4_serialNumber + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
