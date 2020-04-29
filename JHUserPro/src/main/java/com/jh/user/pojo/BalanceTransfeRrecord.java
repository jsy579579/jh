package com.jh.user.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "BalanceTransfeRecord")
public class BalanceTransfeRrecord implements Serializable {


    public BalanceTransfeRrecord() {
    }

    ;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    public Long id;
    @Column(name = "amount")
    public BigDecimal amout;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(name = "payStarttime")
    public Date paystartTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmout() {
        return amout;
    }

    public void setAmout(BigDecimal amout) {
        this.amout = amout;
    }

    public Date getPaystartTime() {
        return paystartTime;
    }

    public void setPaystartTime(Date paystartTime) {
        this.paystartTime = paystartTime;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Long getRecipientid() {
        return recipientid;
    }

    public void setRecipientid(Long recipientid) {
        this.recipientid = recipientid;
    }

    public Long getAssignor() {
        return assignor;
    }

    public void setAssignor(Long assignor) {
        this.assignor = assignor;
    }

    /**
     * @param amout
     * @param payStartTime
     * @param rate
     * @param recipientid  接收人
     * @param userid
     */
    public BalanceTransfeRrecord(BigDecimal amout, Date payStartTime, BigDecimal rate, Long recipientid, Long userid) {
        this.amout = amout;
        this.paystartTime = payStartTime;
        this.rate = rate;
        this.recipientid = recipientid;
        this.assignor = userid;
    }

    @Column(name = "rate")
    public BigDecimal rate;
    @Column(name = "recipientid")
    public Long recipientid;
    @Column(name = "assignor")
    public Long assignor;


}
