package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_brand_notify_config")
public class BrandNotifyConfig implements Serializable{

	private static final long serialVersionUID = 134L;
	
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private int brandId;
	
	@Column(name="secret_key")
	private String secretKey;
	
	@Column(name="ip_address")
	private String ipAddress;
	
	@Column(name="notify_url")
	private String notifyUrl;
	
	@Column(name="update_time")
	private String updateTime;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
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

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	@Override
	public String toString() {
		return "BrandNotifyConfig [id=" + id + ", brandId=" + brandId + ", secretKey=" + secretKey + ", ipAddress="
				+ ipAddress + ", notifyUrl=" + notifyUrl + ", updateTime=" + updateTime + ", createTime=" + createTime
				+ "]";
	}

}
