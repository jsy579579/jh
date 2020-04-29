package com.jh.paymentgateway.pojo.kft;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description 快付通协议绑卡
 */
@Table(name = "t_kft_XieyiBindcard")
@Entity
public class KFTProtocolBindCard implements Serializable {
    private static final long serialVersionUID = -6698615398975844603L;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "kft_order_no")
    private String kftOrderNo;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private long createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
