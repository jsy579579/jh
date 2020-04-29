package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_user_award_config")
public class UserAwardConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	@Column(name="on_off")
	private boolean onOff = false;
	@Column(name="brand_id")
	private String brandId;
	@Column(name="type")
	private String type = "0";
//	注册人奖励金额
	@Column(name="award_money")
	private BigDecimal awardMoney = BigDecimal.ZERO;
//	推荐人奖励金额
	@Column(name="pre_award_money")
	private BigDecimal preAwardMoney = BigDecimal.ZERO;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isOnOff() {
		return onOff;
	}
	public void setOnOff(boolean onOff) {
		this.onOff = onOff;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public BigDecimal getAwardMoney() {
		return awardMoney;
	}
	public void setAwardMoney(BigDecimal awardMoney) {
		this.awardMoney = awardMoney;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public BigDecimal getPreAwardMoney() {
		return preAwardMoney;
	}
	public void setPreAwardMoney(BigDecimal preAwardMoney) {
		this.preAwardMoney = preAwardMoney;
	}
	

}
