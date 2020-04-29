package com.juhe.creditcardapplyfor.bo;



import java.io.Serializable;

/**
 * @author Administrator
 * @title: LoanBO
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002310:48
 */

public class LoanBO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String mobile;
    private Long oemChannelId;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getOemChannelId() {
        return oemChannelId;
    }

    public void setOemChannelId(Long oemChannelId) {
        this.oemChannelId = oemChannelId;
    }

    @Override
    public String toString() {
        return "LoanBO{" +
                "mobile='" + mobile + '\'' +
                ", oemChannelId=" + oemChannelId +
                '}';
    }
}
