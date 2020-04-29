package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name="t_channel_bank_rate")
public class ChannelBankRate implements Serializable{


	private static final long serialVersionUID = 134L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="bank_name")
	private String bankName;
	
	@Column(name="cost_rate")
	private BigDecimal costRate;
	
	@Column(name="extra_fee")
	private BigDecimal extraFee;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public BigDecimal getCostRate() {
		return costRate;
	}

	public void setCostRate(BigDecimal costRate) {
		this.costRate = costRate;
	}

	public BigDecimal getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(BigDecimal extraFee) {
		this.extraFee = extraFee;
	}

	@Override
	public String toString() {
		return "ChannelBankRate [id=" + id + ", channelTag=" + channelTag + ", bankName=" + bankName + ", costRate="
				+ costRate + ", extraFee=" + extraFee + "]";
	}
	
	
	
	
}
