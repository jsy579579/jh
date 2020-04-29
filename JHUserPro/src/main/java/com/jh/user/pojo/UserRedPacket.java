package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Random;

@Entity
@Table(name="t_user_red_packet")
public class UserRedPacket implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name="id")
	private long id;
	
	/***奖励描述***/
	@Column(name="red_title")
	private String redTitle;
	
	/***奖励类型  0:现金奖励； 1：积分奖励 ；3：其他奖励***/
	@Column(name="type")
	private int type;
	
	/***区间最大值****/
	@Column(name="max_balance")
	private BigDecimal maxBalance;
	
	/***区间最小值****/
	@Column(name="min_balance")
	private BigDecimal minBalance;

	/***获奖概率***/
	@Column(name="ratio")
	private int ratio;
	
	/**所属贴牌*/
	@Column(name="brand_id")
	private long brandId;
	
	/**图片地址**/
	@Column(name="img_path")
	private String imgPath;
	
	/**保留域1**/
	@Column(name="reserver1")
	private String reserver1;
	
	/**保留域2**/
	@Column(name="reserver2")
	private String reserver2;
	
	/**保留域3**/
	@Column(name="reserver3")
	private String reserver3;
	
	
	/**保留域4**/
	@Column(name="reserver4")
	private String reserver4;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
//	public static void main(String[] args){
//	  Random rand = new Random();
//	  for(int i=0; i<10; i++) {
//	   System.out.println(rand.nextInt(500-300+1) + 300);
//	  }
//	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getRedTitle() {
		return redTitle;
	}

	public void setRedTitle(String redTitle) {
		this.redTitle = redTitle;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public String getReserver1() {
		return reserver1;
	}

	public void setReserver1(String reserver1) {
		this.reserver1 = reserver1;
	}

	public String getReserver2() {
		return reserver2;
	}

	public void setReserver2(String reserver2) {
		this.reserver2 = reserver2;
	}

	public String getReserver3() {
		return reserver3;
	}

	public void setReserver3(String reserver3) {
		this.reserver3 = reserver3;
	}

	public String getReserver4() {
		return reserver4;
	}

	public void setReserver4(String reserver4) {
		this.reserver4 = reserver4;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public BigDecimal getMaxBalance() {
		return maxBalance;
	}

	public void setMaxBalance(BigDecimal maxBalance) {
		this.maxBalance = maxBalance;
	}

	public BigDecimal getMinBalance() {
		return minBalance;
	}

	public void setMinBalance(BigDecimal minBalance) {
		this.minBalance = minBalance;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	

	
	
}
