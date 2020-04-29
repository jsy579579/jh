package com.jh.paymentgateway.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_bqx_merchant")
public class BqxMerchant implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "mcc_code")
	private String mccCode;
	
	@Column(name = "mcc_name")
	private String mccName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMccCode() {
		return mccCode;
	}

	public void setMccCode(String mccCode) {
		this.mccCode = mccCode;
	}

	public String getMccName() {
		return mccName;
	}

	public void setMccName(String mccName) {
		this.mccName = mccName;
	}
	
}
