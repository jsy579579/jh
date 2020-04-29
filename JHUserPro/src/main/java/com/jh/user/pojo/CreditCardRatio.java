package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="t_credit_card_distri_ratio")
public class CreditCardRatio {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="pre_level")
    private String preLevel;

    @Column(name="credit_ratio")
    private BigDecimal creditRatio;

    @Column(name="brand_id")
    private String brandId;

    @Column(name="create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getPreLevel() {
        return preLevel;
    }

    public void setPreLevel(String preLevel) {
        this.preLevel = preLevel;
    }

    public BigDecimal getCreditRatio() {
        return creditRatio;
    }

    public void setCreditRatio(BigDecimal creditRatio) {
        this.creditRatio = creditRatio;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}