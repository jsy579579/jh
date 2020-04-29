package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="t_older_rebate_history")
public class UserOlderRebateHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "balance")
    private BigDecimal balance =BigDecimal.ZERO.setScale(2);

    @Column(name="execute_date")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-mm-dd")
    private String executeDate;

    @Column(name="execute_date_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String ExecuteDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getExecuteDateTime() {
        return ExecuteDateTime;
    }

    public void setExecuteDateTime(String executeDateTime) {
        ExecuteDateTime = executeDateTime;
    }

    public String getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(String executeDate) {
        this.executeDate = executeDate;
    }
}
