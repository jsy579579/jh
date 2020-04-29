package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="t_channel_support_bank")
public class ChannelSupportBank implements Serializable{

	private static final long serialVersionUID = 1L;


	@Id
	@Column(name="id")
	private long id; 
	
	
	/**通道标识*/
	@Column(name="channel_tag")
	private String channelTag;
	
	
	/**支持银行卡的名称 */
	@Column(name="support_bank_name")
	private String supportBankName;
	
	/**支持银行卡的类型*/
	@Column(name="support_bank_type")
	private String supportBankType;
	
	/**是否支持所有的银行卡 0代表支持所有银行，1代表仅支持列表中的银行*/
	@Column(name="support_bank_all")
	private String supportBankAll;

	@Column(name = "single_min_limit")
	private BigDecimal singleMinLimit;
	
	@Column(name = "single_max_limit")
	private BigDecimal singleMaxLimit;
	
	@Column(name = "every_day_max_limit")
	private BigDecimal everyDayMaxLimit;
	
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

	public String getSupportBankName() {
		return supportBankName;
	}

	public void setSupportBankName(String supportBankName) {
		this.supportBankName = supportBankName;
	}

	public String getSupportBankType() {
		return supportBankType;
	}

	public void setSupportBankType(String supportBankType) {
		this.supportBankType = supportBankType;
	}

	public String getSupportBankAll() {
		return supportBankAll;
	}

	public void setSupportBankAll(String supportBankAll) {
		this.supportBankAll = supportBankAll;
	}

	public BigDecimal getSingleMaxLimit() {
		return singleMaxLimit;
	}

	public void setSingleMaxLimit(BigDecimal singleMaxLimit) {
		this.singleMaxLimit = singleMaxLimit;
	}

	public BigDecimal getEveryDayMaxLimit() {
		return everyDayMaxLimit;
	}

	public void setEveryDayMaxLimit(BigDecimal everyDayMaxLimit) {
		this.everyDayMaxLimit = everyDayMaxLimit;
	}

	public BigDecimal getSingleMinLimit() {
		return singleMinLimit;
	}

	public void setSingleMinLimit(BigDecimal singleMinLimit) {
		this.singleMinLimit = singleMinLimit;
	}

	@Override
	public String toString() {
		return "ChannelSupportBank [id=" + id + ", channelTag=" + channelTag + ", supportBankName=" + supportBankName
				+ ", supportBankType=" + supportBankType + ", supportBankAll=" + supportBankAll + ", singleMinLimit="
				+ singleMinLimit + ", singleMaxLimit=" + singleMaxLimit + ", everyDayMaxLimit=" + everyDayMaxLimit
				+ "]";
	}

}
