package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_channel_cost_rate")
public class ChannelCostRate implements Serializable{


	private static final long serialVersionUID = 134L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="channel_id")
	private String channelId;
	
	@Column(name="channel_real_name")
	private String channelRealName;
	
	@Column(name="channel_name")
	private String channelName;
	
	@Column(name="channel_type")
	private String channelType;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="is_bank_rate")
	private int isBankRate;
	
	@Column(name="brand_min_rate")
	private BigDecimal brandMinRate;
	
	@Column(name="brand_extra_fee")
	private BigDecimal brandExtraFee;
	
	@Column(name="cost_rate")
	private BigDecimal costRate;
	
	@Column(name="cost_extra_fee")
	private BigDecimal costExtraFee;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public BigDecimal getBrandMinRate() {
		return brandMinRate;
	}

	public void setBrandMinRate(BigDecimal brandMinRate) {
		this.brandMinRate = brandMinRate;
	}

	public BigDecimal getBrandExtraFee() {
		return brandExtraFee;
	}

	public void setBrandExtraFee(BigDecimal brandExtraFee) {
		this.brandExtraFee = brandExtraFee;
	}

	public BigDecimal getCostRate() {
		return costRate;
	}

	public void setCostRate(BigDecimal costRate) {
		this.costRate = costRate;
	}

	public BigDecimal getCostExtraFee() {
		return costExtraFee;
	}

	public void setCostExtraFee(BigDecimal costExtraFee) {
		this.costExtraFee = costExtraFee;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getIsBankRate() {
		return isBankRate;
	}

	public void setIsBankRate(int isBankRate) {
		this.isBankRate = isBankRate;
	}

	public String getChannelRealName() {
		return channelRealName;
	}

	public void setChannelRealName(String channelRealName) {
		this.channelRealName = channelRealName;
	}

	@Override
	public String toString() {
		return "ChannelCostRate [id=" + id + ", channelId=" + channelId + ", channelRealName=" + channelRealName
				+ ", channelName=" + channelName + ", channelType=" + channelType + ", channelTag=" + channelTag
				+ ", isBankRate=" + isBankRate + ", brandMinRate=" + brandMinRate + ", brandExtraFee=" + brandExtraFee
				+ ", costRate=" + costRate + ", costExtraFee=" + costExtraFee + ", createTime=" + createTime + "]";
	}

	
	
	
}
