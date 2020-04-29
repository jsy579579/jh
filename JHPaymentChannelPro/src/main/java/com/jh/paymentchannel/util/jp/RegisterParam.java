package com.jh.paymentchannel.util.jp;

import java.util.List;

public class RegisterParam {
	public String address;
	public String alias;
	public String businessClass;
	public String businessType;
	public String city;
	public String email;
	public String idNumber;
	public String idType;
	public String merchantType;
	public String name;
	public String outNo;
	public String phone;
	public String province;
	public List<Rates> rates;
	public String shopName;
	public String telephone;
	@Override
	public String toString() {
		return "RegisterParam [address=" + address + ", alias=" + alias + ", businessClass=" + businessClass
				+ ", businessType=" + businessType + ", city=" + city + ", email=" + email + ", idNumber=" + idNumber
				+ ", idType=" + idType + ", merchantType=" + merchantType + ", name=" + name + ", outNo=" + outNo
				+ ", phone=" + phone + ", province=" + province + ", rates=" + rates + ", shopName=" + shopName
				+ ", telephone=" + telephone + "]";
	}
	
	
}
