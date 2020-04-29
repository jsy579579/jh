package com.jh.user.pojo;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_user_rebate_freeze_history")
public class UserRebateAccountFreezeHistory {

	
	@Id
	@Column(name="id")
	private  int id;
	
	@Column(name="user_id")
	private  long userId;
	
	@Column(name="amount")
	private  BigDecimal amount;
	
	@Column(name="add_or_sub")
	private  String addOrSub;
	
	@Column(name="cur_freeze_bal")
	private  BigDecimal curFreezeBal;
	
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

	public BigDecimal getCurFreezeBal() {
		return curFreezeBal;
	}

	public void setCurFreezeBal(BigDecimal curFreezeBal) {
		this.curFreezeBal = curFreezeBal;
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
