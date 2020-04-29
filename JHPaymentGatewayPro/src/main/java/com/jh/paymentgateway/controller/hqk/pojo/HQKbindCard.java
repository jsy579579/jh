package com.jh.paymentgateway.controller.hqk.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_hqk_bindcard")
public class HQKbindCard implements  Serializable{

    private static final long serialVersionUID = 2684304257186348427L;
    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "status")
    private String status;

    @Column(name = "bind_id")
    private String bindId;

    @Column(name = "dsorderid")
    private String dsorderid;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getBindId() {
            return bindId;
        }
    public void setBindId(String bindId) {
        this.bindId = bindId;
    }
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

    public String getDsorderid() {
        return dsorderid;
    }

    public void setDsorderid(String dsorderid) {
        this.dsorderid = dsorderid;
    }

}
