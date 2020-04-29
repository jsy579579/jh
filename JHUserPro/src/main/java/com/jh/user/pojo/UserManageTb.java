package com.jh.user.pojo;

import javax.persistence.Id;
import java.math.BigDecimal;

public class UserManageTb {

    private String userId;
    private String userName;
    private BigDecimal amount;
    private String brandId;
    private String brandName;
    private String userPhone;

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    @Override
    public String toString() {
        return "UserManageTb{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", amount=" + amount +
                ", brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                '}';
    }
}
