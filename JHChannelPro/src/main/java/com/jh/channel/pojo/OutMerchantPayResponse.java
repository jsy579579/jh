package com.jh.channel.pojo;


import java.io.Serializable;


public class OutMerchantPayResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String data;

    private String sign;

    private String merchant_id;


    public String getData() {
        return data;
    }


    public void setData(String data) {
        this.data = data;
    }


    public String getSign() {
        return sign;
    }


    public void setSign(String sign) {
        this.sign = sign;
    }


    public String getMerchant_id() {
        return merchant_id;
    }


    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

}
