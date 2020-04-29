package com.jh.paymentgateway.pojo.tl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TLbackInfo {
    @JsonProperty(value = "COM_ID")
    private String comId;     //合作公司ID

    @JsonProperty(value = "USER_INFO_ID")
    private String userInfoId;     //末端商户编号，

    @JsonProperty(value = "ORDER_ID")
    private String orderId;   //平台代付订单号

    @JsonProperty(value = "STATUS")
    private String status;   //状态：0、待出款，2、出款失败；3、出款中；大于等于6、出款成功。

    @JsonProperty(value = "AMOUNT")
    private String amount;      //代付订单金额

    @JsonProperty(value = "LOAN_AMOUNT")
    private String loanAmount; //实际出款金额

    @JsonProperty(value = "IN_ACCOUNT")
    private String inAccount;  //收款账号

    @JsonProperty(value = "IN_NAME")
    private String inName;      //收款账户名

    @JsonProperty(value = "MSG")
    private String msg;         //STATUS=2时，返回错误消息

    @JsonProperty(value = "NONCE_STR")
    private String nonceStr;    // 随机字符串

    @JsonProperty(value = "SIGN")
    private String sign;        //签名


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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getInAccount() {
        return inAccount;
    }

    public void setInAccount(String inAccount) {
        this.inAccount = inAccount;
    }

    public String getInName() {
        return inName;
    }

    public void setInName(String inName) {
        this.inName = inName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }



    @Override
    public String toString() {
        return "TLbackBody{" +
                "comId='" + comId + '\'' +
                ", userInfoId='" + userInfoId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", amount='" + amount + '\'' +
                ", loanAmount='" + loanAmount + '\'' +
                ", inAccount='" + inAccount + '\'' +
                ", inName='" + inName + '\'' +
                ", msg='" + msg + '\'' +
                ", nonceStr='" + nonceStr + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
