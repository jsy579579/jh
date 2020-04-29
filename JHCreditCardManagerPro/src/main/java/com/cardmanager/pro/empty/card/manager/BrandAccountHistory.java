package com.cardmanager.pro.empty.card.manager;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="t_brand_account_histroy",indexes= {
		@Index(columnList="brand_account_id,apply_order_id,add_or_sub",name="idx_baa"),
		@Index(columnList="brand_account_id",name="idx_brand_account_id"),
		@Index(columnList="apply_order_id",name="idx_apply_order_id")
})
public class BrandAccountHistory implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="brand_account_id")
	private Long brandAccountId;
	@Column(name="apply_order_id")
	private Long applyOrderId;
	@Column(name="add_or_sub")
	private int addOrSub;
	@Column(name="amount",scale=2)
	private BigDecimal amount = BigDecimal.ZERO;
	@Column(name="residue_balance",scale=2)
	private BigDecimal residueBalance = BigDecimal.ZERO;
	@Column(name="residue_freeze_balance",scale=2)
	private BigDecimal residueFreezeBalance = BigDecimal.ZERO;
	@Column(name="create_time")
    @Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime = new Date();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getBrandAccountId() {
		return brandAccountId;
	}
	public void setBrandAccountId(Long brandAccountId) {
		this.brandAccountId = brandAccountId;
	}
	public Long getApplyOrderId() {
		return applyOrderId;
	}
	public void setApplyOrderId(Long applyOrderId) {
		this.applyOrderId = applyOrderId;
	}
	public int getAddOrSub() {
		return addOrSub;
	}
	public void setAddOrSub(int addOrSub) {
		this.addOrSub = addOrSub;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getResidueBalance() {
		return residueBalance;
	}
	public void setResidueBalance(BigDecimal residueBalance) {
		this.residueBalance = residueBalance;
	}
	public BigDecimal getResidueFreezeBalance() {
		return residueFreezeBalance;
	}
	public void setResidueFreezeBalance(BigDecimal residueFreezeBalance) {
		this.residueFreezeBalance = residueFreezeBalance;
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
