package com.cardmanager.pro.empty.card.manager;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="t_brand_account",indexes= {
		@Index(columnList="brand_id,version",name="idx_bv",unique=true),
		@Index(columnList="brand_id",name="idx_brand_id"),
		@Index(columnList="version",name="idx_version")
})
public class BrandAccount implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	@Column(name="version")
	private String version="";
	@Column(name="brand_id")
	private String brandId;
	@Column(name="balance")
	private BigDecimal balance = BigDecimal.ZERO;
	@Column(name="freeze_balance")
	private BigDecimal freezeBalance = BigDecimal.ZERO;

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

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public BigDecimal getFreezeBalance() {
		return freezeBalance;
	}

	public void setFreezeBalance(BigDecimal freezeBalance) {
		this.freezeBalance = freezeBalance;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	

}
