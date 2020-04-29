package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_bank_number")
public class BankNumber implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id; 
	
	/**银行名字*/
	@Column(name="bank_name")
	private String bankName;
	    
	/**银行编号*/
	@Column(name="bank_number")
	private String bankNumber;

	/**银行编号*/
	@Column(name="bank_abbreviation")
	private String bankAbbreviation;
	
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankNumber() {
		return bankNumber;
	}

	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}

	public String getBankAbbreviation() {
		return bankAbbreviation;
	}

	public void setBankAbbreviation(String bankAbbreviation) {
		this.bankAbbreviation = bankAbbreviation;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	
	
	
}
