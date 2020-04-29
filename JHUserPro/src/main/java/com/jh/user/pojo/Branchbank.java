package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_le_bankbranch")
public class Branchbank implements Serializable{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="bankbranch_name")
	private String bankBranchname;
	
	@Column(name="bankbranch_no")
	private String bankBranchno;
	
	@Column(name="province")
	private String province;
	
	@Column(name="city")
	private String city;
	
	@Column(name="bank_id")
	private long bankid;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBankBranchname() {
		return bankBranchname;
	}

	public void setBankBranchname(String bankBranchname) {
		this.bankBranchname = bankBranchname;
	}

	public String getBankBranchno() {
		return bankBranchno;
	}

	public void setBankBranchno(String bankBranchno) {
		this.bankBranchno = bankBranchno;
	}

	public String getProvince() {
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
	}

	public long getBankid() {
		return bankid;
	}

	public void setBankid(long bankid) {
		this.bankid = bankid;
	}
	
}
