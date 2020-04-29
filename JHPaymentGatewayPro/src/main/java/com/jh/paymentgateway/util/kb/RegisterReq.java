package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;

/**
 * 注册请求.
 *
 * @author creasy
 * @date 2018-08-14.
 */
public class RegisterReq extends AbstractReq {

    private String mobile;

    private String idNo;

    private String customerName;

    private String bankCardNo;

    private String province;

    private String city;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }


    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
