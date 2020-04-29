package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_mh_ght_city_merchant")
public class MHGHTCityMerchant {

	private static final long serialVersionUID = 154L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "province")
	private String province;

	@Column(name = "city")
	private String city;

	@Column(name = "merchant_code")
	private String merchantCode;
	
	@Column(name = "merchant_name")
	private String merchantName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	@Override
	public String toString() {
		return "GHTCityMerchant [id=" + id + ", province=" + province + ", city=" + city + ", merchantCode="
				+ merchantCode + ", merchantName=" + merchantName + "]";
	}

	
	
	
}
