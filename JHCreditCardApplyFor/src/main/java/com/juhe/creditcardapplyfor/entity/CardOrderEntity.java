package com.juhe.creditcardapplyfor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author huhao
 * @title: CardOrderEntity
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/20 002014:17
 */

@Entity
@Table(name="card_order")
public class CardOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //配置主键生成策略,自动增长
    @Column(name = "id")
    private Integer id;

    @Column(name = "client_no")
    private String clientNo;

    @Column(name="brand_id")
    private String brandId;

    @Column(name = "user_id")
    private String useId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "card_id")
    private String cardId;

    @Column(name = "channel_id_station")
    private String stationChannelId;

    @Column(name = "channel_id_bankcard")
    private String bankCardChannelId;

    @Column(name = "create_time")
    private String createTime;

    @Column(name="update_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Column(name = "trade_no")
    private String tradeNo;

    @Column(name = "user_price")
    private BigDecimal userPrice;

    @Column(name = "rebate_price")
    private BigDecimal rebatePrice;

    @Column(name = "status")
    private String status;


    @Override
    public String toString() {
        return "CardOrderEntity{" +
                "id=" + id +
                ", clientNo='" + clientNo + '\'' +
                ", brandId='" + brandId + '\'' +
                ", useId='" + useId + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", cardId='" + cardId + '\'' +
                ", stationChannelId='" + stationChannelId + '\'' +
                ", bankCardChannelId='" + bankCardChannelId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime=" + updateTime +
                ", tradeNo='" + tradeNo + '\'' +
                ", userPrice=" + userPrice +
                ", rebatePrice=" + rebatePrice +
                ", status='" + status + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getUseId() {
        return useId;
    }

    public void setUseId(String useId) {
        this.useId = useId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getStationChannelId() {
        return stationChannelId;
    }

    public void setStationChannelId(String stationChannelId) {
        this.stationChannelId = stationChannelId;
    }

    public String getBankCardChannelId() {
        return bankCardChannelId;
    }

    public void setBankCardChannelId(String bankCardChannelId) {
        this.bankCardChannelId = bankCardChannelId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public BigDecimal getUserPrice() {
        return userPrice;
    }

    public void setUserPrice(BigDecimal userPrice) {
        this.userPrice = userPrice;
    }

    public BigDecimal getRebatePrice() {
        return rebatePrice;
    }

    public void setRebatePrice(BigDecimal rebatePrice) {
        this.rebatePrice = rebatePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
