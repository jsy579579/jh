package com.jh.mircomall.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class UserAccount implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer userId;

    private Integer id;

    private Integer coin;

    private BigDecimal rateFee;

    private BigDecimal balance;

    private BigDecimal freezeBalance;

    private BigDecimal rebateBalance;

    private BigDecimal freezeRebateBalance;

    private Date createTime;
    


	public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCoin() {
        return coin;
    }

    public void setCoin(Integer coin) {
        this.coin = coin;
    }

    public BigDecimal getRateFee() {
        return rateFee;
    }

    public void setRateFee(BigDecimal rateFee) {
        this.rateFee = rateFee;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getFreezeBalance() {
        return freezeBalance;
    }

    public void setFreezeBalance(BigDecimal freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    public BigDecimal getRebateBalance() {
        return rebateBalance;
    }

    public void setRebateBalance(BigDecimal rebateBalance) {
        this.rebateBalance = rebateBalance;
    }

    public BigDecimal getFreezeRebateBalance() {
        return freezeRebateBalance;
    }

    public void setFreezeRebateBalance(BigDecimal freezeRebateBalance) {
        this.freezeRebateBalance = freezeRebateBalance;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}