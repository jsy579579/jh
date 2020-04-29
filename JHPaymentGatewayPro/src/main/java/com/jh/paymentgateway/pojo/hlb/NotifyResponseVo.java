package com.jh.paymentgateway.pojo.hlb;



public class NotifyResponseVo {

    private String rt1_customerNumber;
    private String rt2_orderId;
    private String rt3_systemSerial;
    private String rt4_status;
    private String rt5_orderAmount;
    private String rt6_currency;
    private String rt7_timestamp;
    private String rt8_desc;
    private String sign;

    @Override
    public String toString() {
        return "NotifyResponseVo{" +
                "rt1_customerNumber='" + rt1_customerNumber + '\'' +
                ", rt2_orderId='" + rt2_orderId + '\'' +
                ", rt3_systemSerial='" + rt3_systemSerial + '\'' +
                ", rt4_status='" + rt4_status + '\'' +
                ", rt5_orderAmount='" + rt5_orderAmount + '\'' +
                ", rt6_currency='" + rt6_currency + '\'' +
                ", rt7_timestamp='" + rt7_timestamp + '\'' +
                ", rt8_desc='" + rt8_desc + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }

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

    public String getRt3_systemSerial() {
        return rt3_systemSerial;
    }

    public void setRt3_systemSerial(String rt3_systemSerial) {
        this.rt3_systemSerial = rt3_systemSerial;
    }

    public String getRt4_status() {
        return rt4_status;
    }

    public void setRt4_status(String rt4_status) {
        this.rt4_status = rt4_status;
    }

    public String getRt5_orderAmount() {
        return rt5_orderAmount;
    }

    public void setRt5_orderAmount(String rt5_orderAmount) {
        this.rt5_orderAmount = rt5_orderAmount;
    }

    public String getRt6_currency() {
        return rt6_currency;
    }

    public void setRt6_currency(String rt6_currency) {
        this.rt6_currency = rt6_currency;
    }

    public String getRt7_timestamp() {
        return rt7_timestamp;
    }

    public void setRt7_timestamp(String rt7_timestamp) {
        this.rt7_timestamp = rt7_timestamp;
    }

    public String getRt8_desc() {
        return rt8_desc;
    }

    public void setRt8_desc(String rt8_desc) {
        this.rt8_desc = rt8_desc;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
