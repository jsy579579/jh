package com.jh.paymentgateway.pojo.ypl;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @description 易票联储蓄卡绑卡
 */
@Entity
@Table(name = "t_ypl_dbindcard")
public class DBindCard implements Serializable {
    private static final long serialVersionUID = -2161507292421877773L;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id",strategy = "uuid")
    private String id;
    @Column(name ="debit_card_no")
    private String debitCardNo;
    @Column(name ="phone")
    private String phone;
    @Column(name ="id_card")
    private String idCard;
    @Column(name ="user_name")
    private String userName;
    @Column(name ="d_sms_no")
    private String dSmsNo;
    @Column(name ="create_time")
    private Date createTime;

    public String getDebitCardNo() {
        return debitCardNo;
    }

    public void setDebitCardNo(String debitCardNo) {
        this.debitCardNo = debitCardNo;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


}
