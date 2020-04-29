package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_brand_auto_upgrade_config")
public class BrandAutoUpdateConfig implements  Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	private long id;
	
	
	@Column(name="brand_id")
	private long brandId; 

	@Column(name="grade")
	private long grade;
	
	@Column(name="pursuant_grade")
	private long pursuantGrade=0;
	
	@Column(name="people_num")
	private int  peopleNum;
	
	@Column(name="team_people_num")
	private int  teamPeopleNum=0;
	
	//团队判定等级
	@Column(name="team_grade")
	private int teamGrade=0;

	@Column(name="auto_update_type")
	private int autoUpdateType;
	
	@Column(name="status")
	private String status;
	
	
	@Transient
	private String productName;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public long getBrandId() {
		return brandId;
	}


	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}


	public long getGrade() {
		return grade;
	}


	public void setGrade(long grade) {
		this.grade = grade;
	}


	public int getPeopleNum() {
		return peopleNum;
	}


	public void setPeopleNum(int peopleNum) {
		this.peopleNum = peopleNum;
	}


	public long getPursuantGrade() {
		return pursuantGrade;
	}


	public void setPursuantGrade(long pursuantGrade) {
		this.pursuantGrade = pursuantGrade;
	}


	public int getTeamPeopleNum() {
		return teamPeopleNum;
	}


	public void setTeamPeopleNum(int teamPeopleNum) {
		this.teamPeopleNum = teamPeopleNum;
	}

	public int getTeamGrade() {
		return teamGrade;
	}


	public void setTeamGrade(int teamGrade) {
		this.teamGrade = teamGrade;
	}


	public int getAutoUpdateType() {
		return autoUpdateType;
	}


	public void setAutoUpdateType(int autoUpdateType) {
		this.autoUpdateType = autoUpdateType;
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


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	
	
	
	
}
