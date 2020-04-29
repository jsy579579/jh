package com.jh.paymentgateway.pojo.apdh;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_ap_bindCard")
public class APDHXBindCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "bind_id")
    private String bindId;

    @Column(name = "bind_serial_no")
    private String bindSerialNo;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }

    public String getBindSerialNo() {
        return bindSerialNo;
    }

    public void setBindSerialNo(String bindSerialNo) {
        this.bindSerialNo = bindSerialNo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "APDHXBindCard{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", userName='" + userName + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", idCard='" + idCard + '\'' +
                ", bindId='" + bindId + '\'' +
                ", bindSerialNo='" + bindSerialNo + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
