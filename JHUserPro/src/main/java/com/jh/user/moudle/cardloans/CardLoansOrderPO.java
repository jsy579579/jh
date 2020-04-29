package com.jh.user.moudle.cardloans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_card_loans_order")
public class CardLoansOrderPO implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="user_id")
	private String userId;
	@Column(name="brand_id")
	private String brandId;
	@Column(name="phone")
	private String phone;
	@Column(name="name")
	private String name;
	@Column(name="idcard")
	private String idcard;
	@Column(name="order_type")
	private String orderType;
	@Column(name="order_code")
	private String orderCode;
	@Column(name="classify")
	private String classify;
	@Column(name="rebate")
	private BigDecimal rebate = BigDecimal.ZERO;
	@Column(name="loan_amount")
	private BigDecimal loanAmount = BigDecimal.ZERO;
	@Column(name="order_status")
	private String orderStatus = "0";
	@Column(name="clearing_form")
	private String clearingForm;
	@Column(name="feedback_picture")
	private String feedbackPicture = "";
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
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
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getClassify() {
		return classify;
	}
	public void setClassify(String classify) {
		this.classify = classify;
	}
	public BigDecimal getRebate() {
		return rebate;
	}
	public void setRebate(BigDecimal rebate) {
		this.rebate = rebate;
	}
	public BigDecimal getLoanAmount() {
		return loanAmount;
	}
	public void setLoanAmount(BigDecimal loanAmount) {
		this.loanAmount = loanAmount;
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
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getClearingForm() {
		return clearingForm;
	}
	public void setClearingForm(String clearingForm) {
		this.clearingForm = clearingForm;
	}
	public String getFeedbackPicture() {
		return feedbackPicture;
	}
	public void setFeedbackPicture(String feedbackPicture) {
		this.feedbackPicture = feedbackPicture;
	}
	
}
