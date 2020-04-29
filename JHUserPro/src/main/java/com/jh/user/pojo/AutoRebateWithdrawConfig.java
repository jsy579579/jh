package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.context.annotation.Configuration;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="t_rebate_withdraw_auto_config")
public class AutoRebateWithdrawConfig {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(name="brand_id")
    private String brandId;

    @Column(name="on_off")
    private Integer onOff;

    @Column(name="update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public Integer getOnOff() {
        return onOff;
    }

    public void setOnOff(Integer onOff) {
        this.onOff = onOff;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
