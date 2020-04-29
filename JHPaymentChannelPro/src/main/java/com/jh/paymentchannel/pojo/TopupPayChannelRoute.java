package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_topup_pay_route")
public class TopupPayChannelRoute implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	private int id;
	
	/**品牌Id*/
	@Column(name="brand_code")
	private String brandcode;
	
	
	/**比如走的 快捷，  支付宝扫码，  微信扫码*/
	/**渠道标识*/
	@Column(name="channel_code")
	private String channelcode;
	
	/**渠道哦名字*/
	@Column(name="channel_name")
	private String channelName;
	
	
	/***目标走的通到*/
	@Column(name="target_channel_tag")
	private String targetChannelTag;
	
	
	/**类型  0 为充值通道，  类型 1 为代付渠道*/
	@Column(name="type")
	private String type;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getBrandcode() {
		return brandcode;
	}


	public void setBrandcode(String brandcode) {
		this.brandcode = brandcode;
	}


	public String getChannelcode() {
		return channelcode;
	}


	public void setChannelcode(String channelcode) {
		this.channelcode = channelcode;
	}


	public String getChannelName() {
		return channelName;
	}


	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	

	public String getTargetChannelTag() {
		return targetChannelTag;
	}


	public void setTargetChannelTag(String targetChannelTag) {
		this.targetChannelTag = targetChannelTag;
	}


	@Override
	public String toString() {
		return "TopupPayChannelRoute [id=" + id + ", brandcode=" + brandcode + ", channelcode=" + channelcode
				+ ", channelName=" + channelName + ", targetChannelTag=" + targetChannelTag + ", type=" + type
				+ ", createTime=" + createTime + "]";
	}
	
	
	
	
	
}
