package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_kb_error_desc")
public class KBErrorDesc {

	private static final long serialVersionUID = 1156L;

	@Id 
	@Column(name = "id")
	private long id;

	@Column(name = "error_code")
	private String errorCode;

	@Column(name = "error_msg")
	private String errorMsg;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public String toString() {
		return "KBErrorDesc [id=" + id + ", errorCode=" + errorCode + ", errorMsg=" + errorMsg + "]";
	}

	
	
}
