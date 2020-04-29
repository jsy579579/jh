package cn.jh.risk.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_black_white_list")
public class BlackWhiteList implements Serializable{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="user_id")
	private long  userid;
	
	@Column(name="phone")
	private String phone;
	
	@Column(name="brand_id")
	private long brandid;
	
	
	/**0 表示登陆无法进行  1表示无法充值   2 表示无法提现  3 无法支付*/
	@Column(name="operation_type")
	private String operationType;//0表示登录1表示账户操作
	
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getBrandid() {
		return brandid;
	}

	public void setBrandid(long brandid) {
		this.brandid = brandid;
	}

	

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	
	
	
	
}
