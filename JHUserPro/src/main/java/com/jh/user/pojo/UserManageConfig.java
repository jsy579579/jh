package com.jh.user.pojo;


import com.alibaba.druid.support.monitor.annotation.MTable;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="t_user_manage_config")
public class UserManageConfig {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name="brand_id")
    private Long brandId;

    @Column(name="rate")
    private BigDecimal rate = BigDecimal.ZERO.setScale(3);

    @Column(name="status")
    private int status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
