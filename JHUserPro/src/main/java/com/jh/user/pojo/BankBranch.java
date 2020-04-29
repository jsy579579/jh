package com.jh.user.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;
@Entity
@Table(name="t_bank_branch")
public class BankBranch implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="bankNo")
	private String bankNo;
	
	@Column(name="bankName")
	private String bankName;
	
	@Column(name="topNo")
	private String topNo;
	
	@Column(name="topName")
	private String topName;
	
	@Transient
	private String province;
	
	@Transient
	private String city;
	
	/*public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}*/

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBankNo() {
		return bankNo;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getTopNo() {
		return topNo;
	}

	public void setTopNo(String topNo) {
		this.topNo = topNo;
	}

	public String getTopName() {
		return topName;
	}

	public void setTopName(String topName) {
		this.topName = topName;
	}

}
