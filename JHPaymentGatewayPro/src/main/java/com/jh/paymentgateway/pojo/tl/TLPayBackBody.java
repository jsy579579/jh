package com.jh.paymentgateway.pojo.tl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TLPayBackBody {

    @JsonProperty(value = "ACTION_INFO")
    private TLPayBackInfo tlPayBackInfo;

    @JsonProperty(value = "SIGN")
    private String sign;

    @JsonProperty(value = "ACTION_NAME")
    private String actionName;

    public TLPayBackInfo getTlPayBackInfo() {
        return tlPayBackInfo;
    }

    public void setTlPayBackInfo(TLPayBackInfo tlPayBackInfo) {
        this.tlPayBackInfo = tlPayBackInfo;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "TLPayBackBody{" +
                "tlPayBackInfo=" + tlPayBackInfo +
                ", sign='" + sign + '\'' +
                ", actionName='" + actionName + '\'' +
                '}';
    }
}
