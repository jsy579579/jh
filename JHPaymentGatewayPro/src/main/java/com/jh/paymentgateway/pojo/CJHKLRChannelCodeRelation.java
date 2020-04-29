package com.jh.paymentgateway.pojo;

import javax.persistence.*;
import java.util.Date;

/**
 * @title: CJHKLRChannelCodeRelation
 * @projectName: DDSH
 * @description: TODO
 * @author: huhao
 * @date: 2019/12/6 14:41
 */
@Entity(name = "t_cjhk_lr_channel_code_relation")
public class CJHKLRChannelCodeRelation  {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "idcard")
    private String idcard;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "channel_code")
    private String chanelCode;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private Date createTime;

    @Override
    public String toString() {
        return "CJHKLRChannelCodeRelation{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", idcard='" + idcard + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", chanelCode='" + chanelCode + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getChanelCode() {
        return chanelCode;
    }

    public void setChanelCode(String chanelCode) {
        this.chanelCode = chanelCode;
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
}
