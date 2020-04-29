package com.juhe.creditcardapplyfor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;


import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="t_amount_dis_ratio_config")
public class AmountDisRatioConfig {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="desc")
    private String desc;

    @Column(name="amount_dis_ratio")
    private BigDecimal amountDisRatio;

    @Column(name="brand_id")
    private String brandId;

    @Column(name="create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    @Override
    public String toString() {
        return "AmountDisRatioConfig{" +
                "id=" + id +
                ", desc='" + desc + '\'' +
                ", amountDisRatio=" + amountDisRatio +
                ", brandId='" + brandId + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BigDecimal getAmountDisRatio() {
        return amountDisRatio;
    }

    public void setAmountDisRatio(BigDecimal amountDisRatio) {
        this.amountDisRatio = amountDisRatio;
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
