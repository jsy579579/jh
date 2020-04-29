package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserExcel implements Serializable {

	private Date createTime;// 创建日期

	private String realname;

	private String phone;// 手机号

	private String realnameStatus = "3";

	private String shopsStatus = "3";

	private String grade = "0";
	
	private String preUserPhone = "";
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRealnameStatus() {
		return realnameStatus;
	}

	public void setRealnameStatus(String realnameStatus) {
		this.realnameStatus = realnameStatus;
	}

	public String getShopsStatus() {
		return shopsStatus;
	}

	public void setShopsStatus(String shopsStatus) {
		this.shopsStatus = shopsStatus;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getPreUserPhone() {
		return preUserPhone;
	}

	public void setPreUserPhone(String preUserPhone) {
		this.preUserPhone = preUserPhone;
	}

	
}
