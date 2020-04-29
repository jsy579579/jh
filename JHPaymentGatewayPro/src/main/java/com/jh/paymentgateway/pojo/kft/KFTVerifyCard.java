package com.jh.paymentgateway.pojo.kft;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description 快付通效验
 */
@Table(name = "t_kft_verifycard")
@Entity
public class KFTVerifyCard implements Serializable {
    private static final long serialVersionUID = -1655416247719831488L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "kft_order_no")
    private String kftOrderNo;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private Date createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKftOrderNo() {
        return kftOrderNo;
    }

    public void setKftOrderNo(String kftOrderNo) {
        this.kftOrderNo = kftOrderNo;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
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
