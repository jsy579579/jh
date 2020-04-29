package com.jh.paymentgateway.pojo.hlb;


public class AppPayPublicOrderResponseVo {


    private String rt1_bizType;
    private String rt2_retCode;
    private String rt3_retMsg;
    private String rt4_customerNumber;
    private String rt5_orderId;
    private String rt6_serialNumber;
    private String rt7_payType;
    private String rt8_appid;
    private String rt9_tokenId;
    private String rt10_payInfo;
    private String rt11_orderAmount;
    private String rt12_currency;
    private String rt13_channelRetCode;
    private String rt14_appPayType;
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

    public String getRt6_serialNumber() {
        return rt6_serialNumber;
    }

    public void setRt6_serialNumber(String rt6_serialNumber) {
        this.rt6_serialNumber = rt6_serialNumber;
    }

    public String getRt7_payType() {
        return rt7_payType;
    }

    public void setRt7_payType(String rt7_payType) {
        this.rt7_payType = rt7_payType;
    }

    public String getRt8_appid() {
        return rt8_appid;
    }

    public void setRt8_appid(String rt8_appid) {
        this.rt8_appid = rt8_appid;
    }

    public String getRt9_tokenId() {
        return rt9_tokenId;
    }

    public void setRt9_tokenId(String rt9_tokenId) {
        this.rt9_tokenId = rt9_tokenId;
    }

    public String getRt10_payInfo() {
        return rt10_payInfo;
    }

    public void setRt10_payInfo(String rt10_payInfo) {
        this.rt10_payInfo = rt10_payInfo;
    }

    public String getRt11_orderAmount() {
        return rt11_orderAmount;
    }

    public void setRt11_orderAmount(String rt11_orderAmount) {
        this.rt11_orderAmount = rt11_orderAmount;
    }

    public String getRt12_currency() {
        return rt12_currency;
    }

    public void setRt12_currency(String rt12_currency) {
        this.rt12_currency = rt12_currency;
    }

    public String getRt13_channelRetCode() {
        return rt13_channelRetCode;
    }

    public void setRt13_channelRetCode(String rt13_channelRetCode) {
        this.rt13_channelRetCode = rt13_channelRetCode;
    }

    public String getRt14_appPayType() {
        return rt14_appPayType;
    }

    public void setRt14_appPayType(String rt14_appPayType) {
        this.rt14_appPayType = rt14_appPayType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "AppPayPublicOrderResponseVo{" +
                "rt1_bizType='" + rt1_bizType + '\'' +
                ", rt2_retCode='" + rt2_retCode + '\'' +
                ", rt3_retMsg='" + rt3_retMsg + '\'' +
                ", rt4_customerNumber='" + rt4_customerNumber + '\'' +
                ", rt5_orderId='" + rt5_orderId + '\'' +
                ", rt6_serialNumber='" + rt6_serialNumber + '\'' +
                ", rt7_payType='" + rt7_payType + '\'' +
                ", rt8_appid='" + rt8_appid + '\'' +
                ", rt9_tokenId='" + rt9_tokenId + '\'' +
                ", rt10_payInfo='" + rt10_payInfo + '\'' +
                ", rt11_orderAmount='" + rt11_orderAmount + '\'' +
                ", rt12_currency='" + rt12_currency + '\'' +
                ", rt13_channelRetCode='" + rt13_channelRetCode + '\'' +
                ", rt14_appPayType='" + rt14_appPayType + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
