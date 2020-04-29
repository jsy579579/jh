package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


//商铺表
@Entity
@Table(name="t_user_shops")
public class UserShops  implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id; 
	
	
	@Column(name="user_id")
	private long userId; 
	
	/**
	 * 商铺名
	 * 
	 * **/
	@Column(name="name")
	private String name; 
	
	/**
	 * 所在地区
	 * **/
	@Column(name="address")
	private String address; 
	
	/**
	 * 商铺地址
	 * 
	 * **/
	@Column(name="shopsaddress")
	private String shopsaddress; 
	
	/**
	 * 图片文件夹名
	 * **/
	@Column(name="src")
	private String src; 
	
	
	
	/**
	 * 经营模式
	 * **/
	@Column(name="management_form")
	private String ManagementForm; 
	
	/**
	 * 状态
	 * 0、未审核；1、审核成功；2：审核失败;3:未提交
	 * */
	@Column(name="status")
	private String status="0";
	
	
	/**
	 * 创建时间
	 * 
	 **/
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


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getShopsaddress() {
		return shopsaddress;
	}


	public void setShopsaddress(String shopsaddress) {
		this.shopsaddress = shopsaddress;
	}


	public String getSrc() {
		return src;
	}


	public void setSrc(String src) {
		this.src = src;
	}


	public String getManagementForm() {
		return ManagementForm;
	}


	public void setManagementForm(String managementForm) {
		ManagementForm = managementForm;
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

	
	
	
}
