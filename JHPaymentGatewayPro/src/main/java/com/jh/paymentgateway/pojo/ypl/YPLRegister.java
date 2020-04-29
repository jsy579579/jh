package com.jh.paymentgateway.pojo.ypl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhangchaofeng
 * @date 2019/4/22
 * @description 快付通进件信息
 */
@Table(name = "t_ypl_register")
@Entity
public class YPLRegister implements Serializable {






    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "rate")
    private String rate;

    @Column(name = "extra_fee")
    private String extraFee;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "security_code")
    private String securityCode;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "expired_time")
    private String expiredTime;

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(String extraFee) {
        this.extraFee = extraFee;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }
}
