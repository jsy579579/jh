package com.jh.channel.pojo;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "t_channel_security_key")
public class OutMerchantSecurityKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "phone")
    private String phone;
    
    @Column(name = "user_id")
    private long userId;

    @Column(name = "key")
    private String key;

    @Column(name = "brand_id")
    private String brandId;

    
    @Column(name = "update_time")
    private Date updateTime;
    
    @Column(name = "create_time")
    private Date createTime;


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public Date getCreateTime() {
        return createTime;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getBrandId() {
        return brandId;
    }


    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }


	public long getUserId() {
		return userId;
	}


	public void setUserId(long userId) {
		this.userId = userId;
	}


	public Date getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
