package com.jh.paymentchannel.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_xj_areacode")
public class XJAreaCode implements Serializable{
	
	private static final long serialVersionUID = 214L;
	
	@Id
	@Column(name = "id")
	private long id;
	
	@Column(name = "areacode")
	private String areaCode;
	
	@Column(name = "areaname")
	private String areaName;
	
	@Column(name = "arealevel")
	private String areaLevel;
	
	@Column(name = "parentcode")
	private String parentCode;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaLevel() {
		return areaLevel;
	}

	public void setAreaLevel(String areaLevel) {
		this.areaLevel = areaLevel;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	
	
	
	
	
}
