package com.jh.paymentgateway.pojo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_cjxchannel_code")
public class CJXChannelCode implements Serializable{

	private static final long serialVersionUID = -7128426025173944422L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "bank_name")
	private String bankName;

	@Column(name = "channel_code")
	private String channelCode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}
	
}