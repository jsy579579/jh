package com.jh.paymentgateway.controller.tldh.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_tlt_bindcard")
public class TLBindCard implements Serializable {

    private static final long serialVersionUID = -2262648605812751307L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "id_card")
    private String idCard;
    @Column(name = "phone")
    private String phone;
    @Column(name = "bank_card")
    private String bankCard;
    @Column(name = "expired_time")
    private String expiredTime;
    @Column(name = "security_code")
    private String securityCode;
    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Override
    public String toString() {
        return "TLBindCard{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", idCard='" + idCard + '\'' +
                ", phone='" + phone + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", expiredTime='" + expiredTime + '\'' +
                ", securityCode='" + securityCode + '\'' +
                ", status='" + status + '\'' +
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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
