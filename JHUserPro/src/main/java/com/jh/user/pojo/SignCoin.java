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
@Table(name = "t_sign_coin")
public class SignCoin implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "brand_id")
	private String brandId;
	
	@Column(name="grade")
	private String grade;
	
	@Column(name="grade_name")
	private String gradeName;
	
	@Column(name="continue_days")
	private int continueDays;
	
	@Column(name="bonus_coin")
	private String bonusCoin;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public int getContinueDays() {
		return continueDays;
	}

	public void setContinueDays(int continueDays) {
		this.continueDays = continueDays;
	}

	public String getBonusCoin() {
		return bonusCoin;
	}

	public void setBonusCoin(String bonusCoin) {
		this.bonusCoin = bonusCoin;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getGradeName() {
		return gradeName;
	}

	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
	}

	@Override
	public String toString() {
		return "SignCoin [id=" + id + ", brandId=" + brandId + ", grade=" + grade + ", gradeName=" + gradeName
				+ ", continueDays=" + continueDays + ", bonusCoin=" + bonusCoin + ", createTime=" + createTime + "]";
	}

	

	
	
	
}
