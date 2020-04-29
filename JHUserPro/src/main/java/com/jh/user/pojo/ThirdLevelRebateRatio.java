package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_third_level_rebate_ratio")
public class ThirdLevelRebateRatio implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	/**
	 * 向上返的等级
	 * 
	 * 1表示向上一级 2表示向上二级 3表示向上3级
	 */
	@Column(name = "pre_level")
	private String preLevel;

	/** 返的比率 0.2 表示 20% */
	@Column(name = "ratio")
	private BigDecimal ratio;

	/** 所属品牌 */
	@Column(name = "brand_id")
	private Long brandId;

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPreLevel() {
		return preLevel;
	}

	public void setPreLevel(String preLevel) {
		this.preLevel = preLevel;
	}

	public BigDecimal getRatio() {
		return ratio;
	}

	public void setRatio(BigDecimal ratio) {
		this.ratio = ratio;
	}

	public Long getBrandId() {
		return brandId;
	}

	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
