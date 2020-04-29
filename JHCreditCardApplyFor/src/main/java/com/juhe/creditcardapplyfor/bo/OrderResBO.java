package com.juhe.creditcardapplyfor.bo;


import java.io.Serializable;

/**
 * @author huhao
 * @title: OrderResBO
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/22 00229:04
 */


public class OrderResBO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientNo;        //客户端编号
    private String tradeNo;         //交易号
    private Integer userPrice;          //分润金额
    private String message;         //执行消息
    private String callbackType;    //回调类型
    private String sign;            //签名
    private String timestamp;       //时间戳

    @Override
    public String toString() {
        return "OrderResBO{" +
                "clientNo='" + clientNo + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
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

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
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
