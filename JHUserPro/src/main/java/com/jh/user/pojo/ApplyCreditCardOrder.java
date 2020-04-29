package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_apply_credit_card_order")
public class ApplyCreditCardOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@Column(name="user_id")
	private int userId;
	@Column(name="brand_id")
	private long brandId;
	@Column(name="order_code")
	private String orderCode;
	@Column(name="name")
	private String name;
	@Column(name="bank_name")
	private String bankName;
	@Column(name="idcard")
	private String idcard;
	@Column(name="phone")
	private String phone;
	@Column(name="rebate")
	private BigDecimal rebate = BigDecimal.ZERO;
	@Column(name="order_status")
	private int orderStatus = 0;
	@Column(name="create_time")
	private Date createTime;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIdcard() {
		return idcard;
	}
	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public BigDecimal getRebate() {
		return rebate;
	}
	public void setRebate(BigDecimal rebate) {
		this.rebate = rebate;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public long getBrandId() {
		return brandId;
	}
	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	@Override
	public String toString() {
		return "ApplyCreditCardOrder [id=" + id + ", userId=" + userId + ", brandId=" + brandId + ", orderCode="
				+ orderCode + ", name=" + name + ", bankName=" + bankName + ", idcard=" + idcard + ", phone=" + phone
				+ ", rebate=" + rebate + ", orderStatus=" + orderStatus + ", createTime=" + createTime + "]";
	}

}
