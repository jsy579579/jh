package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_user_role")
public class UserRole implements Serializable{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="user_id")
	private long userId;
	
	@Column(name="role_id")
	private long roleId;
	
	@Column(name="status")
	private String status;
	
	@Column(name="certigieruser_id")
	private long certigierUserId; 
	
	/** 所属品牌 */
	@Column(name = "brand_id")
	private long brandId;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getCertigierUserId() {
		return certigierUserId;
	}

	public void setCertigierUserId(long certigierUserId) {
		this.certigierUserId = certigierUserId;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
