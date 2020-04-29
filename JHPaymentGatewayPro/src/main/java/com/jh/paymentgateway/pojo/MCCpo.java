package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_mcc")
public class MCCpo implements Serializable {
	private static final long serialVersionUID = 114L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "type")
	private String type;

	@Column(name = "mcc")
	private String mcc;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	@Override
	public String toString() {
		return "MCCpo [id=" + id + ", type=" + type + ", mcc=" + mcc + "]";
	}

	
	
	
	

}
