package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name="t_repayment_infomation")
public class RepaymentInfoMation implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="version")
	private String version;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="channel_name")
	private String channelName;

	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "RepaymentInfoMation [id=" + id + ", version=" + version + ", channelTag=" + channelTag
				+ ", channelName=" + channelName + ", createTime=" + createTime + "]";
	}
	
	
	
	
}
