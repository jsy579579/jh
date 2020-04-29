package com.jh.user.moudle.cardloans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name =" t_card_loans_ratio",indexes= {@Index(columnList="brand_id,pre_grade",name="idx_bp",unique=true)})
public class CardLoansRatioPO implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id",columnDefinition="CardLoansRatioId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="brand_id",columnDefinition="贴牌号")
	private String brandId;
	@Column(name="pre_grade",columnDefinition="获取分润的等级")
	private String preGrade;
	@Column(name="ratio",scale=4,columnDefinition="分润的比例")
	private BigDecimal ratio = BigDecimal.ZERO;
	@Column(name = "create_time",columnDefinition="创建时间")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public String getPreGrade() {
		return preGrade;
	}
	public void setPreGrade(String preGrade) {
		this.preGrade = preGrade;
	}
	public BigDecimal getRatio() {
		return ratio;
	}
	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
