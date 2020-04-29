package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_channel_detail")
public class ChannelDetail implements Serializable{

	private static final long serialVersionUID = 1L;


	@Id
	@Column(name="id")
	private long id; 
	
	
	/**渠道标识*/
	@Column(name="channel_tag")
	private String channelTag;
	
	
	/**传到渠道的参数, 集成的商家类的 */
	@Column(name="channel_param")
	private String channelParams;
	
	/**渠道商家标号*/
	@Column(name="channel_no")
	private String channelNo;
	
	/**异部回调的url*/
	@Column(name="notify_url")
	private String notifyURL;
	
	/**同步回调的url*/
	@Column(name="return_url")
	private String returnURL;
	
	
	/**渠道名字*/
	@Column(name="name")	
	private String name;
	
	
	/**子渠道标识*/
	@Column(name="sub_channel_tag")	
	private String subChannelTag;
	
	/**渠道名字*/
	@Column(name="sub_name")	
	private String subName;
	
	
	/**单次最低限额*/
	@Column(name="single_min_limit")	
	private BigDecimal  singleMinLimit;
	
	
	/**单次最高额度*/
	@Column(name="single_max_limit")	
	private BigDecimal  singleMaxLimit;
	
	
	/**单日最高额度*/
	@Column(name="every_day_max_limit")	
	private BigDecimal  everyDayMaxLimit;
	
	
	/**通道开始可以使用的时间*/
	@Column(name="start_time")	
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private  Date  startTime;
	
	
	/**通道结算的时间**/
	@Column(name="end_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date   endTime;
	
	
	@Transient
	private BigDecimal rate;
	
	/**是否自动清算*/
	@Column(name="auto_clearing")
	private String autoclearing = "0";
	
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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


	public String getChannelNo() {
		return channelNo;
	}


	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getSubChannelTag() {
		return subChannelTag;
	}


	public void setSubChannelTag(String subChannelTag) {
		this.subChannelTag = subChannelTag;
	}


	public String getSubName() {
		return subName;
	}


	public void setSubName(String subName) {
		this.subName = subName;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public BigDecimal getSingleMinLimit() {
		return singleMinLimit;
	}


	public void setSingleMinLimit(BigDecimal singleMinLimit) {
		this.singleMinLimit = singleMinLimit;
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


	public Date getStartTime() {
		return startTime;
	}


	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}


	public Date getEndTime() {
		return endTime;
	}


	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public BigDecimal getRate() {
		return rate;
	}


	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}


	public String getChannelParams() {
		return channelParams;
	}


	public void setChannelParams(String channelParams) {
		this.channelParams = channelParams;
	}


	public String getNotifyURL() {
		return notifyURL;
	}


	public void setNotifyURL(String notifyURL) {
		this.notifyURL = notifyURL;
	}


	public String getReturnURL() {
		return returnURL;
	}


	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}


	public String getAutoclearing() {
		return autoclearing;
	}


	public void setAutoclearing(String autoclearing) {
		this.autoclearing = autoclearing;
	}
	
	
	
	
}
