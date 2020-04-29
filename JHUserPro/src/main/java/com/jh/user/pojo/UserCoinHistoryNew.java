package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="t_user_coin_history_new")
public class UserCoinHistoryNew {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(name="user_id")
    private long userId;

    @Column(name="coin")
    private BigDecimal coin;

    @Column(name="add_or_sub")
    private  String addOrSub;
    /**关联的订单号*/
    @Column(name="order_id")
    private  String ordercode;
    @Column(name="cur_coin")
    private  BigDecimal curCoin=BigDecimal.ZERO;

    @Column(name="create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    @Transient
    private String fullname;

    @Transient
    private String phone;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getCurCoin() {
        return curCoin;
    }

    public void setCurCoin(BigDecimal curCoin) {
        this.curCoin = curCoin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public String getAddOrSub() {
        return addOrSub;
    }

    public void setAddOrSub(String addOrSub) {
        this.addOrSub = addOrSub;
    }

    public String getOrdercode() {
        return ordercode;
    }

    public void setOrdercode(String ordercode) {
        this.ordercode = ordercode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}

