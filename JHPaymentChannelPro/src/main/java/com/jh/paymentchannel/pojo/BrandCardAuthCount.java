package com.jh.paymentchannel.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity(name="t_brand_card_auth_count")
public class BrandCardAuthCount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1915155558010018208L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(name="brand_id")
	private String brandId;
	
	@Column(name="bank_card_auth_count")
	private Long bankCardAuthCount;
	
	@Transient
	private Long smsCount=0l;
	
	@Column(name="bank_card_type_count")
	private Long bankCardTypeCount;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public Long getBankCardAuthCount() {
		return bankCardAuthCount;
	}
	public void setBankCardAuthCount(Long bankCardAuthCount) {
		this.bankCardAuthCount = bankCardAuthCount;
	}
	public Long getBankCardTypeCount() {
		return bankCardTypeCount;
	}
	public void setBankCardTypeCount(Long bankCardTypeCount) {
		this.bankCardTypeCount = bankCardTypeCount;
	}
	
	public Long getSmsCount() {
		return smsCount;
	}
	public void setSmsCount(Long smsCount) {
		this.smsCount = smsCount;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
