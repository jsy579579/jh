package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_hzhk_order")
public class HZHKOrder {

	private static final long serialVersionUID = 3628493513761818264L;
	
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "id_card")
	private String idCard;
	
	@Column(name = "order_code")
	private String orderCode;
	
	@Column(name = "payypt_order_no")//快捷YPT订单号
	private String yptOrderNo;
	
	@Column(name = "repayypt_order_no")//代还YPT订单号
	private String reyptOrderNo;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private String createTime;

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

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getYptOrderNo() {
		return yptOrderNo;
	}

	public void setYptOrderNo(String yptOrderNo) {
		this.yptOrderNo = yptOrderNo;
	}

	public String getReyptOrderNo() {
		return reyptOrderNo;
	}

	public void setReyptOrderNo(String reyptOrderNo) {
		this.reyptOrderNo = reyptOrderNo;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
