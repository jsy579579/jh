package com.jh.paymentgateway.pojo.tl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TLPayBackInfo {
    @JsonProperty(value = "PAY_RESULT")
    private String payResult;           //支付结果，0表示成功，其他则失败

    @JsonProperty(value = "PAY_INFO")
    private String payInfo;             //支付结果信息，支付成功时为空

    @JsonProperty(value = "TRANSACTION_ID")
    private String transactionId;       //移动支付平台订单号

    @JsonProperty(value = "OUT_TRADE_NO")
    private String outTradeNo;      //合作公司订单号

    @JsonProperty(value = "AMOUNT")
    private String amount;          //总金额

    @JsonProperty(value = "GMT_PAYMENT")
    private String gmtPayment;      //支付完成时间，格式为yyyyMMddHHmmss

    @JsonProperty(value = "COM_ID")
    private String comId;           //合作公司ID

    @JsonProperty(value = "USER_INFO_ID")
    private String userInfoId;      //末端商户编号

    @JsonProperty(value = "NONCE_STR")
    private String nonceStr;        //随机字符串

    public String getPayResult() {
        return payResult;
    }

    public void setPayResult(String payResult) {
        this.payResult = payResult;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getGmtPayment() {
        return gmtPayment;
    }

    public void setGmtPayment(String gmtPayment) {
        this.gmtPayment = gmtPayment;
    }

    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
    }

    public String getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(String userInfoId) {
        this.userInfoId = userInfoId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    @Override
    public String toString() {
        return "TLPayBackInfo{" +
                "payResult='" + payResult + '\'' +
                ", payInfo='" + payInfo + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", outTradeNo='" + outTradeNo + '\'' +
                ", amount='" + amount + '\'' +
                ", gmtPayment='" + gmtPayment + '\'' +
                ", comId='" + comId + '\'' +
                ", userInfoId='" + userInfoId + '\'' +
                ", nonceStr='" + nonceStr + '\'' +
                '}';
    }
}
