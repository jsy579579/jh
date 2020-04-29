package com.jh.paymentgateway.pojo.hxdhd;

import com.fasterxml.jackson.annotation.JsonFormat;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author huhao
 * @title: HXDHDRegister
 * @projectName juhepay
 * @description: TODO
 * @date 2019/8/15 10:14
 */

@Entity
@Table(name = "t_hxkj_register")
public class HXDHDRegister implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "phone")
    private String phone;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "bank_card")
    private String bankCard;

    @Column(name = "id_card")
    private String idCard;

    @Column(name = "merchant_code")
    private String merchantCode;

    @Column(name = "status")
    private String status;

    @Column(name = "rate")
    private String rate;

    @Column(name = "extra_fee")
    private String extraFee;

    @Column(name = "create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "HXDHDRegister [id=" + id + ", phone=" + phone + ", userName=" + userName + ", bankCard=" + bankCard
				+ ", idCard=" + idCard + ", merchantCode=" + merchantCode + ", status=" + status + ", rate=" + rate
				+ ", extraFee=" + extraFee + ", createTime=" + createTime + "]";
	}
    
    
}
