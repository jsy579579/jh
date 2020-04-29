package com.jh.user.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_bank_icon")
public class BankIcon implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="bank_name")
	private String bank_name;
	
	@Column(name="bank_acronym")
	private String bank_acronym;
	
	@Column(name="background")
	private String background;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_acronym() {
		return bank_acronym;
	}

	public void setBank_acronym(String bank_acronym) {
		this.bank_acronym = bank_acronym;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	@Override
	public String toString() {
		return "BankIcon [id=" + id + ", bank_name=" + bank_name + ", bank_acronym=" + bank_acronym + ", background="
				+ background + "]";
	}

	
	
}
