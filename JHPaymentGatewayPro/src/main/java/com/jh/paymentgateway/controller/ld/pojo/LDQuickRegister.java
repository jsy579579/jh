package com.jh.paymentgateway.controller.ld.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "t_ldquick_register")
public class LDQuickRegister implements Serializable {

    private static final long serialVersionUID = -7403597263484121208L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "idcard")
    private String idCard;

    @Column(name = "phone")
    private String phone;

    @Column(name = "merchant_no")
    private String merchantNo;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "extra_fee")
    private String extraFee;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(String extraFee) {
        this.extraFee = extraFee;
    }

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Column(name = "update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "YKRegister [id=" + id + ", bankCard=" + bankCard + ", realName=" + realName + ", idCard=" + idCard
                + ", phone=" + phone + ", merchantNo=" + merchantNo + ", createTime=" + createTime + ", updateTime="
                + updateTime + "]";
    }

}
