package com.jh.user.pojo;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_user")
public class UserDTO implements Serializable{
	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="phone")
	private String phone;//手机号

	
	/**0表示审核中   1表示审核通过  2表示审核拒绝 */
	@Column(name="real_name_status")
	private String realNameStatus;
	
	@Column(name="brand_id")
	private String brandId;




	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRealNameStatus() {
		return realNameStatus;
	}

	public void setRealNameStatus(String realNameStatus) {
		this.realNameStatus = realNameStatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	
}
