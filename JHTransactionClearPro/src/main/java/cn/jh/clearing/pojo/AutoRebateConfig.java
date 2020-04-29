package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_auto_rebate_config")
public class AutoRebateConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6576366054651748540L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name = "on_off")
	private Integer onOff;
	@Column(name = "limit_amount")
	private BigDecimal limitAmount;
	@Column(name = "brand_id")
	private Long brandId;
	@Column(name = "rebate")
	private BigDecimal rebate;
	@Column(name = "create_user_id")
	private Long createUserId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getOnOff() {
		return onOff;
	}

	public void setOnOff(Integer onOff) {
		this.onOff = onOff;
	}

	public BigDecimal getLimitAmount() {
		return limitAmount;
	}

	public void setLimitAmount(BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}

	public BigDecimal getRebate() {
		return rebate;
	}

	public void setRebate(BigDecimal rebate) {
		this.rebate = rebate;
	}

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public Long getBrandId() {
		return brandId;
	}

	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}

	@Override
	public String toString() {
		return "AutoRebateConfig [id=" + id + ", onOff=" + onOff + ", limitAmount=" + limitAmount + ", brandId="
				+ brandId + ", rebate=" + rebate + ", createUserId=" + createUserId + "]";
	}
}
