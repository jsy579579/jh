package com.jh.paymentgateway.pojo.xkdhd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_xkdhd_register")
public class XKDHDRegister implements Serializable {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name ="order_no")
    private String orderNo;


    @Column(name ="bank_card")
    private String bankCard;

    @Column(name ="bank_type")
    private String bankType;

    @Column(name ="phone")
    private String phone;

    @Column(name ="id_card")
    private String idCard;

    @Column(name ="user_name")
    private String userName;

    @Column(name ="expired_time")
    private String expiredTime;

    @Column(name ="security_code")
    private String securityCode;
    @Column(name="rate")
    private String rate;

    @Column(name ="extra_fee")
    private String extraFee;

    @Column(name="sms_seq")
    private String smsSeq;

    @Column(name ="create_time")
    private Date createTime;



    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
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

    public String getSmsSeq() {
        return smsSeq;
    }

    public void setSmsSeq(String smsSeq) {
        this.smsSeq = smsSeq;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
