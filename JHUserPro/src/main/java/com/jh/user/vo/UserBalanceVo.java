package com.jh.user.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class UserBalanceVo implements Serializable {

    private static final long serialVersionUID = -482422507888185074L;

    private String phone;

    private String userName;

    private BigDecimal totalProfit;

    private BigDecimal totalRebate;

    private BigDecimal totalDistribution;

    private BigDecimal curBalance;

    private BigDecimal spendBalance;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getTotalRebate() {
        return totalRebate;
    }

    public void setTotalRebate(BigDecimal totalRebate) {
        this.totalRebate = totalRebate;
    }

    public BigDecimal getCurBalance() {
        return curBalance;
    }

    public void setCurBalance(BigDecimal curBalance) {
        this.curBalance = curBalance;
    }

    public BigDecimal getSpendBalance() {
        return spendBalance;
    }

    public void setSpendBalance(BigDecimal spendBalance) {
        this.spendBalance = spendBalance;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimal getTotalDistribution() {
        return totalDistribution;
    }

    public void setTotalDistribution(BigDecimal totalDistribution) {
        this.totalDistribution = totalDistribution;
    }


}
