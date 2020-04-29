package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_upgrade_detail")
public class UpGradeDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "brand_id")
	private int brandId;

	@Column(name = "user_id")
	private int userId;
	
	@Column(name = "phone")
	private String phone;
	
	@Column(name = "modify_user_id")
	private int modifyUserId;
	
	@Column(name = "modify_phone")
	private String modifyPhone;
	
	@Column(name = "modify_grade")
	private int modifyGrade;
	
	@Column(name = "modify_grade_name")
	private String modifyGradeName;
	
	@Column(name = "modify_type")
	private int modifyType;
	
	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime = new Date();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(int modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getModifyPhone() {
		return modifyPhone;
	}

	public void setModifyPhone(String modifyPhone) {
		this.modifyPhone = modifyPhone;
	}

	public int getModifyGrade() {
		return modifyGrade;
	}

	public void setModifyGrade(int modifyGrade) {
		this.modifyGrade = modifyGrade;
	}

	public String getModifyGradeName() {
		return modifyGradeName;
	}

	public void setModifyGradeName(String modifyGradeName) {
		this.modifyGradeName = modifyGradeName;
	}

	public int getModifyType() {
		return modifyType;
	}

	public void setModifyType(int modifyType) {
		this.modifyType = modifyType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "UpGradeDetail [id=" + id + ", brandId=" + brandId + ", userId=" + userId + ", phone=" + phone
				+ ", modifyUserId=" + modifyUserId + ", modifyPhone=" + modifyPhone + ", modifyGrade=" + modifyGrade
				+ ", modifyGradeName=" + modifyGradeName + ", modifyType=" + modifyType + ", createTime=" + createTime
				+ "]";
	}

}
