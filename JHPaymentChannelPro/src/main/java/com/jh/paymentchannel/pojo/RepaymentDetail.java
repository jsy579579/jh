package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name="t_repayment_detail")
public class RepaymentDetail implements Serializable{

	private static final long serialVersionUID = 132L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="channel_id")
	private String channelId;
	
	@Column(name="version")
	private String version;
	
	@Column(name="channel_name")
	private String channelName;
	
	@Column(name="description_one")
	private String descriptionOne;
	
	@Column(name="description_two")
	private String descriptionTwo;
	
	@Column(name="description_three")
	private String descriptionThree;
	
	@Column(name="everyday_limit")
	private String everydayLimit;
	
	@Column(name="single_limit")
	private String singleLimit;
	
	@Column(name="trade_time")
	private String tradeTime;
	
	@Column(name="back_ground")
	private String backGround;
	
	@Column(name="class")
	private String classes;
	
	@Column(name="on_off")
	private int onOff;
	
	@Column(name="status")
	private int status;
	
	@Column(name="sort")
	private int sort;
	
	@Column(name="recommend")
	private int recommend;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getDescriptionOne() {
		return descriptionOne;
	}

	public void setDescriptionOne(String descriptionOne) {
		this.descriptionOne = descriptionOne;
	}

	public String getDescriptionTwo() {
		return descriptionTwo;
	}

	public void setDescriptionTwo(String descriptionTwo) {
		this.descriptionTwo = descriptionTwo;
	}

	public String getDescriptionThree() {
		return descriptionThree;
	}

	public void setDescriptionThree(String descriptionThree) {
		this.descriptionThree = descriptionThree;
	}

	public String getBackGround() {
		return backGround;
	}

	public void setBackGround(String backGround) {
		this.backGround = backGround;
	}

	public int getOnOff() {
		return onOff;
	}

	public void setOnOff(int onOff) {
		this.onOff = onOff;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getEverydayLimit() {
		return everydayLimit;
	}

	public void setEverydayLimit(String everydayLimit) {
		this.everydayLimit = everydayLimit;
	}

	public String getSingleLimit() {
		return singleLimit;
	}

	public void setSingleLimit(String singleLimit) {
		this.singleLimit = singleLimit;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getClasses() {
		return classes;
	}

	public void setClasses(String classes) {
		this.classes = classes;
	}

	@Override
	public String toString() {
		return "RepaymentDetail [id=" + id + ", channelTag=" + channelTag + ", channelId=" + channelId + ", version="
				+ version + ", channelName=" + channelName + ", descriptionOne=" + descriptionOne + ", descriptionTwo="
				+ descriptionTwo + ", descriptionThree=" + descriptionThree + ", everydayLimit=" + everydayLimit
				+ ", singleLimit=" + singleLimit + ", tradeTime=" + tradeTime + ", backGround=" + backGround
				+ ", classes=" + classes + ", onOff=" + onOff + ", status=" + status + ", sort=" + sort + ", recommend="
				+ recommend + ", createTime=" + createTime + "]";
	}

	

}
