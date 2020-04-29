package com.jh.paymentchannel.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

public class PaymentOrder implements  Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id;
	
	/**订单号*/
	private String ordercode;
	
	/**描述**/
	private String desc;
	
	/**额外分组
	 * ***/
	private String descCode;
	
	private String outNotifyUrl;
	
	private String outMerOrdercode;
	
	private String outReturnUrl;

	/**总金额*/
	private BigDecimal  amount;
	
	/**用户的Id*/
	private long  userid;
	
	private String openid;
	
	private String userName;
	
	/**用户的手机号码*/
	private String phone;
	
	/**用户充值手机号码*/
	private String phoneBill;
	
	/**车牌号*/
	private String carNo;
	
	/**品牌id*/
	private long brandid;
	
	/**品牌id*/
	private String brandname;
	
	/**结算费率*/
	private BigDecimal rate;
	
	/**通道额外费用*/
	private BigDecimal extraFee;
	
	/**成本费用*/
	private BigDecimal costfee;
	
	/**实际到帐/支付金额*/
	private BigDecimal realAmount;
	
	/**0 充值  1支付   2提现  3退款  7信用卡还款 8信用卡还款账户充值*/
	private String type;
	
	/**当type=1时，  thirdProid 不为空，就证明购买了三级分销*/
	private String  thirdlevelid;
	
	/**走的支付通道*/
	private long channelid;
	
	/**走的支付通道名字*/
	private String channelname;
	
	/**支付标识*/
	private String channelTag;
	
	/**通道的计算类型*/
	private String channelType;

	/**0  待完成   1已成功已结算     2已取消   3待处理  4已成功待结算*/
	private String status;
	
	/**第三方返回的订单号**/
	private String thirdOrdercode;
	
	/**是否是自清**/
	private String autoClearing;
	
	private String remark;
	
	/**
	 * 出款卡号,信用卡
	 */
	private String bankcard;
	
	private String bankName;
	
	/**
	 * 结算卡号,借记卡
	 */
	private String debitBankCard = "";

	private String debitBankName = "";
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getOrdercode() {
		return ordercode;
	}


	public void setOrdercode(String ordercode) {
		this.ordercode = ordercode;
	}


	public String getDescCode() {
		return descCode;
	}


	public void setDescCode(String descCode) {
		this.descCode = descCode;
	}


	public String getDesc() {
		return desc;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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


	public BigDecimal getRate() {
		return rate;
	}


	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}


	public BigDecimal getRealAmount() {
		return realAmount;
	}


	public void setRealAmount(BigDecimal realAmount) {
		this.realAmount = realAmount;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public long getChannelid() {
		return channelid;
	}


	public void setChannelid(long channelid) {
		this.channelid = channelid;
	}


	public String getChannelname() {
		return channelname;
	}


	public void setChannelname(String channelname) {
		this.channelname = channelname;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getRemark() {
		return remark;
	}


	public void setRemark(String remark) {
		this.remark = remark;
	}


	public BigDecimal getExtraFee() {
		return extraFee;
	}


	public void setExtraFee(BigDecimal extraFee) {
		this.extraFee = extraFee;
	}


	public BigDecimal getCostfee() {
		return costfee;
	}


	public void setCostfee(BigDecimal costfee) {
		this.costfee = costfee;
	}


	public String getThirdlevelid() {
		return thirdlevelid;
	}


	public void setThirdlevelid(String thirdlevelid) {
		this.thirdlevelid = thirdlevelid;
	}


	public String getThirdOrdercode() {
		return thirdOrdercode;
	}


	public void setThirdOrdercode(String thirdOrdercode) {
		this.thirdOrdercode = thirdOrdercode;
	}


	public String getBrandname() {
		return brandname;
	}


	public void setBrandname(String brandname) {
		this.brandname = brandname;
	}


	public String getOutNotifyUrl() {
		return outNotifyUrl;
	}


	public void setOutNotifyUrl(String outNotifyUrl) {
		this.outNotifyUrl = outNotifyUrl;
	}


	public String getOutReturnUrl() {
		return outReturnUrl;
	}


	public void setOutReturnUrl(String outReturnUrl) {
		this.outReturnUrl = outReturnUrl;
	}


	public String getOutMerOrdercode() {
		return outMerOrdercode;
	}


	public void setOutMerOrdercode(String outMerOrdercode) {
		this.outMerOrdercode = outMerOrdercode;
	}


	public String getChannelTag() {
		return channelTag;
	}


	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}


	public String getChannelType() {
		return channelType;
	}


	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}


	public String getOpenid() {
		return openid;
	}


	public void setOpenid(String openid) {
		this.openid = openid;
	}


	public String getBankcard() {
		return bankcard;
	}


	public void setBankcard(String bankcard) {
		this.bankcard = bankcard;
	}


	public String getAutoClearing() {
		return autoClearing;
	}


	public void setAutoClearing(String autoClearing) {
		this.autoClearing = autoClearing;
	}
	public String getPhoneBill() {
		return phoneBill;
	}


	public void setPhoneBill(String phoneBill) {
		this.phoneBill = phoneBill;
	}


	public String getCarNo() {
		return carNo;
	}


	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getDebitBankCard() {
		return debitBankCard;
	}


	public void setDebitBankCard(String debitBankCard) {
		this.debitBankCard = debitBankCard;
	}

	
	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getBankName() {
		return bankName;
	}


	public void setBankName(String bankName) {
		this.bankName = bankName;
	}


	public String getDebitBankName() {
		return debitBankName;
	}


	public void setDebitBankName(String debitBankName) {
		this.debitBankName = debitBankName;
	}


	@Override
	public String toString() {
		return "PaymentOrder [id=" + id + ", ordercode=" + ordercode + ", desc=" + desc + ", descCode=" + descCode
				+ ", outNotifyUrl=" + outNotifyUrl + ", outMerOrdercode=" + outMerOrdercode + ", outReturnUrl="
				+ outReturnUrl + ", amount=" + amount + ", userid=" + userid + ", openid=" + openid + ", userName="
				+ userName + ", phone=" + phone + ", phoneBill=" + phoneBill + ", carNo=" + carNo + ", brandid="
				+ brandid + ", brandname=" + brandname + ", rate=" + rate + ", extraFee=" + extraFee + ", costfee="
				+ costfee + ", realAmount=" + realAmount + ", type=" + type + ", thirdlevelid=" + thirdlevelid
				+ ", channelid=" + channelid + ", channelname=" + channelname + ", channelTag=" + channelTag
				+ ", channelType=" + channelType + ", status=" + status + ", thirdOrdercode=" + thirdOrdercode
				+ ", autoClearing=" + autoClearing + ", remark=" + remark + ", bankcard=" + bankcard + ", bankName="
				+ bankName + ", debitBankCard=" + debitBankCard + ", debitBankName=" + debitBankName + "]";
	}


	


}
