package com.jh.user.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "t_user_manage_history")
public class UserManageHistory implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name="order_code")
    private String orderCode;
    @Column(name="amount")
    private BigDecimal amount;
    @Column(name = "first_user_id")
    private Long firstUserId;
    @Column(name = "first_user_phone")
    private String firstUserPhone;
    @Column(name = "first_user_grade")
    private Integer firstUserGrade = 0;
    @Column(name = "pre_user_id")
    private Long preUserId;
    @Column(name = "pre_user_phone")
    private String preUserPhone;
    @Column(name = "pre_user_grade")
    private Integer preUserGrade = 0;
    @Column(name = "level")
    private Integer level = 0;
    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime = new Date();
    @Column(name="status")
    private String status;
    @Transient
    private String brandId;
    @Transient
    private String preUserName;
    @Transient
    private String firUserName;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(Long firstUserId) {
        this.firstUserId = firstUserId;
    }

    public String getFirstUserPhone() {
        return firstUserPhone;
    }

    public void setFirstUserPhone(String firstUserPhone) {
        this.firstUserPhone = firstUserPhone;
    }

    public Integer getFirstUserGrade() {
        return firstUserGrade;
    }

    public void setFirstUserGrade(Integer firstUserGrade) {
        this.firstUserGrade = firstUserGrade;
    }

    public Long getPreUserId() {
        return preUserId;
    }

    public void setPreUserId(Long preUserId) {
        this.preUserId = preUserId;
    }

    public String getPreUserPhone() {
        return preUserPhone;
    }

    public void setPreUserPhone(String preUserPhone) {
        this.preUserPhone = preUserPhone;
    }

    public Integer getPreUserGrade() {
        return preUserGrade;
    }

    public void setPreUserGrade(Integer preUserGrade) {
        this.preUserGrade = preUserGrade;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getPreUserName() {
        return preUserName;
    }

    public void setPreUserName(String preUserName) {
        this.preUserName = preUserName;
    }

    public String getFirUserName() {
        return firUserName;
    }

    public void setFirUserName(String firUserName) {
        this.firUserName = firUserName;
    }

    @Override
    public String toString() {
        return "UserManageHistory{" +
                "id=" + id +
                ", orderCode='" + orderCode + '\'' +
                ", amount=" + amount +
                ", firstUserId=" + firstUserId +
                ", firstUserPhone='" + firstUserPhone + '\'' +
                ", firstUserGrade=" + firstUserGrade +
                ", preUserId=" + preUserId +
                ", preUserPhone='" + preUserPhone + '\'' +
                ", preUserGrade=" + preUserGrade +
                ", level=" + level +
                ", createTime=" + createTime +
                ", brandId='" + brandId + '\'' +
                ", preUserName='" + preUserName + '\'' +
                ", firUserName='" + firUserName + '\'' +
                '}';
    }
}
