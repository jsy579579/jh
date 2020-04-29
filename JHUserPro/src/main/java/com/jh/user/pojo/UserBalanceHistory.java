package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_user_balance_history")
public class UserBalanceHistory  implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private  int id;
	
	@Column(name="user_id")
	private  long userId;
	
	@Column(name="amount")
	private  BigDecimal amount;
	
	@Column(name="add_or_sub")
	private  String addOrSub;
	
	@Column(name="cur_bal")
	private  BigDecimal curBal;
	
	/**关联的订单号*/
	@Column(name="order_code")
	private  String orderCode;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getAddOrSub() {
		return addOrSub;
	}

	public void setAddOrSub(String addOrSub) {
		this.addOrSub = addOrSub;
	}

	public BigDecimal getCurBal() {
		return curBal;
	}

	public void setCurBal(BigDecimal curBal) {
		this.curBal = curBal;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
}
