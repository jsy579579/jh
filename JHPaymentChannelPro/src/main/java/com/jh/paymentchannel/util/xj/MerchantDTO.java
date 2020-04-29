package com.jh.paymentchannel.util.xj;

/**
 * 下游请求的参数封装
 *
 * @author Zoran
 * @date 2017/12/7
 */
public class MerchantDTO extends BaseDTO {

    private String customerInfo;

    private String provinceCode;

    private String cityCode;

    private String address;

    private String fee0;

    private Integer d0fee;
    
    private String pointsType;

	public String getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(String customerInfo) {
		this.customerInfo = customerInfo;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFee0() {
		return fee0;
	}

	public void setFee0(String fee0) {
		this.fee0 = fee0;
	}

	public Integer getD0fee() {
		return d0fee;
	}

	public void setD0fee(Integer d0fee) {
		this.d0fee = d0fee;
	}

	public String getPointsType() {
		return pointsType;
	}

	public void setPointsType(String pointsType) {
		this.pointsType = pointsType;
	}
    
}
