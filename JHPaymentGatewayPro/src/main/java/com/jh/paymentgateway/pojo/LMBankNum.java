package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_lm_bank_num")
public class LMBankNum implements Serializable{
	
	private static final long serialVersionUID = 111L;
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="bank_num")
	private String bankNum;
	
	@Column(name="bank_name")
	private String bankName;

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
	
}
