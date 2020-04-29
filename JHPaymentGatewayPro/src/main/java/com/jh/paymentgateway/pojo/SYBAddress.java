package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_syb_address")
public class SYBAddress implements Serializable {


	private static final long serialVersionUID = 1343301342090855734L;

	@Id
	@Column(name = "area")  //地区码
	private String area;

	@Column(name = "name")   //地区名
	private String name;

	@Column(name = "province")  //省市码
	private String province;

	@Column(name = "level")    //层级     2省  3市
	private String level;

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
