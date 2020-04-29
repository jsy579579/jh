package com.jh.notice.pojo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_sms_inform")
public class SMSInform implements Serializable{

	private static final long serialVersionUID = 1L;

	public SMSInform(){
		this.createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	
	@Id
	@Column(name="id")
	private int id;
	//发送手机号
	@Column(name="phone")
	private String phone;
	
	//归属品牌
	@Column(name="brand_id")
	private String brandId;
	
	//发送IP
	@Column(name="ip_address")
	private String ipAddress;
	//发送类型
	@Column(name="veri_code")
	private String veriCode;
	//发送内容
	@Column(name="content")
	private String content;
	//发送状态码
	@Column(name="resp_code")
	private String respcode;
	//描述
	@Column(name="reason")
	private String reason;
	//所属日期
	@Column(name="create_date")
	private String createDate;
	//创建日期
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getRespcode() {
		return respcode;
	}

	public void setRespcode(String respcode) {
		this.respcode = respcode;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getVeriCode() {
		return veriCode;
	}

	public void setVeriCode(String veriCode) {
		this.veriCode = veriCode;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
}
