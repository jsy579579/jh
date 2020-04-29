package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_abroad_ratio")
public class AbroadRatio implements Serializable{


	private static final long serialVersionUID = 134L;
	
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="brand_id")
	private int brandId;
	
	@Column(name="grade")
	private int grade;
	
	@Column(name="grade_name")
	private String grade_name;
	
	@Column(name="rate")
	private BigDecimal rate;
	
	@Column(name="ratio")
	private BigDecimal ratio;
	
	@Column(name="update_time")
	private String update_time;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getGrade_name() {
		return grade_name;
	}

	public void setGrade_name(String grade_name) {
		this.grade_name = grade_name;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getRatio() {
		return ratio;
	}

	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "AbroadRatio [id=" + id + ", brandId=" + brandId + ", grade=" + grade + ", grade_name=" + grade_name
				+ ", rate=" + rate + ", ratio=" + ratio + ", update_time=" + update_time + ", createTime=" + createTime
				+ "]";
	}

	
	
}
