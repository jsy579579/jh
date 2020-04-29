package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_credit_coin_bank")
public class CreditCoinExchangeBank implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	@Column(name="bank_code")
	private String bankCode;
	@Column(name="bank_name")
	private String bankName;
	@Column(name="bank_detail")
	private String bankDetail;
	@Column(name="min_exchange_coin")
	private int minExchangeCoin;
	@Column(name="one_level")
	private BigDecimal oneLevel;
	@Column(name="two_level")
	private BigDecimal twoLevel;
	@Column(name="three_level")
	private BigDecimal threeLevel;
	@Column(name="four_level")
	private BigDecimal fourLevel;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getBankDetail() {
		return bankDetail;
	}
	public void setBankDetail(String bankDetail) {
		this.bankDetail = bankDetail;
	}
	public int getMinExchangeCoin() {
		return minExchangeCoin;
	}
	public void setMinExchangeCoin(int minExchangeCoin) {
		this.minExchangeCoin = minExchangeCoin;
	}
	public BigDecimal getOneLevel() {
		return oneLevel;
	}
	public void setOneLevel(BigDecimal oneLevel) {
		this.oneLevel = oneLevel;
	}
	public BigDecimal getTwoLevel() {
		return twoLevel;
	}
	public void setTwoLevel(BigDecimal twoLevel) {
		this.twoLevel = twoLevel;
	}
	public BigDecimal getThreeLevel() {
		return threeLevel;
	}
	public void setThreeLevel(BigDecimal threeLevel) {
		this.threeLevel = threeLevel;
	}
	public BigDecimal getFourLevel() {
		return fourLevel;
	}
	public void setFourLevel(BigDecimal fourLevel) {
		this.fourLevel = fourLevel;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
