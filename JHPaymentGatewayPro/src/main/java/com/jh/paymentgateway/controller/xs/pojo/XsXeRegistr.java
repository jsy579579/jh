package com.jh.paymentgateway.controller.xs.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "t_xsxe_register")
@Entity
public class XsXeRegistr implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;//交易状态
    @Column(name = "username")
    private String username;//用户名
    @Column(name = "phone")
    private String phone;//手机号
    @Column(name = "bank_card")
    private String bankCard;//银行卡号
    @Column(name = "id_card")
    private String idCard;//身份证
    @Column(name = "status")
    private String status;//1签约成功
    @Column(name = "statusmsg")
    private String statusmsg;//错误消息
    @Column(name = "biz_protocol_no")
    private String bizProtocolNo;//用户业务协议号
    @Column(name = "pay_protocol_no")
    private String payProtocolNo;//支付协议号
    @Column(name = "bank_code")
    private String bankCode;//签约银行编码
        @Column(name = "short_card_no")
    private String shortCardNo;//签约卡号尾号
    @Column(name = "pay_number")//下游订单
    private String payNumber;
    @Column(name = "gallery_number")
    private String galleryNumber;//消费号
    @Column(name = "create_time")
    private Date createTime;//时间

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusmsg() {
        return statusmsg;
    }

    public void setStatusmsg(String statusmsg) {
        this.statusmsg = statusmsg;
    }

    public String getBizProtocolNo() {
        return bizProtocolNo;
    }

    public void setBizProtocolNo(String bizProtocolNo) {
        this.bizProtocolNo = bizProtocolNo;
    }

    public String getPayProtocolNo() {
        return payProtocolNo;
    }

    public void setPayProtocolNo(String payProtocolNo) {
        this.payProtocolNo = payProtocolNo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getShortCardNo() {
        return shortCardNo;
    }

    public void setShortCardNo(String shortCardNo) {
        this.shortCardNo = shortCardNo;
    }

    public String getPayNumber() {
        return payNumber;
    }

    public void setPayNumber(String payNumber) {
        this.payNumber = payNumber;
    }

    public String getGalleryNumber() {
        return galleryNumber;
    }

    public void setGalleryNumber(String galleryNumber) {
        this.galleryNumber = galleryNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
