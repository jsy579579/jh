package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_repayment_brand_status",indexes= {
		@Index(columnList="brand_id,version",name="idx_bv",unique=true),
		@Index(columnList="version",name="idx_version"),
		@Index(columnList="brand_id",name="idx_brandid"),
})
public class RepaymentBrandStatus implements Serializable{

	private static final long serialVersionUID = 123L; 
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private int brandId;
	
	@Column(name="version")
	private String version;
	
	@Column(name="channel_tag")
	private String channelTag;
	
	@Column(name="status")
	private int status;
	
	@Column(name="update_time")
	private String updateTime;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT=8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "RepaymentBrandStatus [id=" + id + ", brandId=" + brandId + ", version=" + version + ", channelTag="
				+ channelTag + ", status=" + status + ", updateTime=" + updateTime + ", createTime=" + createTime + "]";
	}
	
}
