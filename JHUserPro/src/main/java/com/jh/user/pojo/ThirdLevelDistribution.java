package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

/**三级分销产品列表*/
@Entity
@Table(name="t_third_level_distribution")
public class ThirdLevelDistribution implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	private long id; 
	
	/**3表示最高级  2表示中级 1表示低级 */
	@Column(name="grade")
	private int grade;
	
	@Column(name="name")
	private String name;
	
	/**加盟费*/
	@Column(name="money")
	private BigDecimal money;
	
	/**保证金*/
	@Column(name="deposit")
	private BigDecimal deposit;
	
	
	@Column(name="discount")
	private BigDecimal  discount;
	
	//如何升级描述
	@Column(name="upgrade_state")
	private String upgradestate;
	
	//享受收益描述
	@Column(name="earnings_state")
	private String earningsState;
	
	@Column(name="remark")
	private String remark;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	/**所属品牌*/
	@Column(name="brand_id")
	private Long brandId;
	
	
	/**是否有效  0 表示有效   1表示无效*/
	@Column(name="status")
	private String status;
	
	/**是否有效  0 表示线上购买显示价格  1表示线下购买显示价格   2线上购买不显示价格   3线下购买不显示价格  4 隐藏产品*/
	@Column(name="true_false_buy")
	private String TrueFalseBuy;

	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public BigDecimal getMoney() {
		return money;
	}


	public void setMoney(BigDecimal money) {
		this.money = money;
	}


	public BigDecimal getDiscount() {
		return discount;
	}


	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}


	


	public String getUpgradestate() {
		return upgradestate;
	}


	public void setUpgradestate(String upgradestate) {
		this.upgradestate = upgradestate;
	}


	public String getEarningsState() {
		return earningsState;
	}


	public void setEarningsState(String earningsState) {
		this.earningsState = earningsState;
	}


	public String getRemark() {
		return remark;
	}


	public void setRemark(String remark) {
		this.remark = remark;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public int getGrade() {
		return grade;
	}


	public void setGrade(int grade) {
		this.grade = grade;
	}


	public Long getBrandId() {
		return brandId;
	}


	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}


	public BigDecimal getDeposit() {
		return deposit;
	}


	public void setDeposit(BigDecimal deposit) {
		this.deposit = deposit;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getTrueFalseBuy() {
		return TrueFalseBuy;
	}


	public void setTrueFalseBuy(String trueFalseBuy) {
		TrueFalseBuy = trueFalseBuy;
	}
	
	
	
	
	
}
