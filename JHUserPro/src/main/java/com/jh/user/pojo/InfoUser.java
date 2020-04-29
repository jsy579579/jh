package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;

public class InfoUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long userid;// 系统编号

	private String sex;// 性别

	private String nickname;// 姓名

	private String phone;// 手机号

	private String realname;// 真实姓名

	private String idcard;// 身份证号

	private String grade;// 用户等级
	
	private String gradeName = "普通用户";// 用户等级名称
	
	private String remarks = "";// 用户备注

	private String realnameStatus;// 用户实名状态

	private String usershopStatus;// 商铺状态
	
	private String brandStatus;// 贴牌状态

	private String province;// 省

	private String city;// 市

	// 信用卡管家的激活状态: 0 未激活,1已激活
	private Integer bankCardManagerStatus = 0;

	private String county;// 区

	private String userShopName;// 商铺名称

	private String userShopAddress;// 商铺地址
	/** 银行卡名字 */
	private String bankName;
	/** 银行卡品牌 */
	private String bankBrand;
	/** 卡的号码 */
	private String cardNo;
	/** 商户名称 **/
	private String fullname;
	/** 贴牌id*/
	private long brandId;
	/** 贴牌名称*/
	private String brandName;
	/** 余额 */
	private BigDecimal balance;
	/** 冻结余额 */
	private BigDecimal freezeBalance;
	/** 分润余额 */
	private BigDecimal rebateBalance;
	/** 被冻结的分润 */
	private BigDecimal freezerebateBalance;
	/** 积分 */
	private int coin;
	/** 总入金剔除到银行卡的 **/
	private BigDecimal rechargeSum;
	/** 总提现 */
	private BigDecimal withdrawSum;
	/*** 总分润 **/
	private BigDecimal rebateSum;

	private BigDecimal withdrawFee;// 提款手续费

	private Date createTime;// 创建日期

	private int older;


	public int getOlder() {
		return older;
	}

	public void setOlder(int older) {
		this.older = older;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getRealnameStatus() {
		return realnameStatus;
	}

	public void setRealnameStatus(String realnameStatus) {
		this.realnameStatus = realnameStatus;
	}

	public String getUserShopName() {
		return userShopName;
	}

	public void setUserShopName(String userShopName) {
		this.userShopName = userShopName;
	}

	public String getUserShopAddress() {
		return userShopAddress;
	}

	public void setUserShopAddress(String userShopAddress) {
		this.userShopAddress = userShopAddress;
	}

	public String getUsershopStatus() {
		return usershopStatus;
	}

	public void setUsershopStatus(String usershopStatus) {
		this.usershopStatus = usershopStatus;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankBrand() {
		return bankBrand;
	}

	public void setBankBrand(String bankBrand) {
		this.bankBrand = bankBrand;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public BigDecimal getWithdrawFee() {
		return withdrawFee;
	}

	public void setWithdrawFee(BigDecimal withdrawFee) {
		this.withdrawFee = withdrawFee;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
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

	public BigDecimal getRebateBalance() {
		return rebateBalance;
	}

	public void setRebateBalance(BigDecimal rebateBalance) {
		this.rebateBalance = rebateBalance;
	}

	public BigDecimal getFreezerebateBalance() {
		return freezerebateBalance;
	}

	public void setFreezerebateBalance(BigDecimal freezerebateBalance) {
		this.freezerebateBalance = freezerebateBalance;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public BigDecimal getRechargeSum() {
		return rechargeSum;
	}

	public void setRechargeSum(BigDecimal rechargeSum) {
		this.rechargeSum = rechargeSum;
	}

	public BigDecimal getWithdrawSum() {
		return withdrawSum;
	}

	public void setWithdrawSum(BigDecimal withdrawSum) {
		this.withdrawSum = withdrawSum;
	}

	public BigDecimal getRebateSum() {
		return rebateSum;
	}

	public void setRebateSum(BigDecimal rebateSum) {
		this.rebateSum = rebateSum;
	}

	public Integer getBankCardManagerStatus() {
		return bankCardManagerStatus;
	}

	public void setBankCardManagerStatus(Integer bankCardManagerStatus) {
		this.bankCardManagerStatus = bankCardManagerStatus;
	}

	public String getGradeName() {
		return gradeName;
	}

	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBrandStatus() {
		return brandStatus;
	}

	public void setBrandStatus(String brandStatus) {
		this.brandStatus = brandStatus;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


}
