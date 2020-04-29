package com.jh.paymentgateway.controller.xs.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "t_xsxe_bindcard")
@Entity
public class XSXEBindCard implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "username")
    private String username;
    @Basic
    @Column(name = "phone")
    private String phone;
    @Basic
    @Column(name = "bank_card")
    private String bankCard;
    @Basic
    @Column(name = "id_card")
    private String idCard;
    @Basic
    @Column(name = "create_time")
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date createTime;
    @Basic
    @Column(name = "status")
    private String status;
    public XSXEBindCard(){};

    /**
     *
     * @param username
     * @param phone
     * @param bankCard
     * @param idCard
     * @param createTime
     * @param status
     */
    public XSXEBindCard(String username, String phone, String bankCard, String idCard, Date createTime, String status) {
        this.username = username;
        this.phone = phone;
        this.bankCard = bankCard;
        this.idCard = idCard;
        this.createTime = createTime;
        this.status = status;
    }

    @Override
    public String toString() {
        return "XSXEBindCard{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", idCard='" + idCard + '\'' +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
