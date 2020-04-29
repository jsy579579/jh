package com.jh.paymentgateway.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="t_yc_bind_card")
public class YCBindCard implements Serializable{


	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	@Column(name="card_no")
	private String cardNo;
	@Column(name="user_name")
	private String userName;
	@Column(name="card_phone")
	private String cardPhone;
	@Column(name="id_card")
	private String idCard;
	@Column(name="bind_agree_no")
	private String bindAgreeNo;
	@Column(name="status")
	private String status;
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCardPhone() {
		return cardPhone;
	}
	public void setCardPhone(String cardPhone) {
		this.cardPhone = cardPhone;
	}
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getBindAgreeNo() {
		return bindAgreeNo;
	}
	public void setBindAgreeNo(String bindAgreeNo) {
		this.bindAgreeNo = bindAgreeNo;
	}
	public Date getCreateTime() {
		return createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
