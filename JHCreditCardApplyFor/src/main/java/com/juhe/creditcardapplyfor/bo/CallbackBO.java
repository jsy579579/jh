package com.juhe.creditcardapplyfor.bo;



import java.io.Serializable;

/**
 * @author Administrator
 * @title: CallbackBO
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002312:06
 */

public class CallbackBO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String clientNo;
    private String transNo;
    private Integer amount;
    private Integer userPrice;
    private String message;
    private String callbackType;
    private String  sign;
    private String timestamp;

    @Override
    public String toString() {
        return "CallbackBO{" +
                "clientNo='" + clientNo + '\'' +
                ", transNo='" + transNo + '\'' +
                ", amount=" + amount +
                ", userPrice=" + userPrice +
                ", message='" + message + '\'' +
                ", callbackType='" + callbackType + '\'' +
                ", sign='" + sign + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public String getTransNo() {
        return transNo;
    }

    public void setTransNo(String transNo) {
        this.transNo = transNo;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getUserPrice() {
        return userPrice;
    }

    public void setUserPrice(Integer userPrice) {
        this.userPrice = userPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCallbackType() {
        return callbackType;
    }

    public void setCallbackType(String callbackType) {
        this.callbackType = callbackType;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
