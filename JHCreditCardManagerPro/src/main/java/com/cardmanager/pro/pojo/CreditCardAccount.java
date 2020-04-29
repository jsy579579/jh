package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_credit_card_account", indexes = {
        @Index(columnList = "user_id,credit_card_number,version", name = "idx_ucv", unique = true),
        @Index(columnList = "user_id", name = "idx_userid"),
        @Index(columnList = "credit_card_number", name = "idx_credit_card_number"),
        @Index(columnList = "blance", name = "idx_blance"),
        @Index(columnList = "version", name = "idx_version")
})
public class CreditCardAccount implements Serializable {

    public CreditCardAccount() {
        this.createTime = new Date();
    }

    /**
     *
     */
    private static final long serialVersionUID = -1123305685465627694L;
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "credit_card_number")
    private String creditCardNumber;
    @Column(name = "blance", scale = 2)
    private BigDecimal blance = BigDecimal.ZERO;
    @Column(name = "freeze_blance", scale = 2)
    private BigDecimal freezeBlance = BigDecimal.ZERO;
    @Column(name = "credit_blance", scale = 2)
    private BigDecimal creditBlance = BigDecimal.ZERO;
    @Column(name = "phone")
    private String phone;
    @Column(name = "brand_id")
    private String brandId;
    @Column(name = "bill_date")
    private Integer billDate = 0;
    @Column(name = "repayment_date")
    private Integer repaymentDate = 0;
    @Column(name = "version")
    private String version = "1";
    @Column(name = "last_update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;
    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Transient
    private String bankName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public BigDecimal getBlance() {
        return blance;
    }

    public void setBlance(BigDecimal blance) {
        this.blance = blance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getCreditBlance() {
        return creditBlance;
    }

    public void setCreditBlance(BigDecimal creditBlance) {
        this.creditBlance = creditBlance;
    }

    public Integer getBillDate() {
        return billDate;
    }

    public void setBillDate(Integer billDate) {
        this.billDate = billDate;
    }

    public Integer getRepaymentDate() {
        return repaymentDate;
    }

    public void setRepaymentDate(Integer repaymentDate) {
        this.repaymentDate = repaymentDate;
    }

    public BigDecimal getFreezeBlance() {
        return freezeBlance;
    }

    public void setFreezeBlance(BigDecimal freezeBlance) {
        this.freezeBlance = freezeBlance;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    @Override
    public String toString() {
        return "CreditCardAccount [id=" + id + ", userId=" + userId + ", creditCardNumber=" + creditCardNumber
                + ", blance=" + blance + ", freezeBlance=" + freezeBlance + ", creditBlance=" + creditBlance
                + ", phone=" + phone + ", brandId=" + brandId + ", billDate=" + billDate + ", repaymentDate="
                + repaymentDate + ", version=" + version + ", lastUpdateTime=" + lastUpdateTime + ", createTime="
                + createTime + ", bankName=" + bankName + "]";
    }

}
