package com.jh.paymentgateway.pojo.hlb;



public class QueryOrderVo {

    private String P1_bizType;
    private String P2_orderId;
    private String P3_customerNumber;
    private String P4_serialNumber;
//    private String P5_queryRefundStatus;
    private String sign;

    @Override
    public String toString() {
        return "QueryOrderVo{" +
                "P1_bizType='" + P1_bizType + '\'' +
                ", P2_orderId='" + P2_orderId + '\'' +
                ", P3_customerNumber='" + P3_customerNumber + '\'' +
                ", P4_serialNumber='" + P4_serialNumber + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }

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
}
