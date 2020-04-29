package com.jh.good.pojo;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_goods")
public class Goods {

    @Id
    @Column(name = "id")
    private Long id;    //商品id

    @Column(name = "audit_status")
    private String auditStatus; //状态

    @Column(name = "is_marketable")
    private String isMarketable;    //是否上架 0上架 1下架

    @Column(name = "goods_name")
    private String goodsName;   //商品名

    @Column(name = "caption")
    private String caption; // 商品描述

    @Column(name = "category1_id")
    private Long category1Id;   // 一级分类id

    @Column(name = "category2_id")
    private Long category2Id;   // 二级分类id (暂时没用)

    @Column(name = "small_pic")
    private String smallPic;    // 商品图片

    @Column(name = "price")
    private BigDecimal price;   //  价格

    @Column(name = "integral")
    private Integer integral;   // 积分

    @Column(name = "is_delete")
    private String isDelete;    // 是否删除

    @Column(name = "details")
    private String details;     // 商品详情

    @Column(name = "inventory")
    private Integer inventory;  // 库存

    @Column(name = "market_price")
    private BigDecimal marketPrice; // 市场价格

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getIsMarketable() {
        return isMarketable;
    }

    public void setIsMarketable(String isMarketable) {
        this.isMarketable = isMarketable;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Long getCategory1Id() {
        return category1Id;
    }

    public void setCategory1Id(Long category1Id) {
        this.category1Id = category1Id;
    }

    public Long getCategory2Id() {
        return category2Id;
    }

    public void setCategory2Id(Long category2Id) {
        this.category2Id = category2Id;
    }

    public String getSmallPic() {
        return smallPic;
    }

    public void setSmallPic(String smallPic) {
        this.smallPic = smallPic;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }
}
