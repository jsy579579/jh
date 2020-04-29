package com.jh.paymentgateway.pojo.ypl;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @description 易票联信用卡绑卡
 */
@Entity
@Table(name = "t_ypl_cbindcard")  //TODO   需要修改属性
public class CBindCard implements Serializable {
    private static final long serialVersionUID = -2161507292421877273L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id",strategy = "uuid")
    private String id;

    @Column(name ="bank_card")
    private String bankCard;

    @Column(name ="phone")
    private String phone;

    @Column(name ="id_card")
    private String idCard;

    @Column(name ="user_name")
    private String userName;

    @Column(name ="expired_time")
    private String expiredTime;

    @Column(name ="security_code")
    private String securityCode;

    @Column(name ="sms_no")
    private String smsNo;

    @Column(name ="sms_code")
    private String smsCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getSmsNo() {
        return smsNo;
    }

    public void setSmsNo(String smsNo) {
        this.smsNo = smsNo;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }




}
