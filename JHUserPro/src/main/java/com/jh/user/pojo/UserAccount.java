package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_user_account")
public class UserAccount implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	/***用户ID***/
	@Column(name="user_id")
	private long userId;
	/***信用分***/
	@Column(name="credit_points")
	private BigDecimal creditPoints= BigDecimal.ZERO.setScale(2);

	/***用户积分***/
	@Column(name="coin")
	private int coin;
	
	/***用户钱包余额****/
	@Column(name="balance")
	private BigDecimal balance;
	
	/***用户钱包冻结金额***/
	@Column(name="freeze_balance")
	private BigDecimal freezeBalance;
	
	/**分润余额*/
	@Column(name="rebate_balance")
	private BigDecimal rebateBalance;
	
	/**被冻结的分润*/
	@Column(name="freeze_rebate_balance")
	private BigDecimal freezerebateBalance;
	/**管理奖当月累计金额*/
	@Column(name="manage")
	private BigDecimal manage = BigDecimal.ZERO.setScale(4);

	@Column(name="coins")
	private BigDecimal coinNew=BigDecimal.ZERO.setScale(1);

	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public BigDecimal getCoinNew() {
		return coinNew;
	}

	public void setCoinNew(BigDecimal coinNew) {
		this.coinNew = coinNew;
	}
	public BigDecimal getCreditPoints() {
		return creditPoints;
	}

	public void setCreditPoints(BigDecimal creditPoints) {
		this.creditPoints = creditPoints;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public BigDecimal getRebateBalance() {
		return rebateBalance;
	}

	public void setRebateBalance(BigDecimal rebateBalance) {
		this.rebateBalance = rebateBalance;
	}

	public BigDecimal getFreezeBalance() {
		return freezeBalance;
	}

	public void setFreezeBalance(BigDecimal freezeBalance) {
		this.freezeBalance = freezeBalance;
	}

	public BigDecimal getFreezerebateBalance() {
		return freezerebateBalance;
	}

	public void setFreezerebateBalance(BigDecimal freezerebateBalance) {
		this.freezerebateBalance = freezerebateBalance;
	}

	public BigDecimal getManage() {
		return manage;
	}

	public void setManage(BigDecimal manage) {
		this.manage = manage;
	}

	@Override
	public String toString() {
		return "UserAccount{" +
				"id=" + id +
				", userId=" + userId +
				", coin=" + coin +
				", balance=" + balance +
				", freezeBalance=" + freezeBalance +
				", rebateBalance=" + rebateBalance +
				", freezerebateBalance=" + freezerebateBalance +
				", manage=" + manage +
				", createTime=" + createTime +
				'}';
	}
}
