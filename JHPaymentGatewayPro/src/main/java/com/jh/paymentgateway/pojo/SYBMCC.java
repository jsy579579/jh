package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_syb_merchant")
public class SYBMCC implements Serializable {


	private static final long serialVersionUID = -2432813924121676152L;
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "mcc_code")   //商户号  传这个
	private String mccCode;

	@Column(name = "mcc_name")  //商户名
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
