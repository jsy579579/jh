package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_brand_resource")
public class BrandResource implements Serializable{

	
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	/**品牌id*/
	@Column(name="brand_id")
	private long brandid;
	
	/**资源id*/
	@Column(name="resource_id")
	private long resourceid;
	
	/**当前状态*/
	@Column(name="status")
	private String status;
	
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date  createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getBrandid() {
		return brandid;
	}

	public void setBrandid(long brandid) {
		this.brandid = brandid;
	}

	public long getResourceid() {
		return resourceid;
	}

	public void setResourceid(long resourceid) {
		this.resourceid = resourceid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	
	
}
