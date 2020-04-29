package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_sign_calc")
public class SignCalc implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "user_id")
	private String userId;
	
	@Column(name="continue_days")
	private int continueDays;
	
	@Column(name="last_update_time")
	private String lastUpdateTime;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public int getContinueDays() {
		return continueDays;
	}

	public void setContinueDays(int continueDays) {
		this.continueDays = continueDays;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "SignCalc [id=" + id + ", userId=" + userId + ", continueDays=" + continueDays + ", lastUpdateTime="
				+ lastUpdateTime + ", createTime=" + createTime + "]";
	}

	
	
}
