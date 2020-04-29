package com.jh.paymentgateway.pojo.kft;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description 快付通订单
 */
@Entity
@Table(name = "t_kft_order")
public class KFTOrder implements Serializable {
    private static final long serialVersionUID = 7178356037143320223L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "kft_order_no")
    private String kftOrderNo;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "extra_fee")
    private String extraFee;

    @Column(name = "expired_time")
    private String expiredTime;

    @Column(name = "security_code")
    private String securityCode;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKftOrderNo() {
        return kftOrderNo;
    }

    public void setKftOrderNo(String kftOrderNo) {
        this.kftOrderNo = kftOrderNo;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(String extraFee) {
        this.extraFee = extraFee;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
