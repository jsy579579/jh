package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class GoodsChildrenBrandStyle implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer id;

    private String styleName;

    private Integer goodsBrandId;

    private Date createTime;

    private Date changeTime;

    private Integer status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName == null ? null : styleName.trim();
    }

    public Integer getGoodsBrandId() {
        return goodsBrandId;
    }

    public void setGoodsBrandId(Integer goodsBrandId) {
        this.goodsBrandId = goodsBrandId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Date changeTime) {
        this.changeTime = changeTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}