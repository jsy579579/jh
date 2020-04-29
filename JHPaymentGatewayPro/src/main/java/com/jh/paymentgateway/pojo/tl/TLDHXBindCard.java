package com.jh.paymentgateway.pojo.tl;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author keke
 * @date 2019/08/05
 * @description  通联代还绑卡实体类
 */
@Entity
@Table(name = "t_tldhx_bindcard")
public class TLDHXBindCard implements Serializable {
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
    private String expiredTime; //过期时间

    @Column(name ="security_code")
    private String securityCode;

    @Column(name ="status")
    private String status;

    @Column(name ="create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

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
