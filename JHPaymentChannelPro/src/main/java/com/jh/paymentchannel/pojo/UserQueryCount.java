package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name="t_user_query_count")
public class UserQueryCount implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="query_count")
	private int queryCount;

	@Column(name="car_query_count")
	private int carQueryCount;
	
	@Column(name="update_time")
	private String updateTime;
	
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getQueryCount() {
		return queryCount;
	}

	public void setQueryCount(int queryCount) {
		this.queryCount = queryCount;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getCarQueryCount() {
		return carQueryCount;
	}

	public void setCarQueryCount(int carQueryCount) {
		this.carQueryCount = carQueryCount;
	}

	@Override
	public String toString() {
		return "UserQueryCount [id=" + id + ", userId=" + userId + ", phone=" + phone + ", queryCount=" + queryCount
				+ ", carQueryCount=" + carQueryCount + ", updateTime=" + updateTime + ", createTime=" + createTime
				+ "]";
	}

	

	
	
}
