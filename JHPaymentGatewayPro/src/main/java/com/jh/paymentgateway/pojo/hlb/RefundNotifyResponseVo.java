package com.jh.paymentgateway.pojo.hlb;



public class RefundNotifyResponseVo {

    private String rt1_customerNumber;
    private String rt2_orderId;
    private String rt3_refundOrderId;
    private String rt4_systemSerial;
    private String rt5_status;
    private String rt6_amount;
    private String rt7_currency;
    private String rt8_timestamp;
    private String sign;

    public String getRt1_customerNumber() {
        return rt1_customerNumber;
    }

    public void setRt1_customerNumber(String rt1_customerNumber) {
        this.rt1_customerNumber = rt1_customerNumber;
    }

    public String getRt2_orderId() {
        return rt2_orderId;
    }

    public void setRt2_orderId(String rt2_orderId) {
        this.rt2_orderId = rt2_orderId;
    }

    public String getRt3_refundOrderId() {
        return rt3_refundOrderId;
    }

    public void setRt3_refundOrderId(String rt3_refundOrderId) {
        this.rt3_refundOrderId = rt3_refundOrderId;
    }

    public String getRt4_systemSerial() {
        return rt4_systemSerial;
    }

    public void setRt4_systemSerial(String rt4_systemSerial) {
        this.rt4_systemSerial = rt4_systemSerial;
    }

    public String getRt5_status() {
        return rt5_status;
    }

    public void setRt5_status(String rt5_status) {
        this.rt5_status = rt5_status;
    }

    public String getRt6_amount() {
        return rt6_amount;
    }

    public void setRt6_amount(String rt6_amount) {
        this.rt6_amount = rt6_amount;
    }

    public String getRt7_currency() {
        return rt7_currency;
    }

    public void setRt7_currency(String rt7_currency) {
        this.rt7_currency = rt7_currency;
    }

    public String getRt8_timestamp() {
        return rt8_timestamp;
    }

    public void setRt8_timestamp(String rt8_timestamp) {
        this.rt8_timestamp = rt8_timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "RefundNotifyResponseVo{" +
                "rt1_customerNumber='" + rt1_customerNumber + '\'' +
                ", rt2_orderId='" + rt2_orderId + '\'' +
                ", rt3_refundOrderId='" + rt3_refundOrderId + '\'' +
                ", rt4_systemSerial='" + rt4_systemSerial + '\'' +
                ", rt5_status='" + rt5_status + '\'' +
                ", rt6_amount='" + rt6_amount + '\'' +
                ", rt7_currency='" + rt7_currency + '\'' +
                ", rt8_timestamp='" + rt8_timestamp + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
