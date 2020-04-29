package cn.jh.clearing.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;



@Entity
@Table(name="t_brand_profit")
public class BrandProfit implements Serializable{


	private static final long serialVersionUID = 134L;
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name="brand_id")
	private long brandId;
	
	@Column(name="channel_name")
	private String channelName;
	
	@Column(name="channel_real_name")
	private String channelRealName;
	
	@Column(name="channel_type")
	private String channelType;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="sum_amount")
	private BigDecimal sumAmount;
	
	@Column(name="sum_real_amount")
	private BigDecimal sumRealAmount;
	
	@Column(name="number")
	private long number;
	
	@Column(name="brand_profit")
	private BigDecimal brandProfit;

	@Column(name="count_profit")
	private BigDecimal countProfit;

	@Column(name="cost_profit")
	private BigDecimal costProfit;
	
	@Column(name="trade_time")
	private String tradeTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelRealName() {
		return channelRealName;
	}

	public void setChannelRealName(String channelRealName) {
		this.channelRealName = channelRealName;
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

	public BigDecimal getSumAmount() {
		return sumAmount;
	}

	public void setSumAmount(BigDecimal sumAmount) {
		this.sumAmount = sumAmount;
	}

	public BigDecimal getSumRealAmount() {
		return sumRealAmount;
	}

	public void setSumRealAmount(BigDecimal sumRealAmount) {
		this.sumRealAmount = sumRealAmount;
	}

	public BigDecimal getBrandProfit() {
		return brandProfit;
	}

	public void setBrandProfit(BigDecimal brandProfit) {
		this.brandProfit = brandProfit;
	}

	public BigDecimal getCostProfit() {
		return costProfit;
	}

	public void setCostProfit(BigDecimal costProfit) {
		this.costProfit = costProfit;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public BigDecimal getCountProfit() {
		return countProfit;
	}

	public void setCountProfit(BigDecimal countProfit) {
		this.countProfit = countProfit;
	}

	@Override
	public String toString() {
		return "BrandProfit{" +
				"id=" + id +
				", brandId=" + brandId +
				", channelName='" + channelName + '\'' +
				", channelRealName='" + channelRealName + '\'' +
				", channelType='" + channelType + '\'' +
				", channelTag='" + channelTag + '\'' +
				", sumAmount=" + sumAmount +
				", sumRealAmount=" + sumRealAmount +
				", number=" + number +
				", brandProfit=" + brandProfit +
				", countProfit=" + countProfit +
				", costProfit=" + costProfit +
				", tradeTime='" + tradeTime + '\'' +
				'}';
	}
}
