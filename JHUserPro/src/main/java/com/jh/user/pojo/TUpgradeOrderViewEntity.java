package com.jh.user.pojo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_upgrade_order_View")
public class TUpgradeOrderViewEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "order_name")
    private String orderName;

    @Column(name = "order_img_uri")
    private String orderImgUri;

    @Column(name = "order_price")
    private String orderPrice;

    @Column(name = "order_detail_url")
    private String orderDetailUrl;

    @Column(name = "brand_id")
    private Long brandId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getOrderImgUri() {
        return orderImgUri;
    }

    public void setOrderImgUri(String orderImgUri) {
        this.orderImgUri = orderImgUri;
    }

    public String getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(String orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getOrderDetailUrl() {
        return orderDetailUrl;
    }

    public void setOrderDetailUrl(String orderDetailUrl) {
        this.orderDetailUrl = orderDetailUrl;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
}
