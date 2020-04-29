package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * @title: VIPGiftOrder
 * @projectName: juhepay
 * @description: TODO
 * @author: huhao
 * @date: 2019/8/30 15:14
 */

@Entity
@Table(name = "t_vip_gift_order")
public class VIPGiftOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "logistics_code")
    private String logisticsCode;  //物流订单号

    @Column(name = "gift_name")
    private String giftName;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getLogisticsCode() {
        return logisticsCode;
    }

    public void setLogisticsCode(String logisticsCode) {
        this.logisticsCode = logisticsCode;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "VIPGiftOrder{" +
                "id=" + id +
                ", userId=" + userId +
                ", userPhone='" + userPhone + '\'' +
                ", userName='" + userName + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", logisticsCode='" + logisticsCode + '\'' +
                ", giftName='" + giftName + '\'' +
                ", address='" + address + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
