package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_channel_support_debit_bankcard")
public class ChannelSupportDebitBankCard {

	private static final long serialVersionUID = 164L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "channel_tag")
	private String channelTag;

	@Column(name = "bank_name")
	private String bankName;
	
	@Column(name = "bank_abbr")
	private String bankAbbr;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankAbbr() {
		return bankAbbr;
	}

	public void setBankAbbr(String bankAbbr) {
		this.bankAbbr = bankAbbr;
	}

	@Override
	public String toString() {
		return "ChannelSupportDebitBankCard [id=" + id + ", channelTag=" + channelTag + ", bankName=" + bankName
				+ ", bankAbbr=" + bankAbbr + "]";
	}

	
	
	
	

}
