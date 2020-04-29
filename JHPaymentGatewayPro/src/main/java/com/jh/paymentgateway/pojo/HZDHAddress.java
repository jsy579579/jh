package com.jh.paymentgateway.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_hzdh_address")
public class HZDHAddress implements Serializable {

	private static final long serialVersionUID = 147L;

	@Id
	@Column(name = "id")
	private long id;

	// MCC码
	@Column(name = "mcc_code")
	private String mccCode;
	// 账单商户编号
	@Column(name = "mct_code")
	private String mctCode;
	// 商户全称
	@Column(name = "mct_name")
	private String mctName;
	// 商户所在地
	@Column(name = "mct_add")
	private String mctAdd;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMccCode() {
		return mccCode;
	}
	public void setMccCode(String mccCode) {
		this.mccCode = mccCode;
	}
	public String getMctCode() {
		return mctCode;
	}
	public void setMctCode(String mctCode) {
		this.mctCode = mctCode;
	}
	public String getMctName() {
		return mctName;
	}
	public void setMctName(String mctName) {
		this.mctName = mctName;
	}
	public String getMctAdd() {
		return mctAdd;
	}
	public void setMctAdd(String mctAdd) {
		this.mctAdd = mctAdd;
	}
	
}
