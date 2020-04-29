package com.jh.paymentgateway.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_channel_detail")
public class ChannelDetail implements Serializable {

	/**
	 * @author Robin-QQ/WX:354476429
	 * @date 2018年7月3日
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="beanName")
	private String beanName;
	
	@Column(name="channel_type")
	private String channelType;
	
	@Column(name="channel_selurl")
	private String channelSelurl;
	
	
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
	public String getBeanName() {
		return beanName;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getChannelType() {
		return channelType;
	}
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}
	public String getChannelSelurl() {
		return channelSelurl;
	}
	public void setChannelSelurl(String channelSelurl) {
		this.channelSelurl = channelSelurl;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
