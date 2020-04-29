package com.jh.paymentgateway.util.ap.model;

public class APBindCardBack {
    private String data;

    private String mchtNo;

    private String resultCode;

    private String errorCode;

    private String errorDesc;

    private String signl;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMchtNo() {
        return mchtNo;
    }

    public void setMchtNo(String mchtNo) {
        this.mchtNo = mchtNo;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getSignl() {
        return signl;
    }

    public void setSignl(String signl) {
        this.signl = signl;
    }

    @Override
    public String toString() {
        return "APBindCardBack{" +
                "data='" + data + '\'' +
                ", mchtNo='" + mchtNo + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorDesc='" + errorDesc + '\'' +
                ", signl='" + signl + '\'' +
                '}';
    }
}
