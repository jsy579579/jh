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
@Table(name="t_sms_record")
public class SMSRecord implements Serializable{

	private static final long serialVersionUID = 1L;

	public SMSRecord(){
		this.createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="ip_address")
	private String ipAddress;
	
	@Column(name="veri_code")
	private String veriCode;
	
	@Column(name="content")
	private String content;
	
	@Column(name="resp_code")
	private String respcode;

	@Column(name="reason")
	private String reason;
	
	@Column(name="create_date")
	private String createDate;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
