package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_pass_verification")
public class PassVerification implements Serializable {

	private static final long serialVersionUID = 1L;

	public PassVerification(String brandId, int batchNo) {
		this();
		this.batchNo = batchNo;
		this.status = "0";
		this.createTime = new Date();
		this.brandId = brandId;
	}

	public PassVerification() {
	}

	@Id
	@Column(name = "id")
	private long id;
	// 激活码
	@Column(name = "passkey")
	private String passkey;
	// 激活的用户id
	@Column(name = "user_id")
	private String userId;
	// 管理激活码的手机
	@Column(name = "user_phone")
	private String userPhone = "0";
	// 管理激活码的姓名
	@Column(name = "user_name")
	private String userName;
	// 批次
	@Column(name = "batch_no")
	private int batchNo;
	// 管理激活码的userId
	@Column(name = "dependence_user_id")
	private Long dependenceUserId;
	// 管理激活码的手机
	@Column(name = "dependence_user_phone")
	private String dependencePhone = "0";
	// 管理激活码的姓名
	@Column(name = "dependence_user_name")
	private String dependenceName;

	// 激活状态: 0未使用 1已使用
	@Column(name = "status")
	private String status;
	// 贴牌号
	@Column(name = "brand_id")
	private String brandId;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPasskey() {
		return passkey;
	}

	public void setPasskey(String passkey) {
		this.passkey = passkey;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public int getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(int batchNo) {
		this.batchNo = batchNo;
	}

	public Long getDependenceUserId() {
		return dependenceUserId;
	}

	public void setDependenceUserId(Long dependenceUserId) {
		this.dependenceUserId = dependenceUserId;
	}

	public String getDependencePhone() {
		return dependencePhone;
	}

	public void setDependencePhone(String dependencePhone) {
		this.dependencePhone = dependencePhone;
	}

	public String getDependenceName() {
		return dependenceName;
	}

	public void setDependenceName(String dependenceName) {
		this.dependenceName = dependenceName;
	}

}
