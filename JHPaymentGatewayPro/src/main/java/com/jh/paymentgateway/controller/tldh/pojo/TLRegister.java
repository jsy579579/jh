package com.jh.paymentgateway.controller.tldh.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "t_tlt_register")
public class TLRegister implements Serializable {

    private static final long serialVersionUID = 7780886155900868280L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Column(name = "user_name")
    private String userName;
    @Column(name = "idcard")
    private String idcard;
    @Column(name = "bank_card")
    private String bankCard;
    @Column(name = "phone")
    private String phone;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "rate")
    private String rate;
    @Column(name = "extra_fee")
    private String extraFee;
    @Column(name = "merchant_code")
    private String merchantCode;
    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Override
    public String toString() {
        return "TLRegister{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", idcard='" + idcard + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", phone='" + phone + '\'' +
                ", bankName='" + bankName + '\'' +
                ", rate='" + rate + '\'' +
                ", extraFee='" + extraFee + '\'' +
                ", merchantCode='" + merchantCode + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
