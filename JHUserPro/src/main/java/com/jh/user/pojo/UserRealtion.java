package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_user_relation")
public class UserRealtion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7357876677250663875L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	@Column(name="first_user_id")
	private Long firstUserId;
	@Column(name="first_user_phone")
	private String firstUserPhone;
	@Column(name="first_user_grade")
	private Integer firstUserGrade = 0;
	@Column(name="real_name_status")
	private Integer realNameStatus = 0;
	@Column(name="pre_user_id")
	private Long preUserId;
	@Column(name="pre_user_phone")
	private String preUserPhone;
	@Column(name="pre_user_grade")
	private Integer preUserGrade = 0;
	@Column(name="level")
	private Integer level = 0;
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime = new Date();
	@Transient
	private BigDecimal rate = BigDecimal.ONE;
	@Transient
	private String firstUserName;
	@Transient
	private String preUserName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFirstUserId() {
		return firstUserId;
	}
	public void setFirstUserId(Long firstUserId) {
		this.firstUserId = firstUserId;
	}
	public String getFirstUserPhone() {
		return firstUserPhone;
	}
	public void setFirstUserPhone(String firstUserPhone) {
		this.firstUserPhone = firstUserPhone;
	}
	public Integer getFirstUserGrade() {
		return firstUserGrade;
	}
	public void setFirstUserGrade(Integer firstUserGrade) {
		this.firstUserGrade = firstUserGrade;
	}
	public Integer getRealNameStatus() {
		return realNameStatus;
	}
	public void setRealNameStatus(Integer realNameStatus) {
		this.realNameStatus = realNameStatus;
	}
	public Long getPreUserId() {
		return preUserId;
	}
	public void setPreUserId(Long preUserId) {
		this.preUserId = preUserId;
	}
	public String getPreUserPhone() {
		return preUserPhone;
	}
	public void setPreUserPhone(String preUserPhone) {
		this.preUserPhone = preUserPhone;
	}
	public Integer getPreUserGrade() {
		return preUserGrade;
	}
	public void setPreUserGrade(Integer preUserGrade) {
		this.preUserGrade = preUserGrade;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public String getFirstUserName() {
		return firstUserName;
	}
	public void setFirstUserName(String firstUserName) {
		this.firstUserName = firstUserName;
	}
	public String getPreUserName() {
		return preUserName;
	}
	public void setPreUserName(String preUserName) {
		this.preUserName = preUserName;
	}
	@Override
	public String toString() {
		return "UserRealtion [id=" + id + ", firstUserId=" + firstUserId + ", firstUserPhone=" + firstUserPhone
				+ ", firstUserGrade=" + firstUserGrade + ", realNameStatus=" + realNameStatus + ", preUserId="
				+ preUserId + ", preUserPhone=" + preUserPhone + ", preUserGrade=" + preUserGrade + ", level=" + level
				+ ", createTime=" + createTime + ", rate=" + rate + ", firstUserName=" + firstUserName
				+ ", preUserName=" + preUserName + "]";
	}
	
	
	
	
}
