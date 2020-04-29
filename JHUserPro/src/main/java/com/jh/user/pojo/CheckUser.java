package com.jh.user.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_checkuser")
public class CheckUser implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id; 
	
	
	//用户姓名
	@Column(name="username")
	private String userName;
	
	
	//性别 : 0代表男  1代表女
	@Column(name="gender")
	private int gender;
	
	//手机号
	@Column(name="phone")
	private String phone;
	
	//用户简介
	@Column(name="userinfo")
	private String userinfo;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getUserinfo() {
		return userinfo;
	}

	public void setUserinfo(String userinfo) {
		this.userinfo = userinfo;
	}
	
}
