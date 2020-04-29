package com.jh.paymentgateway.pojo.tl;





import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TLbackBody implements Serializable {

    @JsonProperty(value = "ACTION_INFO")
    private TLbackInfo tLbackInfo;

    @JsonProperty(value = "ACTION_NAME")
    private String action_name;

    public TLbackInfo gettLbackInfo() {
        return tLbackInfo;
    }

    public void settLbackInfo(TLbackInfo tLbackInfo) {
        this.tLbackInfo = tLbackInfo;
    }


    public String getAction_name() {
        return action_name;
    }

    public void setAction_name(String action_name) {
        this.action_name = action_name;
    }

    @Override
    public String toString() {
        return "TLbackBody{" +
                "tLbackInfo=" + tLbackInfo +
                ", action_name='" + action_name + '\'' +
                '}';
    }
}
