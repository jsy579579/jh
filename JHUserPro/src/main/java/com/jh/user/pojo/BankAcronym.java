package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_bank_acronym")
public class BankAcronym implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id; 
	
	/**银行缩写*/
	@Column(name="bank_acronym")
	private String bankAcronym;

	/**银行编号*/
	@Column(name="bank_name")
	private String bankName;
	
	public String getBankAcronym() {
		return bankAcronym;
	}

	public void setBankAcronym(String bankAcronym) {
		this.bankAcronym = bankAcronym;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
