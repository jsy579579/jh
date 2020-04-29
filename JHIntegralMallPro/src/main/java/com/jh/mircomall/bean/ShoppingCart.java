package com.jh.mircomall.bean;

import java.io.Serializable;
import java.util.Date;

public class ShoppingCart implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer goodsId;

	private Integer userId;

	private String goodsLogo;

	private String goodsPrice;

	private Integer businessId;

	private Integer goodsNum;

	private Integer isDelete;

	private Goods goods;

	private Integer goodsCoin;

	public Integer getGoodsCoin() {
		return goodsCoin;
	}

	public void setGoodsCoin(Integer goodsCoin) {
		this.goodsCoin = goodsCoin;
	}

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	public Integer getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}

	private Date createTime;

	private Date changeTime;

	private Integer status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getGoodsLogo() {
		return goodsLogo;
	}

	public void setGoodsLogo(String goodsLogo) {
		this.goodsLogo = goodsLogo == null ? null : goodsLogo.trim();
	}

	public String getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(String goodsPrice) {
		this.goodsPrice = goodsPrice == null ? null : goodsPrice.trim();
	}

	public Integer getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
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