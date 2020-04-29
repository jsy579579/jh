package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_pass_verification_count")
public class PassVerificationCount implements Serializable {

	private static final long serialVersionUID = 1L;

	public PassVerificationCount() {
	}

	@Id
	@Column(name = "id")
	private long id;
	//用户id
	@Column(name = "user_id")
	private long userId;
	//贴牌id
	@Column(name = "brand_id")
	private long brandId;
	//手机
	@Column(name = "user_phone")
	private String userPhone = "0";
	// 姓名
	@Column(name = "user_name")
	private String userName;
	//拥有激活码
	@Column(name = "ownpasskey")
	private long ownPasskeys;
	//外放激活码
	@Column(name = "issuepasskey")
	private long issuePasskeys;
	//创建时间
	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	//创建时间
	@Column(name = "update_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public long getOwnPasskeys() {
		return ownPasskeys;
	}

	public void setOwnPasskeys(long ownPasskeys) {
		this.ownPasskeys = ownPasskeys;
	}

	public long getIssuePasskeys() {
		return issuePasskeys;
	}

	public void setIssuePasskeys(long issuePasskeys) {
		this.issuePasskeys = issuePasskeys;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}


}
