package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author 作者:zxj
 */
@Entity
@Table(name = "t_xt_bindcard")
public class XTBindCard implements Serializable {

	private static final long serialVersionUID = 8300466815704961671L;
	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "phone")
	private String phone;

	@Column(name = "id_card")
	private String idCard;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "card_type")
	private String cardType;

	@Column(name = "sub_merch_id")
	private String subMerchId;

	@Column(name = "create_time")
	private String createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getSubMerchId() {
		return subMerchId;
	}

	public void setSubMerchId(String subMerchId) {
		this.subMerchId = subMerchId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
