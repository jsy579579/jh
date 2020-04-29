package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.jh.common.utils.DateUtil;


/**三级分销关系*/
@Entity
@Table(name="t_user_rebate_history")
public class UserRebateHistory implements Serializable{

	private static final long serialVersionUID = 1L;

	public UserRebateHistory(){
		this.createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	@Id
	@Column(name="id")
	private  int id;
	
	@Column(name="user_id")
	private  long userId;
	
	@Column(name="rebate")
	private  BigDecimal rebate;
	
	@Column(name="add_or_sub")
	private  String addOrSub;
	
	@Column(name="cur_rebate")
	private  BigDecimal curRebate;
	
	/**关联的订单号*/
	@Column(name="order_id")
	private  String orderCode;
	
	/***0表示套现分润  1表示分销返利*/
	@Column(name="order_type")
	private  String orderType = "0";
	
	@Column(name="create_date")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	private String createDate;
	
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

	public BigDecimal getRebate() {
		return rebate;
	}

	public void setRebate(BigDecimal rebate) {
		this.rebate = rebate;
	}

	public String getAddOrSub() {
		return addOrSub;
	}

	public void setAddOrSub(String addOrSub) {
		this.addOrSub = addOrSub;
	}

	public BigDecimal getCurRebate() {
		return curRebate;
	}

	public void setCurRebate(BigDecimal curRebate) {
		this.curRebate = curRebate;
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

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	
	
	
	
	
	
}
