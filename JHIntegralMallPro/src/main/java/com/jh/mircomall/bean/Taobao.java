package com.jh.mircomall.bean;

import java.io.Serializable;

public class Taobao implements Serializable{

	@Override
	public String toString() {
		return "Taobao [id=" + id + ", level=" + level + ", parentId=" + parentId + ", zh=" + zh + ", ru=" + ru
				+ ", pt=" + pt + ", en=" + en + ", sort=" + sort + ", catid=" + catid + ", catidUse=" + catidUse
				+ ", query=" + query + ", queryUse=" + queryUse + ", weight=" + weight + ", status=" + status + "]";
	}

	private static final long serialVersionUID = 1L;

	private Integer id;

    private Byte level;

    private Integer parentId;

    private String zh;

    private String ru;

    private String pt;

    private String en;

    private Integer sort;

    private Integer catid;

    private Byte catidUse;

    private String query;

    private Byte queryUse;

    private Float weight;

    private Byte status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Byte getLevel() {
        return level;
    }

    public void setLevel(Byte level) {
        this.level = level;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh == null ? null : zh.trim();
    }

    public String getRu() {
        return ru;
    }

    public void setRu(String ru) {
        this.ru = ru == null ? null : ru.trim();
    }

    public String getPt() {
        return pt;
    }

    public void setPt(String pt) {
        this.pt = pt == null ? null : pt.trim();
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en == null ? null : en.trim();
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getCatid() {
        return catid;
    }

    public void setCatid(Integer catid) {
        this.catid = catid;
    }

    public Byte getCatidUse() {
        return catidUse;
    }

    public void setCatidUse(Byte catidUse) {
        this.catidUse = catidUse;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? null : query.trim();
    }

    public Byte getQueryUse() {
        return queryUse;
    }

    public void setQueryUse(Byte queryUse) {
        this.queryUse = queryUse;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}