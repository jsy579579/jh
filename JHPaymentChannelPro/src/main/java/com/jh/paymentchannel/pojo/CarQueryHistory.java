package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name="t_car_query_history")
public class CarQueryHistory implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="car_num")
	private String carNum;
	
	@Column(name="query_history")
	private String queryHistory;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getQueryHistory() {
		return queryHistory;
	}

	public void setQueryHistory(String queryHistory) {
		this.queryHistory = queryHistory;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCarNum() {
		return carNum;
	}

	public void setCarNum(String carNum) {
		this.carNum = carNum;
	}

	@Override
	public String toString() {
		return "CarQueryHistory [id=" + id + ", userId=" + userId + ", carNum=" + carNum + ", queryHistory="
				+ queryHistory + ", createTime=" + createTime + "]";
	}

	

	
}
