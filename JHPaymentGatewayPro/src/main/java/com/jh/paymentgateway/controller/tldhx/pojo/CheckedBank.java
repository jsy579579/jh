package com.jh.paymentgateway.controller.tldhx.pojo;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the checked_bank database table.
 * 
 */
@Entity
@Table(name="checked_bank")
public class CheckedBank implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;

	@Column(name="bank_describe")
	private String bankDescribe;

	@Column(name="bank_name")
	private String bankName;

	@Column(name="card_bin")
	private String cardBin;

	@Column(name="card_type")
	private String cardType;

	public CheckedBank() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBankDescribe() {
		return this.bankDescribe;
	}

	public void setBankDescribe(String bankDescribe) {
		this.bankDescribe = bankDescribe;
	}

	public String getBankName() {
		return this.bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getCardBin() {
		return this.cardBin;
	}

	public void setCardBin(String cardBin) {
		this.cardBin = cardBin;
	}

	public String getCardType() {
		return this.cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

}