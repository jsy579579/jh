package com.jh.paymentgateway.controller.hqk.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hqk_register")
public class HQKRegister implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "merchant_code")
    private String merchantCode;

    @Column(name = "status")
    private String status;

    @Column(name = "rate")
    private String rate;

    @Column(name = "extra_fee")
    private String extraFee;


    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getBankCard() {
        return bankCard;
    }


    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }


    public String getIdCard() {
        return idCard;
    }


    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }


    public String getMerchantCode() {
        return merchantCode;
    }


    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
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


    public Date getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }



}
