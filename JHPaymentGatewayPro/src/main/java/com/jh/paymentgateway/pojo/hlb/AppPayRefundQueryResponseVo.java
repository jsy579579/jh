package com.jh.paymentgateway.pojo.hlb;


public class AppPayRefundQueryResponseVo {


    private String rt1_bizType;
    private String rt2_retCode;
    private String rt3_retMsg;
    private String rt4_customerNumber;
    private String rt5_orderId;
    private String rt6_refundOrderNum;
    private String rt7_serialNumber;
    private String rt8_orderStatus;
    private String rt9_amount;
    private String rt10_currency;

    private String rt11_refundOrderCompleteDate;
    private String rt12_refundChannelOrderNum;
    private String rt13_desc;
    private String rt14_refundOrderAttribute;
    private String rt15_appPayType;
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

    public String getRt8_orderStatus() {
        return rt8_orderStatus;
    }

    public void setRt8_orderStatus(String rt8_orderStatus) {
        this.rt8_orderStatus = rt8_orderStatus;
    }

    public String getRt9_amount() {
        return rt9_amount;
    }

    public void setRt9_amount(String rt9_amount) {
        this.rt9_amount = rt9_amount;
    }

    public String getRt10_currency() {
        return rt10_currency;
    }

    public void setRt10_currency(String rt10_currency) {
        this.rt10_currency = rt10_currency;
    }

    public String getRt11_refundOrderCompleteDate() {
        return rt11_refundOrderCompleteDate;
    }

    public void setRt11_refundOrderCompleteDate(String rt11_refundOrderCompleteDate) {
        this.rt11_refundOrderCompleteDate = rt11_refundOrderCompleteDate;
    }

    public String getRt12_refundChannelOrderNum() {
        return rt12_refundChannelOrderNum;
    }

    public void setRt12_refundChannelOrderNum(String rt12_refundChannelOrderNum) {
        this.rt12_refundChannelOrderNum = rt12_refundChannelOrderNum;
    }

    public String getRt13_desc() {
        return rt13_desc;
    }

    public void setRt13_desc(String rt13_desc) {
        this.rt13_desc = rt13_desc;
    }

    public String getRt14_refundOrderAttribute() {
        return rt14_refundOrderAttribute;
    }

    public void setRt14_refundOrderAttribute(String rt14_refundOrderAttribute) {
        this.rt14_refundOrderAttribute = rt14_refundOrderAttribute;
    }

    public String getRt15_appPayType() {
        return rt15_appPayType;
    }

    public void setRt15_appPayType(String rt15_appPayType) {
        this.rt15_appPayType = rt15_appPayType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayRefundQueryResponseVo{" +
                "rt1_bizType='" + rt1_bizType + '\'' +
                ", rt2_retCode='" + rt2_retCode + '\'' +
                ", rt3_retMsg='" + rt3_retMsg + '\'' +
                ", rt4_customerNumber='" + rt4_customerNumber + '\'' +
                ", rt5_orderId='" + rt5_orderId + '\'' +
                ", rt6_refundOrderNum='" + rt6_refundOrderNum + '\'' +
                ", rt7_serialNumber='" + rt7_serialNumber + '\'' +
                ", rt8_orderStatus='" + rt8_orderStatus + '\'' +
                ", rt9_amount='" + rt9_amount + '\'' +
                ", rt10_currency='" + rt10_currency + '\'' +
                ", rt11_refundOrderCompleteDate='" + rt11_refundOrderCompleteDate + '\'' +
                ", rt12_refundChannelOrderNum='" + rt12_refundChannelOrderNum + '\'' +
                ", rt13_desc='" + rt13_desc + '\'' +
                ", rt14_refundOrderAttribute='" + rt14_refundOrderAttribute + '\'' +
                ", rt15_appPayType='" + rt15_appPayType + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
