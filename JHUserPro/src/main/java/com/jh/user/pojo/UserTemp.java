package com.jh.user.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_user_temp")
public class UserTemp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6200979920509562018L;
	@Column(name="grade_tpye")
	private String grade = "";
	@Id
	@Column(name="phone")
	private String phone = "";
	@Column(name="real_name")
	private String realName = "";

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
