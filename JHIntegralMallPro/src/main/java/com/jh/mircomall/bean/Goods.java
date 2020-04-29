package com.jh.mircomall.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Goods implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String goodsName;

	private String goodsLogo;

	private String goodsDetails;

	private String goodsContext;

	private BigDecimal goodsPrice;

	private BigDecimal originalPrice;

	private Goods goods;

	public BigDecimal getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(BigDecimal originalPrice) {
		this.originalPrice = originalPrice;
	}

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public BigDecimal getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(BigDecimal goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	private Integer goodsCoin;

	private Integer goodsNum;

	private Date createTime;

	private Date changeTime;

	private Integer isDelete;

	private Integer status;

	private Integer oodsgTypeId;

	private Integer businessId;

	private Integer merchat;

	private GoodsChildren goodsChildren;

	private GoodsChildrenBrand goodsChildrenBrand;

	private GoodsChildrenBrandStyle goodsChildrenBrandStyle;

	private GoodsParent goodsParent;

	private Business business;

	private Groups groups;

	/*
	 * private Taobao taobao;
	 * 
	 * public Taobao getTaobao() { return taobao; }
	 * 
	 * public void setTaobao(Taobao taobao) { this.taobao = taobao; }
	 */

	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}

	public Business getBusiness() {
		return business;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public GoodsChildren getGoodsChildren() {
		return goodsChildren;
	}

	public void setGoodsChildren(GoodsChildren goodsChildren) {
		this.goodsChildren = goodsChildren;
	}

	public GoodsChildrenBrand getGoodsChildrenBrand() {
		return goodsChildrenBrand;
	}

	public void setGoodsChildrenBrand(GoodsChildrenBrand goodsChildrenBrand) {
		this.goodsChildrenBrand = goodsChildrenBrand;
	}

	public GoodsChildrenBrandStyle getGoodsChildrenBrandStyle() {
		return goodsChildrenBrandStyle;
	}

	public void setGoodsChildrenBrandStyle(GoodsChildrenBrandStyle goodsChildrenBrandStyle) {
		this.goodsChildrenBrandStyle = goodsChildrenBrandStyle;
	}

	public GoodsParent getGoodsParent() {
		return goodsParent;
	}

	public void setGoodsParent(GoodsParent goodsParent) {
		this.goodsParent = goodsParent;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName == null ? null : goodsName.trim();
	}

	public String getGoodsLogo() {
		return goodsLogo;
	}

	public void setGoodsLogo(String goodsLogo) {
		this.goodsLogo = goodsLogo == null ? null : goodsLogo.trim();
	}

	public String getGoodsDetails() {
		return goodsDetails;
	}

	public void setGoodsDetails(String goodsDetails) {
		this.goodsDetails = goodsDetails == null ? null : goodsDetails.trim();
	}

	public String getGoodsContext() {
		return goodsContext;
	}

	public void setGoodsContext(String goodsContext) {
		this.goodsContext = goodsContext == null ? null : goodsContext.trim();
	}

	public Integer getGoodsCoin() {
		return goodsCoin;
	}

	public void setGoodsCoin(Integer goodsCoin) {
		this.goodsCoin = goodsCoin;
	}

	public Integer getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
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

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getOodsgTypeId() {
		return oodsgTypeId;
	}

	public void setOodsgTypeId(Integer oodsgTypeId) {
		this.oodsgTypeId = oodsgTypeId;
	}

	public Integer getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}

	public Integer getMerchat() {
		return merchat;
	}

	public void setMerchat(Integer merchat) {
		this.merchat = merchat;
	}
}