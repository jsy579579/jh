package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;



@Entity
@Table(name="t_fixed_profit")
public class fixedProfit implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="brand_id")
	private String brandId;
	
	@Column(name="grade")
	private long grade;
	
	@Column(name="grade_name")
	private String gradeName;
	
	@Column(name="fixed_profit")
	private String  fixedProfit;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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

	public long getGrade() {
		return grade;
	}

	public void setGrade(long grade) {
		this.grade = grade;
	}

	public String getFixedProfit() {
		return fixedProfit;
	}

	public void setFixedProfit(String fixedProfit) {
		this.fixedProfit = fixedProfit;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getGradeName() {
		return gradeName;
	}

	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
	}

	@Override
	public String toString() {
		return "fixedProfit [id=" + id + ", brandId=" + brandId + ", grade=" + grade + ", gradeName=" + gradeName
				+ ", fixedProfit=" + fixedProfit + ", createTime=" + createTime + "]";
	}

	
	
}
