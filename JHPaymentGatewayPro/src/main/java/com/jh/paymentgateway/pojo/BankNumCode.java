package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_bank_num_code")
public class BankNumCode implements Serializable{
	
	private static final long serialVersionUID = 111L;
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="bank_num")
	private String bankNum;
	
	@Column(name="bank_code")
	private String bankCode;
	
	@Column(name="bank_name")
	private String bankName;
	
	@Column(name="bank_branchcode")
	private String bankBranchcode;

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBankNum() {
		return bankNum;
	}

	public void setBankNum(String bankNum) {
		this.bankNum = bankNum;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankBranchcode() {
		return bankBranchcode;
	}

	public void setBankBranchcode(String bankBranchcode) {
		this.bankBranchcode = bankBranchcode;
	}

	
}
