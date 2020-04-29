package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_user_coin_history")
public class UserCoinHistory implements Serializable{

	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="user_id")
	private long userId;
	
	@Column(name="coin")
	private  int coin;
	
	@Column(name="add_or_sub")
	private  String addOrSub;
	
	@Column(name="cur_coin")
	private  int curCoin;
	
	/**关联的订单号*/
	@Column(name="order_id")
	private  String ordercode;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public String getAddOrSub() {
		return addOrSub;
	}

	public void setAddOrSub(String addOrSub) {
		this.addOrSub = addOrSub;
	}

	public int getCurCoin() {
		return curCoin;
	}

	public void setCurCoin(int curCoin) {
		this.curCoin = curCoin;
	}

	

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getOrdercode() {
		return ordercode;
	}

	public void setOrdercode(String ordercode) {
		this.ordercode = ordercode;
	}

	
	
	
	
	
	
}
