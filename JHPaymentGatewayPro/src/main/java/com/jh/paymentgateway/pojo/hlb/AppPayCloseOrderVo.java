package com.jh.paymentgateway.pojo.hlb;


public class AppPayCloseOrderVo {


    private String P1_bizType;
    private String P2_orderId;
    private String P3_customerNumber;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayCloseOrderVo{" +
                "P1_bizType='" + P1_bizType + '\'' +
                ", P2_orderId='" + P2_orderId + '\'' +
                ", P3_customerNumber='" + P3_customerNumber + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
