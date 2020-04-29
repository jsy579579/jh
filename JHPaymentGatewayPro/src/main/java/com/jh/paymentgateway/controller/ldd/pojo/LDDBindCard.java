package com.jh.paymentgateway.controller.ldd.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_ldd_bindcard")
public class LDDBindCard implements Serializable {

    private static final long serialVersionUID = 3754452396822019305L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "idcard")
    private String idCard;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "phone")
    private String phone;

    @Column(name = "security_code")
    private String securityCode;

    @Column(name = "expired_time")
    private String expiredTime;

    @Column(name = "rate")
    private String rate;

    @Column(name = "extra_fee")
    private String extraFee;

    @Column(name = "status")
    private String status;

    @Column(name = "order_num")
    private String orderNum;

    @Column(name = "merchant_no")
    private String merchantNo;

    @Column(name = "update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "LDDBindCard{" +
                "id=" + id +
                ", realName='" + realName + '\'' +
                ", idCard='" + idCard + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", phone='" + phone + '\'' +
                ", securityCode='" + securityCode + '\'' +
                ", expiredTime='" + expiredTime + '\'' +
                ", rate='" + rate + '\'' +
                ", extraFee='" + extraFee + '\'' +
                ", status='" + status + '\'' +
                ", orderNum='" + orderNum + '\'' +
                ", merchantNo='" + merchantNo + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
