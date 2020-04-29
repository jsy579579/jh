package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_qrcode_picture")
public class QrCodePicture implements Serializable {

	private static final long serialVersionUID = 128L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "brand_id")
	private int brandId;
	
	@Column(name = "qrcode_id")
	private String qrcodeId;
	
	@Column(name = "qrcode_url")
	private String qrcodeUrl;
	
	@Column(name = "status")
	private int status = 1;
	
	@Column(name="update_time")
	private String updateTime;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
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

	public String getQrcodeId() {
		return qrcodeId;
	}

	public void setQrcodeId(String qrcodeId) {
		this.qrcodeId = qrcodeId;
	}

	public String getQrcodeUrl() {
		return qrcodeUrl;
	}

	public void setQrcodeUrl(String qrcodeUrl) {
		this.qrcodeUrl = qrcodeUrl;
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
		return "QrCodePicture [id=" + id + ", brandId=" + brandId + ", qrcodeId=" + qrcodeId + ", qrcodeUrl="
				+ qrcodeUrl + ", status=" + status + ", updateTime=" + updateTime + ", createTime=" + createTime + "]";
	}

	
}
