package com.jh.paymentgateway.util.hq;

/*
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CommonBean implements Serializable {

	*//**
		* 
		*/
/*
private static final long serialVersionUID = 5016944159848569429L;
private String chnSerialNo;//创建商户池 后我司会提供一个创建商户池,该字段对于表中的商户号
private String categoryUnion;//银联行业类型
private String inviteMerNo;//如果为实体商户，且为连锁商户，则该商户号必填
private String subMerchantNo;//子商户号

public String getChnSerialNo() {
return chnSerialNo;
}

public void setChnSerialNo(String chnSerialNo) {
this.chnSerialNo = chnSerialNo;
}

public String getCategoryUnion() {
return categoryUnion;
}

public void setCategoryUnion(String categoryUnion) {
this.categoryUnion = categoryUnion;
}

public String getInviteMerNo() {
return inviteMerNo;
}

public void setInviteMerNo(String inviteMerNo) {
this.inviteMerNo = inviteMerNo;
}

public String getSubMerchantNo() {
return subMerchantNo;
}

public void setSubMerchantNo(String subMerchantNo) {
this.subMerchantNo = subMerchantNo;
}

// 交易类型
private String transcode;
private String ExpDate;
private String fixAmount;
// 商户号
private String merchno;
private String accountname;
private String terminalId;//商户机具终端编号
// 商户订单号
private String dsorderid;

public String getTerminalId() {
return terminalId;
}

public void setTerminalId(String terminalId) {
this.terminalId = terminalId;
}

// 注册号或统一信用代码
private String regno;

// 公司名称
private String compayname;

// 企业法人
private String frname;

// 备注
private String remark;

// 版本号
private String version;

// 流水号
private String ordersn;

// 返回码
private String returncode;

// 返回信息
private String errtext;

// 订单号
private String orderid;

// 证件号码
private String idcard;
// 证件类型
private String idtype;
// 银行卡号
private String bankcard;
// 用户名
private String username;

// 人脸识别图1
private String face1;

// 人脸识别图2
private String face2;

// 相识度
private String score;

// 建议值
private String threshold;
// 建议最高
private String thhigh;
// 建议最低
private String thlow;
// 业务发生地
private String businessplace;
// 业务类型
private String businesstype;

// 头像 base64
private String headimg;

private String bankid;

private String bankname;

private String bankcode;

private String bankcodename;

private String image;

private String amount;// 金额
private String productName;// 商户名称
private String accounttype;// 账户类型 00 银行卡 01 存折
private String accountProperty;// 账户属性 00 对私 01 对公

private String provinceCode;// 省份编码
private String cityCode;// 城市编码

private String appid;// 微信appid
private String openid;// 用户openid

private String returnUrl;//

private String formDataInfo;// 主装表单信息

private String carNum;// 是 违章车牌号（完整）
private String carFrameNum;// 否 车架号（依据“1.2.1查询输入条件接口”）
private String engineNum;// 否 发动机号（依据“1.2.1查询输入条件接口”）
private String carType;// 否 车辆类型（见“2.2车辆分类”，不传则默认为小车“02”）
private String provId;// 否 数据来源省份编码（依据“1.2.1查询输入条件接口”），不传则默认查询车牌归属地数据
private String cityId;// 否 数据来源城市编码（同上）

private String licenseno;// 否 驾驶证号码（扣车主本人分时需要填写）
private String licensePhone;// 否 此车辆在车管所登记的手机号码（扣车主本人分时需要填写）

public String agentId;
public String merId;
public String orderId;
public String txnTime;
public String txnAmt;
public String settleAmt;
public String realName;
public String idCardNo;
public String bankCardNo;
public String bankCode;
public String phone;
public String expireDate;
public String withdrawUrl;
public String settleBankCode;
public String settleCardNo;
public String settlePhone;
public String bankName;
public String settleCardBankName;

public String getFormDataInfo() {
return formDataInfo;
}

public void setFormDataInfo(String formDataInfo) {
this.formDataInfo = formDataInfo;
}

public String getStatus() {
return status;
}

public void setStatus(String status) {
this.status = status;
}

public String getTranstype() {
return transtype;
}

public void setTranstype(String transtype) {
this.transtype = transtype;
}

public String getCurrency() {
return currency;
}

public void setCurrency(String currency) {
this.currency = currency;
}

public String getPaytime() {
return paytime;
}

public void setPaytime(String paytime) {
this.paytime = paytime;
}

public String getSignType() {
return signType;
}

public void setSignType(String signType) {
this.signType = signType;
}

private String transtype;//
private String currency;//
private String paytime;//
private String signType;//

public String getTranscode() {
return transcode;
}

public void setTranscode(String transcode) {
this.transcode = transcode;
}

public String getMerchno() {
return merchno;
}

public void setMerchno(String merchno) {
this.merchno = merchno;
}

public String getDsorderid() {
return dsorderid;
}

public void setDsorderid(String dsorderid) {
this.dsorderid = dsorderid;
}

public String getRegno() {
return regno;
}

public void setRegno(String regno) {
this.regno = regno;
}

public String getCompayname() {
return compayname;
}

public void setCompayname(String compayname) {
this.compayname = compayname;
}

public String getFrname() {
return frname;
}

public void setFrname(String frname) {
this.frname = frname;
}

public String getRemark() {
return remark;
}

public void setRemark(String remark) {
this.remark = remark;
}

public String getSign() {
return sign;
}

public void setSign(String sign) {
this.sign = sign;
}

public String getVersion() {
return version;
}

public void setVersion(String version) {
this.version = version;
}

public String getOrdersn() {
return ordersn;
}

public void setOrdersn(String ordersn) {
this.ordersn = ordersn;
}

public String getReturncode() {
return returncode;
}

public void setReturncode(String returncode) {
this.returncode = returncode;
}

public String getErrtext() {
return errtext;
}

public void setErrtext(String errtext) {
this.errtext = errtext;
}

public String getOrderid() {
return orderid;
}

public void setOrderid(String orderid) {
this.orderid = orderid;
}

public String getIdcard() {
return idcard;
}

public void setIdcard(String idcard) {
this.idcard = idcard;
}

public String getIdtype() {
return idtype;
}

public void setIdtype(String idtype) {
this.idtype = idtype;
}

public String getBankcard() {
return bankcard;
}

public void setBankcard(String bankcard) {
this.bankcard = bankcard;
}

public String getUsername() {
return username;
}

public void setUsername(String username) {
this.username = username;
}

public String getFace1() {
return face1;
}

public void setFace1(String face1) {
this.face1 = face1;
}

public String getFace2() {
return face2;
}

public void setFace2(String face2) {
this.face2 = face2;
}

public String getScore() {
return score;
}

public void setScore(String score) {
this.score = score;
}

public String getThreshold() {
return threshold;
}

public void setThreshold(String threshold) {
this.threshold = threshold;
}

public String getThhigh() {
return thhigh;
}

public void setThhigh(String thhigh) {
this.thhigh = thhigh;
}

public String getThlow() {
return thlow;
}

public void setThlow(String thlow) {
this.thlow = thlow;
}

public String getBusinessplace() {
return businessplace;
}

public void setBusinessplace(String businessplace) {
this.businessplace = businessplace;
}

public String getBusinesstype() {
return businesstype;
}

public void setBusinesstype(String businesstype) {
this.businesstype = businesstype;
}

public String getHeadimg() {
return headimg;
}

public void setHeadimg(String headimg) {
this.headimg = headimg;
}

public String getBankid() {
return bankid;
}

public void setBankid(String bankid) {
this.bankid = bankid;
}

public String getBankname() {
return bankname;
}

public void setBankname(String bankname) {
this.bankname = bankname;
}

public String getBankcode() {
return bankcode;
}

public void setBankcode(String bankcode) {
this.bankcode = bankcode;
}

public String getBankcodename() {
return bankcodename;
}

public void setBankcodename(String bankcodename) {
this.bankcodename = bankcodename;
}

public String getImage() {
return image;
}

public void setImage(String image) {
this.image = image;
}

public String getAmount() {
return amount;
}

public void setAmount(String amount) {
this.amount = amount;
}

public String getAccounttype() {
return accounttype;
}

public void setAccounttype(String accounttype) {
this.accounttype = accounttype;
}

public String getAccountProperty() {
return accountProperty;
}

public void setAccountProperty(String accountProperty) {
this.accountProperty = accountProperty;
}

public String getProvinceCode() {
return provinceCode;
}

public void setProvinceCode(String provinceCode) {
this.provinceCode = provinceCode;
}

public String getCityCode() {
return cityCode;
}

public void setCityCode(String cityCode) {
this.cityCode = cityCode;
}

public String getProductName() {
return productName;
}

public void setProductName(String productName) {
this.productName = productName;
}

public String getAppid() {
return appid;
}

public void setAppid(String appid) {
this.appid = appid;
}

public String getOpenid() {
return openid;
}

public void setOpenid(String openid) {
this.openid = openid;
}

public String getReturnUrl() {
return returnUrl;
}

public void setReturnUrl(String returnUrl) {
this.returnUrl = returnUrl;
}

public String getCarNum() {
return carNum;
}

public void setCarNum(String carNum) {
this.carNum = carNum;
}

public String getCarFrameNum() {
return carFrameNum;
}

public void setCarFrameNum(String carFrameNum) {
this.carFrameNum = carFrameNum;
}

public String getEngineNum() {
return engineNum;
}

public void setEngineNum(String engineNum) {
this.engineNum = engineNum;
}

public String getCarType() {
return carType;
}

public void setCarType(String carType) {
this.carType = carType;
}

public String getProvId() {
return provId;
}

public void setProvId(String provId) {
this.provId = provId;
}

public String getCityId() {
return cityId;
}

public void setCityId(String cityId) {
this.cityId = cityId;
}

public String getLicenseno() {
return licenseno;
}

public void setLicenseno(String licenseno) {
this.licenseno = licenseno;
}

public String getLicensePhone() {
return licensePhone;
}

public void setLicensePhone(String licensePhone) {
this.licensePhone = licensePhone;
}

public String getAgentId() {
return agentId;
}

public void setAgentId(String agentId) {
this.agentId = agentId;
}

public String getMerId() {
return merId;
}

public void setMerId(String merId) {
this.merId = merId;
}

public String getOrderId() {
return orderId;
}

public void setOrderId(String orderId) {
this.orderId = orderId;
}

public String getTxnTime() {
return txnTime;
}

public void setTxnTime(String txnTime) {
this.txnTime = txnTime;
}

public String getTxnAmt() {
return txnAmt;
}

public void setTxnAmt(String txnAmt) {
this.txnAmt = txnAmt;
}

public String getSettleAmt() {
return settleAmt;
}

public void setSettleAmt(String settleAmt) {
this.settleAmt = settleAmt;
}

public String getRealName() {
return realName;
}

public void setRealName(String realName) {
this.realName = realName;
}

public String getIdCardNo() {
return idCardNo;
}

public void setIdCardNo(String idCardNo) {
this.idCardNo = idCardNo;
}

public String getBankCardNo() {
return bankCardNo;
}

public void setBankCardNo(String bankCardNo) {
this.bankCardNo = bankCardNo;
}

public String getBankCode() {
return bankCode;
}

public void setBankCode(String bankCode) {
this.bankCode = bankCode;
}

public String getPhone() {
return phone;
}

public void setPhone(String phone) {
this.phone = phone;
}

public String getExpireDate() {
return expireDate;
}

public void setExpireDate(String expireDate) {
this.expireDate = expireDate;
}

public String getWithdrawUrl() {
return withdrawUrl;
}

public void setWithdrawUrl(String withdrawUrl) {
this.withdrawUrl = withdrawUrl;
}

public String getSettleBankCode() {
return settleBankCode;
}

public void setSettleBankCode(String settleBankCode) {
this.settleBankCode = settleBankCode;
}

public String getSettleCardNo() {
return settleCardNo;
}

public void setSettleCardNo(String settleCardNo) {
this.settleCardNo = settleCardNo;
}

public String getSettlePhone() {
return settlePhone;
}

public void setSettlePhone(String settlePhone) {
this.settlePhone = settlePhone;
}

public String getBankName() {
return bankName;
}

public void setBankName(String bankName) {
this.bankName = bankName;
}

public String getSettleCardBankName() {
return settleCardBankName;
}

public void setSettleCardBankName(String settleCardBankName) {
this.settleCardBankName = settleCardBankName;
}

private String subMerNo;// 商户号
private String merName;// 商户名称
private String merState;// 商户所在省
private String merCity;// 商户所在市
private String merAddress;// 地址
private String certType;// 证件类型01：身份证 02：军官证03：护照04：户口簿05：回乡证06：其他
private String certId;// 证件号

private String mobile;// 手机号
private String accountId;// 结算账号
private String accountName;// 结算户名
private String openBankName;// 开户行全称
private String openBankCode;// 开户行联行号
private String openBankState;// 开户行省份
private String openBankCity;// 开户行城市

private String operFlag;// 操作标识
private String t0drawFee;// 单笔D0提现交易手续费
private String t0drawRate;// D0提现交易手续费扣率
private String t1consFee;// 单笔消费交易手续费
private String t1consRate;// 消费交易手续费扣率

*//**
	* 卡属性 (B.对公C.对私)
	*/
/*
private String cardAttribute;
*//**
	* CREDIT：贷记卡 DEBIT：借记卡(暂时只支持信用卡) DEPOSIT：存折 BUZACC：对公账户
	*
	* 账号类型
	*/
/*
private String accType;

private String orderNo;// 商户订单号
private String orderAmount;// 订单金额

private String retCode;// 商户号
private String retMsg;// 商户订单号
private String transStatus;// 交易状态

private String telNo;// 手机号
private String accNo;// 账号
private String cvn2;// CVN2
private String cardPassword;// 卡密
private String valiDate;// 卡有效期
private String merReserved;// 商户自定义域

private String notifyUrl;
*//**
	* 持卡人
	*/
/*
private String cardOwner;
*//**
	* 证件号码
	*/
/*
private String certNo;
*//**
	* 付款用户IP地址
	*/
/*
private String payerIp;
*//**
	* 卡类型(DEBIT.借记,CREDIT.贷记(信用卡)
	* 
	*//*
	private String cardType;
	// 交易状态 00未处理 01交易成功 02交易失败 03交易处理中 04未支付
	private String status;
	
	// private String transStatus;
	
	private String sign;
	
	public String getSubMerNo() {
	return subMerNo;
	}
	
	public void setSubMerNo(String subMerNo) {
	this.subMerNo = subMerNo;
	}
	
	public String getMerName() {
	return merName;
	}
	
	public void setMerName(String merName) {
	this.merName = merName;
	}
	
	public String getMerState() {
	return merState;
	}
	
	public void setMerState(String merState) {
	this.merState = merState;
	}
	
	public String getMerCity() {
	return merCity;
	}
	
	public void setMerCity(String merCity) {
	this.merCity = merCity;
	}
	
	public String getMerAddress() {
	return merAddress;
	}
	
	public void setMerAddress(String merAddress) {
	this.merAddress = merAddress;
	}
	
	public String getCertType() {
	return certType;
	}
	
	public void setCertType(String certType) {
	this.certType = certType;
	}
	
	public String getCertId() {
	return certId;
	}
	
	public void setCertId(String certId) {
	this.certId = certId;
	}
	
	public String getMobile() {
	return mobile;
	}
	
	public void setMobile(String mobile) {
	this.mobile = mobile;
	}
	
	public String getAccountId() {
	return accountId;
	}
	
	public void setAccountId(String accountId) {
	this.accountId = accountId;
	}
	
	public String getAccountName() {
	return accountName;
	}
	
	public void setAccountName(String accountName) {
	this.accountName = accountName;
	}
	
	public String getOpenBankName() {
	return openBankName;
	}
	
	public void setOpenBankName(String openBankName) {
	this.openBankName = openBankName;
	}
	
	public String getOpenBankCode() {
	return openBankCode;
	}
	
	public void setOpenBankCode(String openBankCode) {
	this.openBankCode = openBankCode;
	}
	
	public String getOpenBankState() {
	return openBankState;
	}
	
	public void setOpenBankState(String openBankState) {
	this.openBankState = openBankState;
	}
	
	public String getOpenBankCity() {
	return openBankCity;
	}
	
	public void setOpenBankCity(String openBankCity) {
	this.openBankCity = openBankCity;
	}
	
	public String getOperFlag() {
	return operFlag;
	}
	
	public void setOperFlag(String operFlag) {
	this.operFlag = operFlag;
	}
	
	public String getT0drawFee() {
	return t0drawFee;
	}
	
	public void setT0drawFee(String t0drawFee) {
	this.t0drawFee = t0drawFee;
	}
	
	public String getT0drawRate() {
	return t0drawRate;
	}
	
	public void setT0drawRate(String t0drawRate) {
	this.t0drawRate = t0drawRate;
	}
	
	public String getT1consFee() {
	return t1consFee;
	}
	
	public void setT1consFee(String t1consFee) {
	this.t1consFee = t1consFee;
	}
	
	public String getT1consRate() {
	return t1consRate;
	}
	
	public void setT1consRate(String t1consRate) {
	this.t1consRate = t1consRate;
	}
	
	public String getCardAttribute() {
	return cardAttribute;
	}
	
	public void setCardAttribute(String cardAttribute) {
	this.cardAttribute = cardAttribute;
	}
	
	public String getAccType() {
	return accType;
	}
	
	public void setAccType(String accType) {
	this.accType = accType;
	}
	
	public String getOrderNo() {
	return orderNo;
	}
	
	public void setOrderNo(String orderNo) {
	this.orderNo = orderNo;
	}
	
	public String getOrderAmount() {
	return orderAmount;
	}
	
	public void setOrderAmount(String orderAmount) {
	this.orderAmount = orderAmount;
	}
	
	public String getRetCode() {
	return retCode;
	}
	
	public void setRetCode(String retCode) {
	this.retCode = retCode;
	}
	
	public String getRetMsg() {
	return retMsg;
	}
	
	public void setRetMsg(String retMsg) {
	this.retMsg = retMsg;
	}
	
	public String getTransStatus() {
	return transStatus;
	}
	
	public void setTransStatus(String transStatus) {
	this.transStatus = transStatus;
	}
	
	public String getTelNo() {
	return telNo;
	}
	
	public void setTelNo(String telNo) {
	this.telNo = telNo;
	}
	
	public String getAccNo() {
	return accNo;
	}
	
	public void setAccNo(String accNo) {
	this.accNo = accNo;
	}
	
	public String getCvn2() {
	return cvn2;
	}
	
	public void setCvn2(String cvn2) {
	this.cvn2 = cvn2;
	}
	
	public String getCardPassword() {
	return cardPassword;
	}
	
	public void setCardPassword(String cardPassword) {
	this.cardPassword = cardPassword;
	}
	
	public String getValiDate() {
	return valiDate;
	}
	
	public void setValiDate(String valiDate) {
	this.valiDate = valiDate;
	}
	
	public String getMerReserved() {
	return merReserved;
	}
	
	public void setMerReserved(String merReserved) {
	this.merReserved = merReserved;
	}
	
	public String getNotifyUrl() {
	return notifyUrl;
	}
	
	public void setNotifyUrl(String notifyUrl) {
	this.notifyUrl = notifyUrl;
	}
	
	public String getCardOwner() {
	return cardOwner;
	}
	
	public void setCardOwner(String cardOwner) {
	this.cardOwner = cardOwner;
	}
	
	public String getCertNo() {
	return certNo;
	}
	
	public void setCertNo(String certNo) {
	this.certNo = certNo;
	}
	
	public String getPayerIp() {
	return payerIp;
	}
	
	public void setPayerIp(String payerIp) {
	this.payerIp = payerIp;
	}
	
	public String getCardType() {
	return cardType;
	}
	
	public void setCardType(String cardType) {
	this.cardType = cardType;
	}
	
	public String getExpDate() {
	return ExpDate;
	}
	
	public void setExpDate(String expDate) {
	ExpDate = expDate;
	}
	
	public String getAccountname() {
	return accountname;
	}
	
	public void setAccountname(String accountname) {
	this.accountname = accountname;
	}
	
	public String txnRate;
	public String siglePrice;
	
	public String getTxnRate() {
	return txnRate;
	}
	
	public void setTxnRate(String txnRate) {
	this.txnRate = txnRate;
	}
	
	public String getSiglePrice() {
	return siglePrice;
	}
	
	public void setSiglePrice(String siglePrice) {
	this.siglePrice = siglePrice;
	}
	
	// Ght start
	private String merchantName;// 商户名称
	
	private String shortName;// 商户简称
	
	private String city;// 商户城市
	
	private String name;// 持卡人姓名
	
	private String bankaccountType;// 银行卡类型
	
	private String merchantAddress;// 商户地址
	
	private String servicePhone;// 客服电话
	
	private String orgCode;// 组织机构代码
	
	private String merchantType;// 商户类型
	
	private String category;// 经营类目
	
	private String corpmanName;// 法人姓名
	
	private String corpmanId;// 法人身份证
	
	private String corpmanPhone;// 法人联系电话
	
	private String corpmanMobile;// 法人联系手机
	
	private String corpmanEmail;// 法人邮箱
	
	private String bankaccountNo;// 开户行账号
	
	private String bankaccountName;// 开户户名
	
	private String autoCus;// 自动提现
	
	private String bankaccProp;// 账户属性
	
	private String certCode;// 办卡证件类型
	
	private String bankbranchNo;// 联行号
	
	private String defaultAcc;// 默认账户
	
	private String merchantId;// 子商户编码
	
	private String handleType;// 操作类型
	
	private String cycleValue;// 结算周期
	
	private String allotFlag;// 机构资金调拨 机构资金调拨
	
	private String busiCode;// 开通业务
	
	private String futureRateType;// 费率类型
	
	private String futureRateValue;// 费率
	
	private String futureMinAmount;// 保底
	
	private String futureMaxAmount;// 封顶
	
	private String bankCardType;// 银行卡类型
	
	private String certificateType;// 证件类型
	
	private String certificateNo;// 证件号码
	
	private String mobilePhone;// 手机号码
	
	private String valid;// 有效期
	
	private String pin;// 卡交易密码
	
	private String bankBranch;// 支行信息
	
	private String province;// 所属省
	
	private String userId;// 商户用户标识
	
	private String childMerchantId;// 子商户号
	
	private String bindId;// 绑卡ID（签约编号）
	
	private String reckonCurrency;// 清算币种
	
	private String fcCardNo;// 入金卡号
	
	private String userFee;// 用户手续费
	
	private String productCategory;// 商品类别
	
	private String notify_url;// 异步通知地址
	
	private String productDesc;// 商品名称
	
	private String oriReqMsgId;// 原请求交易流水号
	
	private String validateCode;// 验证码
	
	private String mobileNo;// 银行卡预留手机号
	
	private String frontUrl;// 前台跳转地址
	
	private String backUrl;// 后台跳转地址
	
	// 代付
	
	private String business_code;//
	
	private String user_id;//
	
	private String DF_type;//
	
	private String account_no;//
	
	private String e_user_code;//
	
	private String bank_code;//
	
	private String account_type;//
	
	private String allot_flag;//
	
	private String bank_name;//
	
	private String bank_type;//
	
	private String account_prop;//
	
	private String terminal_no;//
	
	private String protocol;//
	
	private String protocol_userid;//
	
	private String id_type;//
	
	private String ID;//
	
	private String tel;//
	
	private String extra_fee;//
	
	private String account_name;//
	
	private String deviceType;// 设备类型
	private String deviceId;// 设备号
	private String userIP;// 客户端Ip
	private String bindValid;// 绑卡有效期
	private String Query_sn;// 要查询的交易流水
	private String Query_remark;// 查询备注
	private String reckonCurrent;// 清算币种
	
	private String beginCreateDate;// 开始日期
	
	private String endCreateDate;// 结束日期
	private String page;// 分页数
	private String bindUrl;// 银联绑卡页面
	private String payStatus;// 支付结果
	private String size;// 每页大小
	private String oriPayMsgId;// 原平台交易（确认支付）流水号
	
	public String getMerchantName() {
	return merchantName;
	}
	
	public void setMerchantName(String merchantName) {
	this.merchantName = merchantName;
	}
	
	public String getShortName() {
	return shortName;
	}
	
	public void setShortName(String shortName) {
	this.shortName = shortName;
	}
	
	public String getCity() {
	return city;
	}
	
	public void setCity(String city) {
	this.city = city;
	}
	
	public String getMerchantAddress() {
	return merchantAddress;
	}
	
	public void setMerchantAddress(String merchantAddress) {
	this.merchantAddress = merchantAddress;
	}
	
	public String getServicePhone() {
	return servicePhone;
	}
	
	public void setServicePhone(String servicePhone) {
	this.servicePhone = servicePhone;
	}
	
	public String getOrgCode() {
	return orgCode;
	}
	
	public void setOrgCode(String orgCode) {
	this.orgCode = orgCode;
	}
	
	public String getMerchantType() {
	return merchantType;
	}
	
	public void setMerchantType(String merchantType) {
	this.merchantType = merchantType;
	}
	
	public String getCategory() {
	return category;
	}
	
	public void setCategory(String category) {
	this.category = category;
	}
	
	public String getCorpmanName() {
	return corpmanName;
	}
	
	public void setCorpmanName(String corpmanName) {
	this.corpmanName = corpmanName;
	}
	
	public String getCorpmanId() {
	return corpmanId;
	}
	
	public void setCorpmanId(String corpmanId) {
	this.corpmanId = corpmanId;
	}
	
	public String getCorpmanPhone() {
	return corpmanPhone;
	}
	
	public void setCorpmanPhone(String corpmanPhone) {
	this.corpmanPhone = corpmanPhone;
	}
	
	public String getCorpmanMobile() {
	return corpmanMobile;
	}
	
	public void setCorpmanMobile(String corpmanMobile) {
	this.corpmanMobile = corpmanMobile;
	}
	
	public String getCorpmanEmail() {
	return corpmanEmail;
	}
	
	public void setCorpmanEmail(String corpmanEmail) {
	this.corpmanEmail = corpmanEmail;
	}
	
	public String getBankaccountNo() {
	return bankaccountNo;
	}
	
	public void setBankaccountNo(String bankaccountNo) {
	this.bankaccountNo = bankaccountNo;
	}
	
	public String getBankaccountName() {
	return bankaccountName;
	}
	
	public void setBankaccountName(String bankaccountName) {
	this.bankaccountName = bankaccountName;
	}
	
	public String getAutoCus() {
	return autoCus;
	}
	
	public void setAutoCus(String autoCus) {
	this.autoCus = autoCus;
	}
	
	public String getBankaccProp() {
	return bankaccProp;
	}
	
	public void setBankaccProp(String bankaccProp) {
	this.bankaccProp = bankaccProp;
	}
	
	public String getCertCode() {
	return certCode;
	}
	
	public void setCertCode(String certCode) {
	this.certCode = certCode;
	}
	
	public String getBankbranchNo() {
	return bankbranchNo;
	}
	
	public void setBankbranchNo(String bankbranchNo) {
	this.bankbranchNo = bankbranchNo;
	}
	
	public String getDefaultAcc() {
	return defaultAcc;
	}
	
	public void setDefaultAcc(String defaultAcc) {
	this.defaultAcc = defaultAcc;
	}
	
	public String getMerchantId() {
	return merchantId;
	}
	
	public void setMerchantId(String merchantId) {
	this.merchantId = merchantId;
	}
	
	public String getHandleType() {
	return handleType;
	}
	
	public void setHandleType(String handleType) {
	this.handleType = handleType;
	}
	
	public String getCycleValue() {
	return cycleValue;
	}
	
	public void setCycleValue(String cycleValue) {
	this.cycleValue = cycleValue;
	}
	
	public String getAllotFlag() {
	return allotFlag;
	}
	
	public void setAllotFlag(String allotFlag) {
	this.allotFlag = allotFlag;
	}
	
	public String getName() {
	return name;
	}
	
	public void setName(String name) {
	this.name = name;
	}
	
	public String getBankaccountType() {
	return bankaccountType;
	}
	
	public void setBankaccountType(String bankaccountType) {
	this.bankaccountType = bankaccountType;
	}
	
	public String getBusiCode() {
	return busiCode;
	}
	
	public void setBusiCode(String busiCode) {
	this.busiCode = busiCode;
	}
	
	public String getFutureRateType() {
	return futureRateType;
	}
	
	public void setFutureRateType(String futureRateType) {
	this.futureRateType = futureRateType;
	}
	
	public String getFutureRateValue() {
	return futureRateValue;
	}
	
	public void setFutureRateValue(String futureRateValue) {
	this.futureRateValue = futureRateValue;
	}
	
	public String getFutureMinAmount() {
	return futureMinAmount;
	}
	
	public void setFutureMinAmount(String futureMinAmount) {
	this.futureMinAmount = futureMinAmount;
	}
	
	public String getFutureMaxAmount() {
	return futureMaxAmount;
	}
	
	public void setFutureMaxAmount(String futureMaxAmount) {
	this.futureMaxAmount = futureMaxAmount;
	}
	
	public String getBankCardType() {
	return bankCardType;
	}
	
	public void setBankCardType(String bankCardType) {
	this.bankCardType = bankCardType;
	}
	
	public String getCertificateType() {
	return certificateType;
	}
	
	public void setCertificateType(String certificateType) {
	this.certificateType = certificateType;
	}
	
	public String getCertificateNo() {
	return certificateNo;
	}
	
	public void setCertificateNo(String certificateNo) {
	this.certificateNo = certificateNo;
	}
	
	public String getMobilePhone() {
	return mobilePhone;
	}
	
	public void setMobilePhone(String mobilePhone) {
	this.mobilePhone = mobilePhone;
	}
	
	public String getValid() {
	return valid;
	}
	
	public void setValid(String valid) {
	this.valid = valid;
	}
	
	public String getPin() {
	return pin;
	}
	
	public void setPin(String pin) {
	this.pin = pin;
	}
	
	public String getBankBranch() {
	return bankBranch;
	}
	
	public void setBankBranch(String bankBranch) {
	this.bankBranch = bankBranch;
	}
	
	public String getProvince() {
	return province;
	}
	
	public void setProvince(String province) {
	this.province = province;
	}
	
	public String getUserId() {
	return userId;
	}
	
	public void setUserId(String userId) {
	this.userId = userId;
	}
	
	public String getChildMerchantId() {
	return childMerchantId;
	}
	
	public void setChildMerchantId(String childMerchantId) {
	this.childMerchantId = childMerchantId;
	}
	
	public String getBindId() {
	return bindId;
	}
	
	public void setBindId(String bindId) {
	this.bindId = bindId;
	}
	
	public String getReckonCurrency() {
	return reckonCurrency;
	}
	
	public void setReckonCurrency(String reckonCurrency) {
	this.reckonCurrency = reckonCurrency;
	}
	
	public String getFcCardNo() {
	return fcCardNo;
	}
	
	public void setFcCardNo(String fcCardNo) {
	this.fcCardNo = fcCardNo;
	}
	
	public String getUserFee() {
	return userFee;
	}
	
	public void setUserFee(String userFee) {
	this.userFee = userFee;
	}
	
	public String getProductCategory() {
	return productCategory;
	}
	
	public void setProductCategory(String productCategory) {
	this.productCategory = productCategory;
	}
	
	public String getNotify_url() {
	return notify_url;
	}
	
	public void setNotify_url(String notify_url) {
	this.notify_url = notify_url;
	}
	
	public String getProductDesc() {
	return productDesc;
	}
	
	public void setProductDesc(String productDesc) {
	this.productDesc = productDesc;
	}
	
	public String getOriReqMsgId() {
	return oriReqMsgId;
	}
	
	public void setOriReqMsgId(String oriReqMsgId) {
	this.oriReqMsgId = oriReqMsgId;
	}
	
	public String getValidateCode() {
	return validateCode;
	}
	
	public void setValidateCode(String validateCode) {
	this.validateCode = validateCode;
	}
	
	public String getMobileNo() {
	return mobileNo;
	}
	
	public void setMobileNo(String mobileNo) {
	this.mobileNo = mobileNo;
	}
	
	public String getFrontUrl() {
	return frontUrl;
	}
	
	public void setFrontUrl(String frontUrl) {
	this.frontUrl = frontUrl;
	}
	
	public String getBackUrl() {
	return backUrl;
	}
	
	public void setBackUrl(String backUrl) {
	this.backUrl = backUrl;
	}
	
	public String getBusiness_code() {
	return business_code;
	}
	
	public void setBusiness_code(String business_code) {
	this.business_code = business_code;
	}
	
	public String getUser_id() {
	return user_id;
	}
	
	public void setUser_id(String user_id) {
	this.user_id = user_id;
	}
	
	public String getDF_type() {
	return DF_type;
	}
	
	public void setDF_type(String dF_type) {
	DF_type = dF_type;
	}
	
	public String getAccount_no() {
	return account_no;
	}
	
	public void setAccount_no(String account_no) {
	this.account_no = account_no;
	}
	
	public String getE_user_code() {
	return e_user_code;
	}
	
	public void setE_user_code(String e_user_code) {
	this.e_user_code = e_user_code;
	}
	
	public String getBank_code() {
	return bank_code;
	}
	
	public void setBank_code(String bank_code) {
	this.bank_code = bank_code;
	}
	
	public String getAccount_type() {
	return account_type;
	}
	
	public void setAccount_type(String account_type) {
	this.account_type = account_type;
	}
	
	public String getAllot_flag() {
	return allot_flag;
	}
	
	public void setAllot_flag(String allot_flag) {
	this.allot_flag = allot_flag;
	}
	
	public String getBank_name() {
	return bank_name;
	}
	
	public void setBank_name(String bank_name) {
	this.bank_name = bank_name;
	}
	
	public String getBank_type() {
	return bank_type;
	}
	
	public void setBank_type(String bank_type) {
	this.bank_type = bank_type;
	}
	
	public String getAccount_prop() {
	return account_prop;
	}
	
	public void setAccount_prop(String account_prop) {
	this.account_prop = account_prop;
	}
	
	public String getTerminal_no() {
	return terminal_no;
	}
	
	public void setTerminal_no(String terminal_no) {
	this.terminal_no = terminal_no;
	}
	
	public String getProtocol() {
	return protocol;
	}
	
	public void setProtocol(String protocol) {
	this.protocol = protocol;
	}
	
	public String getProtocol_userid() {
	return protocol_userid;
	}
	
	public void setProtocol_userid(String protocol_userid) {
	this.protocol_userid = protocol_userid;
	}
	
	public String getId_type() {
	return id_type;
	}
	
	public void setId_type(String id_type) {
	this.id_type = id_type;
	}
	
	public String getID() {
	return ID;
	}
	
	public void setID(String iD) {
	ID = iD;
	}
	
	public String getTel() {
	return tel;
	}
	
	public void setTel(String tel) {
	this.tel = tel;
	}
	
	public String getExtra_fee() {
	return extra_fee;
	}
	
	public void setExtra_fee(String extra_fee) {
	this.extra_fee = extra_fee;
	}
	
	public String getAccount_name() {
	return account_name;
	}
	
	public void setAccount_name(String account_name) {
	this.account_name = account_name;
	}
	
	public String getDeviceType() {
	return deviceType;
	}
	
	public void setDeviceType(String deviceType) {
	this.deviceType = deviceType;
	}
	
	public String getDeviceId() {
	return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
	this.deviceId = deviceId;
	}
	
	public String getUserIP() {
	return userIP;
	}
	
	public void setUserIP(String userIP) {
	this.userIP = userIP;
	}
	
	public String getBindValid() {
	return bindValid;
	}
	
	public void setBindValid(String bindValid) {
	this.bindValid = bindValid;
	}
	
	public String getReckonCurrent() {
	return reckonCurrent;
	}
	
	public void setReckonCurrent(String reckonCurrent) {
	this.reckonCurrent = reckonCurrent;
	}
	
	public String getQuery_sn() {
	return Query_sn;
	}
	
	public void setQuery_sn(String query_sn) {
	Query_sn = query_sn;
	}
	
	public String getQuery_remark() {
	return Query_remark;
	}
	
	public void setQuery_remark(String query_remark) {
	Query_remark = query_remark;
	}
	
	public String getBeginCreateDate() {
	return beginCreateDate;
	}
	
	public void setBeginCreateDate(String beginCreateDate) {
	this.beginCreateDate = beginCreateDate;
	}
	
	public String getEndCreateDate() {
	return endCreateDate;
	}
	
	public void setEndCreateDate(String endCreateDate) {
	this.endCreateDate = endCreateDate;
	}
	
	public String getPage() {
	return page;
	}
	
	public void setPage(String page) {
	this.page = page;
	}
	
	public String getPayStatus() {
	return payStatus;
	}
	
	public void setPayStatus(String payStatus) {
	this.payStatus = payStatus;
	}
	
	public String getSize() {
	return size;
	}
	
	public void setSize(String size) {
	this.size = size;
	}
	
	public String getOriPayMsgId() {
	return oriPayMsgId;
	}
	
	public void setOriPayMsgId(String oriPayMsgId) {
	this.oriPayMsgId = oriPayMsgId;
	}
	
	public String getBindUrl() {
	return bindUrl;
	}
	
	public void setBindUrl(String bindUrl) {
	this.bindUrl = bindUrl;
	}
	
	public String getFixAmount() {
	return fixAmount;
	}
	
	public void setFixAmount(String fixAmount) {
	this.fixAmount = fixAmount;
	}
	
	}
	*/

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // 1.将该标记放在属性上，如果该属性为NULL则不参与序列化
								// 2.如果放在类上边,那对这个类的全部属性起作用
public class CommonBean implements Serializable {

	/**
	 * 序列化版本号 根据类名、接口名、成员方法及属性等来生成一个64位的哈希字段
	 */
	private static final long serialVersionUID = 5016944159848569429L;
	// 交易类型
	private String transcode;

	private String settleBankCard;
	private String settleBankName;
	private String integralFlag;

	private String attachRateType;// 附加手续费类型
	private String attachRateValue;// 附加手续费率
									// 费率类型为1时：费率的百分数（支持4位小数），例如：上送1表示费率为1%费率类型为2时：填单笔金额（2位小数），例如：上送2表示每笔2.00元

	private String charge;
	// 商户号
	private String merchno;

	// 商户密钥
	private String deskey;

	// 商户订单号
	private String dsorderid;

	// 注册号或统一信用代码
	private String regno;

	// 公司名称
	private String compayname;

	// 企业法人
	private String frname;

	// 备注
	private String remark;

	// 签名
	private String sign;

	// 版本号
	private String version;

	// 流水号
	private String ordersn;

	// 返回码
	private String returncode;

	// 返回信息
	private String errtext;

	// 订单号
	private String orderid;

	// 证件号码
	private String idcard;
	// 证件类型
	private String idtype;
	// 银行卡号
	private String bankcard;
	// 手机号
	private String mobile;
	// 用户名
	private String username;

	// 人脸识别图1
	private String face1;

	// 人脸识别图2
	private String face2;

	// 相识度
	private String score;

	// 建议值
	private String threshold;
	// 建议最高
	private String thhigh;
	// 建议最低
	private String thlow;
	// 业务发生地
	private String businessplace;
	// 业务类型
	private String businesstype;

	// 头像 base64
	private String headimg;

	// 通道订单号
	private String chanorderid;

	// 图片路径
	private String photo;

	private String bankid;

	private String bankname;

	private String bankcode;

	private String bankcodename;

	private String cardtype;// 卡类型

	private String image;// 图片base64

	private String companycode;// 统一社会信用代码
	private String compaytype;// 类型
	private String regamount;// 注册资本
	private String createtime;// 成立日期
	private String place;// 住所
	private String beginexp;// 营业期限自
	private String endexp;// 营业期限至
	private String scope;// 经营范围
	private String regdept;// 登记机关
	private String approvetime;// 核准日期
	private String compaystatus;// 经营状态

	private String education;// 大学本科（简称“大学”）,
	private String code;// E000000,
	private String address;// 河北省邢台市桥东区东卫生街,
	private String gender;// 性别,
	private String nickname;// 别名、曾用名
	private String nation;// 汉族,
	private String birthplace;// 河北省邢台市桥东区,
	private String nativeplace;// 河北省邢台市沙河市,
	private String maritalstatus;// 婚姻状态 未婚,
	private String brithday;// 1986-02-20,
	private String company;// 工作单位

	private String carNum;// 是 违章车牌号（完整）
	private String carFrameNum;// 否 车架号（依据“1.2.1查询输入条件接口”）
	private String engineNum;// 否 发动机号（依据“1.2.1查询输入条件接口”）
	private String carType;// 否 车辆类型（见“2.2车辆分类”，不传则默认为小车“02”）
	private String provId;// 否 数据来源省份编码（依据“1.2.1查询输入条件接口”），不传则默认查询车牌归属地数据
	private String cityId;// 否 数据来源城市编码（同上）

	private String licenseno;// 否 驾驶证号码（扣车主本人分时需要填写）
	private String licensePhone;// 否 此车辆在车管所登记的手机号码（扣车主本人分时需要填写）

	private String returnData;//

	private String amount;// 金额
	private String accountnum;// 银行账户号
	private String accounttype;// 账户类型 00 银行卡 01 存折
	private String accountname;// 账户名称
	private String accountProperty;// 账户属性 00 对私 01 对公

	private BigDecimal currAccountBalance;// 当前账户余额
	private BigDecimal frozenamount;// 冻结金额

	private String provinceCode;// 省份编码
	private String cityCode;// 城市编码

	private BigDecimal oriTransAmount;// 原始交易金额
	private BigDecimal oriTransFee;// 原始交易手续费
	private BigDecimal oriTransTotalAmount;// 原始交易总金额

	private String linkorderid;//
	private String billtype;//

	private String requestUrl;//
	private String requestMethod;//
	private String requestCharset;//
	private String contextPath;//
	private Map<String, Object> parameterMap;//

	private String productName;// 商户名称
	private String appid;// 微信appid
	private String openid;// 用户openid

	private String formDataInfo;// 主装表单信息
	private String returnUrl;//
	private String notifyUrl;//
	private String status;//
	private String transtype;//
	private String currency;//
	private String paytime;//
	private String signType;//
	private String message;// 通道返回信息

	// 润联data
	private String materialNo;// 材料单号
	private String materialObj;// 材料对象
	private String materialType;// 材料类型
	private String materialNum;// 材料序号
	private String contEmail;// 联系人邮箱
	private String contName;// 联系人姓名
	private String contPhone;// contPhone
	private String orderDate;// 订单日期
	private String phoneNo;// 银行卡预留手机号
	private String customerName;// 持卡人姓名
	private String acctNo;// 卡号
	private String cvn2;// CVN2
	private String expDate;// 卡有效期
	private String terminalId;// 商户机具终端编号
	private String accessObject;// 接入对象
	private String signature;// 验签字段
	private String commodityName;// 商户名
	private String product;// 产品信息

	// 商旅快捷支付接口参数
	public String agentId;// 代理商号
	public String merId;// 商户号
	public String orderId;// 订单号
	public String txnTime;// 交易时间
	public String txnAmt;// 交易金额
	public String settleAmt;// 结算金额
	// 下游收下游的费率
	public String txnRate;
	// 下游收下游的单价
	public String siglePrice;
	// 每笔返回给下游的费用
	public String backFee;
	public String realName;// 真实姓名
	public String idCardNo;// 身份证号
	public String bankCardNo;// 银行卡号
	public String bankCode;// 银行代码
	public String phone;// 消费卡手机号
	public String expireDate;// 过期日期
	public String withdrawUrl;// 到账通知地址
	public String settleBankCode;// 结算银行代码
	public String settleCardNo;// 结算银行卡
	public String settlePhone;// 结算手机号
	public String bankName;// 消费卡银行名称
	public String settleCardBankName;// 结算卡银行名称
	// 支付宝
	public String service;// 支付类型
	public Integer fee;// 支付金额,正整数 订单金额，分
	public String cash;// 支付金额,自然数 顾客实付金额，分
	public String coupon;// 支付金额,顾客优惠金额，分
	private String agency_id;
	private String provider_id;
	private String trade_no;
	private String expire_minutes;
	private String shop_id;
	private String counter_id;
	private String operator_id;
	private String desc;
	private String coupon_tag;
	private String nonce_str;
	private String trade_barcode;

	private String bank_branch;
	private String bank_province;
	private String bank_city;
	private String bank_card;
	private String card_user;
	private String user_type;
	private String card_type;

	private String pay_info;
	private String pay_code;
	// KbPayData
	// 请求ip
	private String subMerNo;// 商户号
	private String merName;// 商户名称
	private String merState;// 商户所在省
	private String merCity;// 商户所在市
	private String merAddress;// 地址
	private String certType;// 证件类型01：身份证 02：军官证03：护照04：户口簿05：回乡证06：其他
	private String certId;// 证件号

	private String accountId;// 结算账号
	private String accountName;// 结算户名
	private String openBankName;// 开户行全称
	private String openBankCode;// 开户行联行号
	private String openBankState;// 开户行省份
	private String openBankCity;// 开户行城市

	private String operFlag;// 操作标识A.添加 M.修改
	private String t0drawFee;// 单笔D0提现交易手续费
	private String t0drawRate;// D0提现交易手续费扣率
	private String t1consFee;// 单笔消费交易手续费
	private String t1consRate;// 消费交易手续费扣率
	// 卡属性 (B.对公C.对私)
	private String cardAttribute;
	/**
	 * CREDIT：贷记卡 DEBIT：借记卡(暂时只支持信用卡) DEPOSIT：存折 BUZACC：对公账户 账号类型
	 */
	private String accType;// 账号类型
	private String orderNo;// 商户订单号
	private String orderAmount;// 订单金额
	private String retCode;// 商户号
	private String retMsg;// 商户订单号
	private String transStatus;// 交易状态
	private String telNo;// 手机号
	private String accNo;// 账号
	private String cardPassword;// 卡密
	private String valiDate;// 卡有效期
	private String merReserved;// 商户自定义域

	/**
	 * 持卡人
	 */
	private String cardOwner;
	/**
	 * 证件号码
	 */
	private String certNo;
	/**
	 * 付款用户IP地址
	 */
	private String payerIp;
	/**
	 * 卡类型(DEBIT.借记,CREDIT.贷记(信用卡)
	 * 
	 */
	private String cardType;
	// 交易状态 00未处理 01交易成功 02交易失败 03交易处理中 04未支付

	// KbData end

	// 反射方法名
	private String methodname;
	private String srcAmt;
	private String accountNumber;
	// private String tel;
	private String holderName;
	// private String idcard;
	private String cvv2;
	private String expired;
	// 费率
	private String fastpayFee;
	private String agencyType;

	private String bizOrderNumber;
	private String smsCode;
	private String extraFee;

	private String txn;

	// 子商户入驻
	private SubMer subMer;

	private String register;

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public SubMer getSubMer() {
		return subMer;
	}

	public void setSubMer(SubMer subMer) {
		this.subMer = subMer;
	}

	public String getTxn() {
		return txn;
	}

	public void setTxn(String txn) {
		this.txn = txn;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public String getSrcAmt() {
		return srcAmt;
	}

	public void setSrcAmt(String srcAmt) {
		this.srcAmt = srcAmt;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getExpired() {
		return expired;
	}

	public void setExpired(String expired) {
		this.expired = expired;
	}

	public String getFastpayFee() {
		return fastpayFee;
	}

	public void setFastpayFee(String fastpayFee) {
		this.fastpayFee = fastpayFee;
	}

	public String getAgencyType() {
		return agencyType;
	}

	public void setAgencyType(String agencyType) {
		this.agencyType = agencyType;
	}

	public String getBizOrderNumber() {
		return bizOrderNumber;
	}

	public void setBizOrderNumber(String bizOrderNumber) {
		this.bizOrderNumber = bizOrderNumber;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public String getTranscode() {
		return transcode;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public String getCoupon() {
		return coupon;
	}

	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}

	public String getAgency_id() {
		return agency_id;
	}

	public void setAgency_id(String agency_id) {
		this.agency_id = agency_id;
	}

	public String getProvider_id() {
		return provider_id;
	}

	public void setProvider_id(String provider_id) {
		this.provider_id = provider_id;
	}

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getExpire_minutes() {
		return expire_minutes;
	}

	public void setExpire_minutes(String expire_minutes) {
		this.expire_minutes = expire_minutes;
	}

	public String getShop_id() {
		return shop_id;
	}

	public void setShop_id(String shop_id) {
		this.shop_id = shop_id;
	}

	public String getCounter_id() {
		return counter_id;
	}

	public void setCounter_id(String counter_id) {
		this.counter_id = counter_id;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCoupon_tag() {
		return coupon_tag;
	}

	public void setCoupon_tag(String coupon_tag) {
		this.coupon_tag = coupon_tag;
	}

	public String getNonce_str() {
		return nonce_str;
	}

	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}

	public String getTrade_barcode() {
		return trade_barcode;
	}

	public void setTrade_barcode(String trade_barcode) {
		this.trade_barcode = trade_barcode;
	}

	public String getBank_branch() {
		return bank_branch;
	}

	public void setBank_branch(String bank_branch) {
		this.bank_branch = bank_branch;
	}

	public String getBank_province() {
		return bank_province;
	}

	public void setBank_province(String bank_province) {
		this.bank_province = bank_province;
	}

	public String getBank_city() {
		return bank_city;
	}

	public void setBank_city(String bank_city) {
		this.bank_city = bank_city;
	}

	public String getBank_card() {
		return bank_card;
	}

	public void setBank_card(String bank_card) {
		this.bank_card = bank_card;
	}

	public String getCard_user() {
		return card_user;
	}

	public void setCard_user(String card_user) {
		this.card_user = card_user;
	}

	public String getUser_type() {
		return user_type;
	}

	public void setUser_type(String user_type) {
		this.user_type = user_type;
	}

	public String getCard_type() {
		return card_type;
	}

	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}

	public void setTranscode(String transcode) {
		this.transcode = transcode;
	}

	public String getMerchno() {
		return merchno;
	}

	public void setMerchno(String merchno) {
		this.merchno = merchno;
	}

	public String getDsorderid() {
		return dsorderid;
	}

	public void setDsorderid(String dsorderid) {
		this.dsorderid = dsorderid;
	}

	public String getRegno() {
		return regno;
	}

	public void setRegno(String regno) {
		this.regno = regno;
	}

	public String getCompayname() {
		return compayname;
	}

	public void setCompayname(String compayname) {
		this.compayname = compayname;
	}

	public String getFrname() {
		return frname;
	}

	public void setFrname(String frname) {
		this.frname = frname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOrdersn() {
		return ordersn;
	}

	public void setOrdersn(String ordersn) {
		this.ordersn = ordersn;
	}

	public String getReturncode() {
		return returncode;
	}

	public void setReturncode(String returncode) {
		this.returncode = returncode;
	}

	public String getErrtext() {
		return errtext;
	}

	public void setErrtext(String errtext) {
		this.errtext = errtext;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getIdtype() {
		return idtype;
	}

	public void setIdtype(String idtype) {
		this.idtype = idtype;
	}

	public String getBankcard() {
		return bankcard;
	}

	public void setBankcard(String bankcard) {
		this.bankcard = bankcard;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFace1() {
		return face1;
	}

	public void setFace1(String face1) {
		this.face1 = face1;
	}

	public String getFace2() {
		return face2;
	}

	public void setFace2(String face2) {
		this.face2 = face2;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getThhigh() {
		return thhigh;
	}

	public void setThhigh(String thhigh) {
		this.thhigh = thhigh;
	}

	public String getThlow() {
		return thlow;
	}

	public void setThlow(String thlow) {
		this.thlow = thlow;
	}

	public String getBusinessplace() {
		return businessplace;
	}

	public void setBusinessplace(String businessplace) {
		this.businessplace = businessplace;
	}

	public String getBusinesstype() {
		return businesstype;
	}

	public void setBusinesstype(String businesstype) {
		this.businesstype = businesstype;
	}

	public String getHeadimg() {
		return headimg;
	}

	public void setHeadimg(String headimg) {
		this.headimg = headimg;
	}

	public String getChanorderid() {
		return chanorderid;
	}

	public void setChanorderid(String chanorderid) {
		this.chanorderid = chanorderid;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getBankid() {
		return bankid;
	}

	public void setBankid(String bankid) {
		this.bankid = bankid;
	}

	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	public String getBankcode() {
		return bankcode;
	}

	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}

	public String getBankcodename() {
		return bankcodename;
	}

	public void setBankcodename(String bankcodename) {
		this.bankcodename = bankcodename;
	}

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCompanycode() {
		return companycode;
	}

	public void setCompanycode(String companycode) {
		this.companycode = companycode;
	}

	public String getCompaytype() {
		return compaytype;
	}

	public void setCompaytype(String compaytype) {
		this.compaytype = compaytype;
	}

	public String getRegamount() {
		return regamount;
	}

	public void setRegamount(String regamount) {
		this.regamount = regamount;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getBeginexp() {
		return beginexp;
	}

	public void setBeginexp(String beginexp) {
		this.beginexp = beginexp;
	}

	public String getEndexp() {
		return endexp;
	}

	public void setEndexp(String endexp) {
		this.endexp = endexp;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getRegdept() {
		return regdept;
	}

	public void setRegdept(String regdept) {
		this.regdept = regdept;
	}

	public String getApprovetime() {
		return approvetime;
	}

	public void setApprovetime(String approvetime) {
		this.approvetime = approvetime;
	}

	public String getCompaystatus() {
		return compaystatus;
	}

	public void setCompaystatus(String compaystatus) {
		this.compaystatus = compaystatus;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNation() {
		return nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public void setBirthplace(String birthplace) {
		this.birthplace = birthplace;
	}

	public String getNativeplace() {
		return nativeplace;
	}

	public void setNativeplace(String nativeplace) {
		this.nativeplace = nativeplace;
	}

	public String getMaritalstatus() {
		return maritalstatus;
	}

	public void setMaritalstatus(String maritalstatus) {
		this.maritalstatus = maritalstatus;
	}

	public String getBrithday() {
		return brithday;
	}

	public void setBrithday(String brithday) {
		this.brithday = brithday;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCarNum() {
		return carNum;
	}

	public void setCarNum(String carNum) {
		this.carNum = carNum;
	}

	public String getCarFrameNum() {
		return carFrameNum;
	}

	public void setCarFrameNum(String carFrameNum) {
		this.carFrameNum = carFrameNum;
	}

	public String getEngineNum() {
		return engineNum;
	}

	public void setEngineNum(String engineNum) {
		this.engineNum = engineNum;
	}

	public String getCarType() {
		return carType;
	}

	public void setCarType(String carType) {
		this.carType = carType;
	}

	public String getProvId() {
		return provId;
	}

	public void setProvId(String provId) {
		this.provId = provId;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getLicenseno() {
		return licenseno;
	}

	public void setLicenseno(String licenseno) {
		this.licenseno = licenseno;
	}

	public String getLicensePhone() {
		return licensePhone;
	}

	public void setLicensePhone(String licensePhone) {
		this.licensePhone = licensePhone;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAccountnum() {
		return accountnum;
	}

	public void setAccountnum(String accountnum) {
		this.accountnum = accountnum;
	}

	public String getAccounttype() {
		return accounttype;
	}

	public void setAccounttype(String accounttype) {
		this.accounttype = accounttype;
	}

	public String getAccountname() {
		return accountname;
	}

	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}

	public String getAccountProperty() {
		return accountProperty;
	}

	public void setAccountProperty(String accountProperty) {
		this.accountProperty = accountProperty;
	}

	@JsonIgnore
	public BigDecimal getCurrAccountBalance() {
		return currAccountBalance;
	}

	public void setCurrAccountBalance(BigDecimal currAccountBalance) {
		this.currAccountBalance = currAccountBalance;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	@JsonIgnore
	public BigDecimal getOriTransAmount() {
		return oriTransAmount;
	}

	public void setOriTransAmount(BigDecimal oriTransAmount) {
		this.oriTransAmount = oriTransAmount;
	}

	@JsonIgnore
	public BigDecimal getOriTransFee() {
		return oriTransFee;
	}

	public void setOriTransFee(BigDecimal oriTransFee) {
		this.oriTransFee = oriTransFee;
	}

	@JsonIgnore
	public BigDecimal getOriTransTotalAmount() {
		return oriTransTotalAmount;
	}

	public void setOriTransTotalAmount(BigDecimal oriTransTotalAmount) {
		this.oriTransTotalAmount = oriTransTotalAmount;
	}

	public String getLinkorderid() {
		return linkorderid;
	}

	public void setLinkorderid(String linkorderid) {
		this.linkorderid = linkorderid;
	}

	public String getBilltype() {
		return billtype;
	}

	public void setBilltype(String billtype) {
		this.billtype = billtype;
	}

	@JsonIgnore
	public BigDecimal getFrozenamount() {
		return frozenamount;
	}

	public void setFrozenamount(BigDecimal frozenamount) {
		this.frozenamount = frozenamount;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestCharset() {
		return requestCharset;
	}

	public void setRequestCharset(String requestCharset) {
		this.requestCharset = requestCharset;
	}

	@JsonIgnore
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getFormDataInfo() {
		return formDataInfo;
	}

	public void setFormDataInfo(String formDataInfo) {
		this.formDataInfo = formDataInfo;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTranstype() {
		return transtype;
	}

	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPaytime() {
		return paytime;
	}

	public void setPaytime(String paytime) {
		this.paytime = paytime;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMaterialNo() {
		return materialNo;
	}

	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}

	public String getMaterialObj() {
		return materialObj;
	}

	public void setMaterialObj(String materialObj) {
		this.materialObj = materialObj;
	}

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}

	public String getMaterialNum() {
		return materialNum;
	}

	public void setMaterialNum(String materialNum) {
		this.materialNum = materialNum;
	}

	public String getContEmail() {
		return contEmail;
	}

	public void setContEmail(String contEmail) {
		this.contEmail = contEmail;
	}

	public String getContName() {
		return contName;
	}

	public void setContName(String contName) {
		this.contName = contName;
	}

	public String getContPhone() {
		return contPhone;
	}

	public void setContPhone(String contPhone) {
		this.contPhone = contPhone;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getAcctNo() {
		return acctNo;
	}

	public void setAcctNo(String acctNo) {
		this.acctNo = acctNo;
	}

	public String getCvn2() {
		return cvn2;
	}

	public void setCvn2(String cvn2) {
		this.cvn2 = cvn2;
	}

	public String getExpDate() {
		return expDate;
	}

	public void setExpDate(String expDate) {
		this.expDate = expDate;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	/*
	 * public RunLianProduct getProduct() { return product; }
	 * 
	 * public void setProduct(RunLianProduct product) { this.product = product;
	 * }
	 */
	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getCommodityName() {
		return commodityName;
	}

	public void setCommodityName(String commodityName) {
		this.commodityName = commodityName;
	}

	public String getAccessObject() {
		return accessObject;
	}

	public void setAccessObject(String accessObject) {
		this.accessObject = accessObject;
	}

	// 商旅
	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTxnTime() {
		return txnTime;
	}

	public void setTxnTime(String txnTime) {
		this.txnTime = txnTime;
	}

	public String getTxnAmt() {
		return txnAmt;
	}

	public void setTxnAmt(String txnAmt) {
		this.txnAmt = txnAmt;
	}

	public String getSettleAmt() {
		return settleAmt;
	}

	public void setSettleAmt(String settleAmt) {
		this.settleAmt = settleAmt;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public String getBankCardNo() {
		return bankCardNo;
	}

	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}

	public String getWithdrawUrl() {
		return withdrawUrl;
	}

	public void setWithdrawUrl(String withdrawUrl) {
		this.withdrawUrl = withdrawUrl;
	}

	public String getSettleBankCode() {
		return settleBankCode;
	}

	public void setSettleBankCode(String settleBankCode) {
		this.settleBankCode = settleBankCode;
	}

	public String getSettleCardNo() {
		return settleCardNo;
	}

	public void setSettleCardNo(String settleCardNo) {
		this.settleCardNo = settleCardNo;
	}

	public String getSettlePhone() {
		return settlePhone;
	}

	public void setSettlePhone(String settlePhone) {
		this.settlePhone = settlePhone;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getSettleCardBankName() {
		return settleCardBankName;
	}

	public void setSettleCardBankName(String settleCardBankName) {
		this.settleCardBankName = settleCardBankName;
	}

	public String getPayerIp() {
		return payerIp;
	}

	public void setPayerIp(String payerIp) {
		this.payerIp = payerIp;
	}

	// KbData start
	public String getSubMerNo() {
		return subMerNo;
	}

	public void setSubMerNo(String subMerNo) {
		this.subMerNo = subMerNo;
	}

	public String getMerName() {
		return merName;
	}

	public void setMerName(String merName) {
		this.merName = merName;
	}

	public String getMerState() {
		return merState;
	}

	public void setMerState(String merState) {
		this.merState = merState;
	}

	public String getMerCity() {
		return merCity;
	}

	public void setMerCity(String merCity) {
		this.merCity = merCity;
	}

	public String getMerAddress() {
		return merAddress;
	}

	public void setMerAddress(String merAddress) {
		this.merAddress = merAddress;
	}

	public String getCertType() {
		return certType;
	}

	public void setCertType(String certType) {
		this.certType = certType;
	}

	public String getCertId() {
		return certId;
	}

	public void setCertId(String certId) {
		this.certId = certId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getOpenBankName() {
		return openBankName;
	}

	public void setOpenBankName(String openBankName) {
		this.openBankName = openBankName;
	}

	public String getOpenBankCode() {
		return openBankCode;
	}

	public void setOpenBankCode(String openBankCode) {
		this.openBankCode = openBankCode;
	}

	public String getOpenBankState() {
		return openBankState;
	}

	public void setOpenBankState(String openBankState) {
		this.openBankState = openBankState;
	}

	public String getOpenBankCity() {
		return openBankCity;
	}

	public void setOpenBankCity(String openBankCity) {
		this.openBankCity = openBankCity;
	}

	public String getOperFlag() {
		return operFlag;
	}

	public void setOperFlag(String operFlag) {
		this.operFlag = operFlag;
	}

	public String getT0drawFee() {
		return t0drawFee;
	}

	public void setT0drawFee(String t0drawFee) {
		this.t0drawFee = t0drawFee;
	}

	public String getT0drawRate() {
		return t0drawRate;
	}

	public void setT0drawRate(String t0drawRate) {
		this.t0drawRate = t0drawRate;
	}

	public String getT1consFee() {
		return t1consFee;
	}

	public void setT1consFee(String t1consFee) {
		this.t1consFee = t1consFee;
	}

	public String getT1consRate() {
		return t1consRate;
	}

	public void setT1consRate(String t1consRate) {
		this.t1consRate = t1consRate;
	}

	public String getCardAttribute() {
		return cardAttribute;
	}

	public void setCardAttribute(String cardAttribute) {
		this.cardAttribute = cardAttribute;
	}

	public String getAccType() {
		return accType;
	}

	public void setAccType(String accType) {
		this.accType = accType;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}

	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public String getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}

	public String getTelNo() {
		return telNo;
	}

	public void setTelNo(String telNo) {
		this.telNo = telNo;
	}

	public String getAccNo() {
		return accNo;
	}

	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}

	public String getCardPassword() {
		return cardPassword;
	}

	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}

	public String getValiDate() {
		return valiDate;
	}

	public void setValiDate(String valiDate) {
		this.valiDate = valiDate;
	}

	public String getMerReserved() {
		return merReserved;
	}

	public void setMerReserved(String merReserved) {
		this.merReserved = merReserved;
	}

	public String getCardOwner() {
		return cardOwner;
	}

	public void setCardOwner(String cardOwner) {
		this.cardOwner = cardOwner;
	}

	public String getCertNo() {
		return certNo;
	}

	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getDeskey() {
		return deskey;
	}

	public void setDeskey(String deskey) {
		this.deskey = deskey;
	}

	public String getTxnRate() {
		return txnRate;
	}

	public void setTxnRate(String txnRate) {
		this.txnRate = txnRate;
	}

	public String getSiglePrice() {
		return siglePrice;
	}

	public void setSiglePrice(String siglePrice) {
		this.siglePrice = siglePrice;
	}

	public String getBackFee() {
		return backFee;
	}

	public void setBackFee(String backFee) {
		this.backFee = backFee;
	}

	// KbData end

	// Ght start
	private String merchantName;// 商户名称

	private String shortName;// 商户简称

	private String city;// 商户城市

	private String name;// 持卡人姓名

	private String bankaccountType;// 银行卡类型

	private String merchantAddress;// 商户地址

	private String servicePhone;// 客服电话

	private String orgCode;// 组织机构代码

	private String merchantType;// 商户类型

	private String category;// 经营类目

	private String corpmanName;// 法人姓名

	private String corpmanId;// 法人身份证

	private String corpmanPhone;// 法人联系电话

	private String corpmanMobile;// 法人联系手机

	private String corpmanEmail;// 法人邮箱

	private String bankaccountNo;// 开户行账号

	private String bankaccountName;// 开户户名

	private String autoCus;// 自动提现

	private String bankaccProp;// 账户属性

	private String certCode;// 办卡证件类型

	private String bankbranchNo;// 联行号

	private String defaultAcc;// 默认账户

	private String merchantId;// 子商户编码

	private String handleType;// 操作类型

	private String cycleValue;// 结算周期

	private String allotFlag;// 机构资金调拨 机构资金调拨

	private String busiCode;// 开通业务

	private String futureRateType;// 费率类型

	private String futureRateValue;// 费率

	private String futureMinAmount;// 保底

	private String futureMaxAmount;// 封顶

	private String fixAmount;// 封顶

	private String bankCardType;// 银行卡类型

	private String certificateType;// 证件类型

	private String certificateNo;// 证件号码

	private String mobilePhone;// 手机号码

	private String valid;// 有效期

	private String pin;// 卡交易密码

	private String bankBranch;// 支行信息

	private String province;// 所属省

	private String userId;// 商户用户标识

	private String childMerchantId;// 子商户号

	private String bindId;// 绑卡ID（签约编号）

	private String channelType;
	
	private String reckonCurrency;// 清算币种

	private String fcCardNo;// 入金卡号

	private String userFee;// 用户手续费

	private String productCategory;// 商品类别

	private String notify_url;// 异步通知地址

	private String productDesc;// 商品名称

	private String oriReqMsgId;// 原请求交易流水号

	private String validateCode;// 验证码

	private String mobileNo;// 银行卡预留手机号

	private String frontUrl;// 前台跳转地址

	private String backUrl;// 后台跳转地址

	private String licenseNo;// 营业执照

	private String taxRegisterNo;// 税务登记证号码

	private String settingSettCard;// 结算卡设定 用于设定本接口传的银行卡是否设定为结算卡：0或null-不设定；1-设定

	private String addrType;// 地址类型
	private String contactType;// 联系人类型
								// OTHER:其他LEGAL_PERSON:法人CONTROLLER:实际控制人AGENT:代理人
	private String childEnter;// 是否使用子商户入驻渠道
	private String mcc;// 行业编码（用于微信支付宝等第三方支付业务）
	private String licenseType;// 营业执照类型
								// NATIONAL_LEGAL_MERGE:营业执照（多证合一）NATIONAL_LEGAL:营业执照
								// INST_RGST_CTF:事业单位法人证书
	// 支付授权目录 最多设置2个支付授权目录, 且域名必
	// 须通过ICP备案，以“|”分割
	// 头部要包含http或https，须细化到二
	// 级或三级目录，以左斜杠“/”结尾
	// 如：
	// http://mp.weixin.qq.com/wiki/|
	// https://mp.weixin.qq.com/cgi-bin/
	// 暂不提供修改接口，请谨慎上传
	private String authPayDir;

	private String scribeAppid;// 推荐关注的公众号

	private String contactMan;// 联系人

	private String email;// 联系人邮箱

	private String licenseBeginDate;// 营业执照开始时间

	private String licenseEndDate;// 营业执照到期时间

	private String licenseRange;// 营业执照经营范围

	private String merchNo;// 子商户号

	private String balanceAmount;// 账号余额

	private String freezeAmount;// 冻结金额
	// 代付

	private String business_code;//

	private String user_id;//

	private String DF_type;//

	private String account_no;//

	private String e_user_code;//

	private String bank_code;//

	private String account_type;//

	private String allot_flag;//

	private String bank_name;//

	private String bank_type;//

	private String account_prop;//

	private String terminal_no;//

	private String protocol;//

	private String protocol_userid;//

	private String id_type;//

	private String ID;//

	private String tel;//

	private String extra_fee;//

	private String account_name;//

	private String deviceType;// 设备类型
	private String deviceId;// 设备号
	private String userIP;// 客户端Ip
	private String bindValid;// 绑卡有效期
	private String Query_sn;// 要查询的交易流水
	private String Query_remark;// 查询备注
	private String reckonCurrent;// 清算币种

	private String beginCreateDate;// 开始日期

	private String endCreateDate;// 结束日期
	private String page;// 分页数
	private String bindUrl;// 银联绑卡页面
	private String payStatus;// 支付结果
	private String size;// 每页大小
	private String oriPayMsgId;// 原平台交易（确认支付）流水号

	private String chnSerialNo;// 创建商户池 后我司会提供一个创建商户池,该字段对于表中的商户号
	private String categoryUnion;// 银联行业类型
	private String inviteMerNo;// 如果为实体商户，且为连锁商户，则该商户号必填
	private String subMerchantNo;// 子商户号

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getMerchantAddress() {
		return merchantAddress;
	}

	public void setMerchantAddress(String merchantAddress) {
		this.merchantAddress = merchantAddress;
	}

	public String getServicePhone() {
		return servicePhone;
	}

	public void setServicePhone(String servicePhone) {
		this.servicePhone = servicePhone;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCorpmanName() {
		return corpmanName;
	}

	public void setCorpmanName(String corpmanName) {
		this.corpmanName = corpmanName;
	}

	public String getCorpmanId() {
		return corpmanId;
	}

	public void setCorpmanId(String corpmanId) {
		this.corpmanId = corpmanId;
	}

	public String getCorpmanPhone() {
		return corpmanPhone;
	}

	public void setCorpmanPhone(String corpmanPhone) {
		this.corpmanPhone = corpmanPhone;
	}

	public String getCorpmanMobile() {
		return corpmanMobile;
	}

	public void setCorpmanMobile(String corpmanMobile) {
		this.corpmanMobile = corpmanMobile;
	}

	public String getCorpmanEmail() {
		return corpmanEmail;
	}

	public void setCorpmanEmail(String corpmanEmail) {
		this.corpmanEmail = corpmanEmail;
	}

	public String getBankaccountNo() {
		return bankaccountNo;
	}

	public void setBankaccountNo(String bankaccountNo) {
		this.bankaccountNo = bankaccountNo;
	}

	public String getBankaccountName() {
		return bankaccountName;
	}

	public void setBankaccountName(String bankaccountName) {
		this.bankaccountName = bankaccountName;
	}

	public String getAutoCus() {
		return autoCus;
	}

	public void setAutoCus(String autoCus) {
		this.autoCus = autoCus;
	}

	public String getBankaccProp() {
		return bankaccProp;
	}

	public void setBankaccProp(String bankaccProp) {
		this.bankaccProp = bankaccProp;
	}

	public String getCertCode() {
		return certCode;
	}

	public void setCertCode(String certCode) {
		this.certCode = certCode;
	}

	public String getBankbranchNo() {
		return bankbranchNo;
	}

	public void setBankbranchNo(String bankbranchNo) {
		this.bankbranchNo = bankbranchNo;
	}

	public String getDefaultAcc() {
		return defaultAcc;
	}

	public void setDefaultAcc(String defaultAcc) {
		this.defaultAcc = defaultAcc;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getHandleType() {
		return handleType;
	}

	public void setHandleType(String handleType) {
		this.handleType = handleType;
	}

	public String getCycleValue() {
		return cycleValue;
	}

	public void setCycleValue(String cycleValue) {
		this.cycleValue = cycleValue;
	}

	public String getAllotFlag() {
		return allotFlag;
	}

	public void setAllotFlag(String allotFlag) {
		this.allotFlag = allotFlag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBankaccountType() {
		return bankaccountType;
	}

	public void setBankaccountType(String bankaccountType) {
		this.bankaccountType = bankaccountType;
	}

	public String getBusiCode() {
		return busiCode;
	}

	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}

	public String getFutureRateType() {
		return futureRateType;
	}

	public void setFutureRateType(String futureRateType) {
		this.futureRateType = futureRateType;
	}

	public String getFutureRateValue() {
		return futureRateValue;
	}

	public void setFutureRateValue(String futureRateValue) {
		this.futureRateValue = futureRateValue;
	}

	public String getFutureMinAmount() {
		return futureMinAmount;
	}

	public void setFutureMinAmount(String futureMinAmount) {
		this.futureMinAmount = futureMinAmount;
	}

	public String getFutureMaxAmount() {
		return futureMaxAmount;
	}

	public void setFutureMaxAmount(String futureMaxAmount) {
		this.futureMaxAmount = futureMaxAmount;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getBankCardType() {
		return bankCardType;
	}

	public void setBankCardType(String bankCardType) {
		this.bankCardType = bankCardType;
	}

	public String getCertificateType() {
		return certificateType;
	}

	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	public String getCertificateNo() {
		return certificateNo;
	}

	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getBankBranch() {
		return bankBranch;
	}

	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getChildMerchantId() {
		return childMerchantId;
	}

	public void setChildMerchantId(String childMerchantId) {
		this.childMerchantId = childMerchantId;
	}

	public String getBindId() {
		return bindId;
	}

	public void setBindId(String bindId) {
		this.bindId = bindId;
	}

	public String getReckonCurrency() {
		return reckonCurrency;
	}

	public void setReckonCurrency(String reckonCurrency) {
		this.reckonCurrency = reckonCurrency;
	}

	public String getFcCardNo() {
		return fcCardNo;
	}

	public void setFcCardNo(String fcCardNo) {
		this.fcCardNo = fcCardNo;
	}

	public String getUserFee() {
		return userFee;
	}

	public void setUserFee(String userFee) {
		this.userFee = userFee;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public String getOriReqMsgId() {
		return oriReqMsgId;
	}

	public void setOriReqMsgId(String oriReqMsgId) {
		this.oriReqMsgId = oriReqMsgId;
	}

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getFrontUrl() {
		return frontUrl;
	}

	public void setFrontUrl(String frontUrl) {
		this.frontUrl = frontUrl;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public String getBusiness_code() {
		return business_code;
	}

	public void setBusiness_code(String business_code) {
		this.business_code = business_code;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getDF_type() {
		return DF_type;
	}

	public void setDF_type(String dF_type) {
		DF_type = dF_type;
	}

	public String getAccount_no() {
		return account_no;
	}

	public void setAccount_no(String account_no) {
		this.account_no = account_no;
	}

	public String getE_user_code() {
		return e_user_code;
	}

	public void setE_user_code(String e_user_code) {
		this.e_user_code = e_user_code;
	}

	public String getBank_code() {
		return bank_code;
	}

	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}

	public String getAccount_type() {
		return account_type;
	}

	public void setAccount_type(String account_type) {
		this.account_type = account_type;
	}

	public String getAllot_flag() {
		return allot_flag;
	}

	public void setAllot_flag(String allot_flag) {
		this.allot_flag = allot_flag;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_type() {
		return bank_type;
	}

	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
	}

	public String getAccount_prop() {
		return account_prop;
	}

	public void setAccount_prop(String account_prop) {
		this.account_prop = account_prop;
	}

	public String getTerminal_no() {
		return terminal_no;
	}

	public void setTerminal_no(String terminal_no) {
		this.terminal_no = terminal_no;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol_userid() {
		return protocol_userid;
	}

	public void setProtocol_userid(String protocol_userid) {
		this.protocol_userid = protocol_userid;
	}

	public String getId_type() {
		return id_type;
	}

	public void setId_type(String id_type) {
		this.id_type = id_type;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getExtra_fee() {
		return extra_fee;
	}

	public void setExtra_fee(String extra_fee) {
		this.extra_fee = extra_fee;
	}

	public String getAccount_name() {
		return account_name;
	}

	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getUserIP() {
		return userIP;
	}

	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}

	public String getBindValid() {
		return bindValid;
	}

	public void setBindValid(String bindValid) {
		this.bindValid = bindValid;
	}

	public String getReckonCurrent() {
		return reckonCurrent;
	}

	public void setReckonCurrent(String reckonCurrent) {
		this.reckonCurrent = reckonCurrent;
	}

	public String getQuery_sn() {
		return Query_sn;
	}

	public void setQuery_sn(String query_sn) {
		Query_sn = query_sn;
	}

	public String getQuery_remark() {
		return Query_remark;
	}

	public void setQuery_remark(String query_remark) {
		Query_remark = query_remark;
	}

	public String getBeginCreateDate() {
		return beginCreateDate;
	}

	public void setBeginCreateDate(String beginCreateDate) {
		this.beginCreateDate = beginCreateDate;
	}

	public String getEndCreateDate() {
		return endCreateDate;
	}

	public void setEndCreateDate(String endCreateDate) {
		this.endCreateDate = endCreateDate;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getOriPayMsgId() {
		return oriPayMsgId;
	}

	public void setOriPayMsgId(String oriPayMsgId) {
		this.oriPayMsgId = oriPayMsgId;
	}

	public String getBindUrl() {
		return bindUrl;
	}

	public void setBindUrl(String bindUrl) {
		this.bindUrl = bindUrl;
	}

	public String getFixAmount() {
		return fixAmount;
	}

	public void setFixAmount(String fixAmount) {
		this.fixAmount = fixAmount;
	}

	public String getLicenseNo() {
		return licenseNo;
	}

	public void setLicenseNo(String licenseNo) {
		this.licenseNo = licenseNo;
	}

	public String getTaxRegisterNo() {
		return taxRegisterNo;
	}

	public void setTaxRegisterNo(String taxRegisterNo) {
		this.taxRegisterNo = taxRegisterNo;
	}

	public String getSettingSettCard() {
		return settingSettCard;
	}

	public void setSettingSettCard(String settingSettCard) {
		this.settingSettCard = settingSettCard;
	}

	public String getAddrType() {
		return addrType;
	}

	public void setAddrType(String addrType) {
		this.addrType = addrType;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getChildEnter() {
		return childEnter;
	}

	public void setChildEnter(String childEnter) {
		this.childEnter = childEnter;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(String licenseType) {
		this.licenseType = licenseType;
	}

	public String getAuthPayDir() {
		return authPayDir;
	}

	public void setAuthPayDir(String authPayDir) {
		this.authPayDir = authPayDir;
	}

	public String getScribeAppid() {
		return scribeAppid;
	}

	public void setScribeAppid(String scribeAppid) {
		this.scribeAppid = scribeAppid;
	}

	public String getContactMan() {
		return contactMan;
	}

	public void setContactMan(String contactMan) {
		this.contactMan = contactMan;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLicenseBeginDate() {
		return licenseBeginDate;
	}

	public void setLicenseBeginDate(String licenseBeginDate) {
		this.licenseBeginDate = licenseBeginDate;
	}

	public String getLicenseEndDate() {
		return licenseEndDate;
	}

	public void setLicenseEndDate(String licenseEndDate) {
		this.licenseEndDate = licenseEndDate;
	}

	public String getLicenseRange() {
		return licenseRange;
	}

	public void setLicenseRange(String licenseRange) {
		this.licenseRange = licenseRange;
	}

	public String getMerchNo() {
		return merchNo;
	}

	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}

	// Ght end
	public String getIntegralFlag() {
		return integralFlag;
	}

	public void setIntegralFlag(String integralFlag) {
		this.integralFlag = integralFlag;
	}

	public String getCharge() {
		return charge;
	}

	public void setCharge(String charge) {
		this.charge = charge;
	}

	public String getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(String balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	public String getFreezeAmount() {
		return freezeAmount;
	}

	public void setFreezeAmount(String freezeAmount) {
		this.freezeAmount = freezeAmount;
	}

	public String getReturnData() {
		return returnData;
	}

	public void setReturnData(String returnData) {
		this.returnData = returnData;
	}

	public String getPay_info() {
		return pay_info;
	}

	public void setPay_info(String pay_info) {
		this.pay_info = pay_info;
	}

	public String getPay_code() {
		return pay_code;
	}

	public void setPay_code(String pay_code) {
		this.pay_code = pay_code;
	}

	public String getChnSerialNo() {
		return chnSerialNo;
	}

	public void setChnSerialNo(String chnSerialNo) {
		this.chnSerialNo = chnSerialNo;
	}

	public String getCategoryUnion() {
		return categoryUnion;
	}

	public void setCategoryUnion(String categoryUnion) {
		this.categoryUnion = categoryUnion;
	}

	public String getInviteMerNo() {
		return inviteMerNo;
	}

	public void setInviteMerNo(String inviteMerNo) {
		this.inviteMerNo = inviteMerNo;
	}

	public String getSubMerchantNo() {
		return subMerchantNo;
	}

	public void setSubMerchantNo(String subMerchantNo) {
		this.subMerchantNo = subMerchantNo;
	}

	public String getAttachRateType() {
		return attachRateType;
	}

	public void setAttachRateType(String attachRateType) {
		this.attachRateType = attachRateType;
	}

	public String getAttachRateValue() {
		return attachRateValue;
	}

	public void setAttachRateValue(String attachRateValue) {
		this.attachRateValue = attachRateValue;
	}

	public String getSettleBankCard() {
		return settleBankCard;
	}

	public void setSettleBankCard(String settleBankCard) {
		this.settleBankCard = settleBankCard;
	}

	public String getSettleBankName() {
		return settleBankName;
	}

	public void setSettleBankName(String settleBankName) {
		this.settleBankName = settleBankName;
	}

	@Override
	public String toString() {
		return "CommonBean [transcode=" + transcode + ", settleBankCard=" + settleBankCard + ", settleBankName="
				+ settleBankName + ", integralFlag=" + integralFlag + ", attachRateType=" + attachRateType
				+ ", attachRateValue=" + attachRateValue + ", charge=" + charge + ", merchno=" + merchno + ", deskey="
				+ deskey + ", dsorderid=" + dsorderid + ", regno=" + regno + ", compayname=" + compayname + ", frname="
				+ frname + ", remark=" + remark + ", sign=" + sign + ", version=" + version + ", ordersn=" + ordersn
				+ ", returncode=" + returncode + ", errtext=" + errtext + ", orderid=" + orderid + ", idcard=" + idcard
				+ ", idtype=" + idtype + ", bankcard=" + bankcard + ", mobile=" + mobile + ", username=" + username
				+ ", face1=" + face1 + ", face2=" + face2 + ", score=" + score + ", threshold=" + threshold
				+ ", thhigh=" + thhigh + ", thlow=" + thlow + ", businessplace=" + businessplace + ", businesstype="
				+ businesstype + ", headimg=" + headimg + ", chanorderid=" + chanorderid + ", photo=" + photo
				+ ", bankid=" + bankid + ", bankname=" + bankname + ", bankcode=" + bankcode + ", bankcodename="
				+ bankcodename + ", cardtype=" + cardtype + ", image=" + image + ", companycode=" + companycode
				+ ", compaytype=" + compaytype + ", regamount=" + regamount + ", createtime=" + createtime + ", place="
				+ place + ", beginexp=" + beginexp + ", endexp=" + endexp + ", scope=" + scope + ", regdept=" + regdept
				+ ", approvetime=" + approvetime + ", compaystatus=" + compaystatus + ", education=" + education
				+ ", code=" + code + ", address=" + address + ", gender=" + gender + ", nickname=" + nickname
				+ ", nation=" + nation + ", birthplace=" + birthplace + ", nativeplace=" + nativeplace
				+ ", maritalstatus=" + maritalstatus + ", brithday=" + brithday + ", company=" + company + ", carNum="
				+ carNum + ", carFrameNum=" + carFrameNum + ", engineNum=" + engineNum + ", carType=" + carType
				+ ", provId=" + provId + ", cityId=" + cityId + ", licenseno=" + licenseno + ", licensePhone="
				+ licensePhone + ", returnData=" + returnData + ", amount=" + amount + ", accountnum=" + accountnum
				+ ", accounttype=" + accounttype + ", accountname=" + accountname + ", accountProperty="
				+ accountProperty + ", currAccountBalance=" + currAccountBalance + ", frozenamount=" + frozenamount
				+ ", provinceCode=" + provinceCode + ", cityCode=" + cityCode + ", oriTransAmount=" + oriTransAmount
				+ ", oriTransFee=" + oriTransFee + ", oriTransTotalAmount=" + oriTransTotalAmount + ", linkorderid="
				+ linkorderid + ", billtype=" + billtype + ", requestUrl=" + requestUrl + ", requestMethod="
				+ requestMethod + ", requestCharset=" + requestCharset + ", contextPath=" + contextPath
				+ ", parameterMap=" + parameterMap + ", productName=" + productName + ", appid=" + appid + ", openid="
				+ openid + ", formDataInfo=" + formDataInfo + ", returnUrl=" + returnUrl + ", notifyUrl=" + notifyUrl
				+ ", status=" + status + ", transtype=" + transtype + ", currency=" + currency + ", paytime=" + paytime
				+ ", signType=" + signType + ", message=" + message + ", materialNo=" + materialNo + ", materialObj="
				+ materialObj + ", materialType=" + materialType + ", materialNum=" + materialNum + ", contEmail="
				+ contEmail + ", contName=" + contName + ", contPhone=" + contPhone + ", orderDate=" + orderDate
				+ ", phoneNo=" + phoneNo + ", customerName=" + customerName + ", acctNo=" + acctNo + ", cvn2=" + cvn2
				+ ", expDate=" + expDate + ", terminalId=" + terminalId + ", accessObject=" + accessObject
				+ ", signature=" + signature + ", commodityName=" + commodityName + ", product=" + product
				+ ", agentId=" + agentId + ", merId=" + merId + ", orderId=" + orderId + ", txnTime=" + txnTime
				+ ", txnAmt=" + txnAmt + ", settleAmt=" + settleAmt + ", txnRate=" + txnRate + ", siglePrice="
				+ siglePrice + ", backFee=" + backFee + ", realName=" + realName + ", idCardNo=" + idCardNo
				+ ", bankCardNo=" + bankCardNo + ", bankCode=" + bankCode + ", phone=" + phone + ", expireDate="
				+ expireDate + ", withdrawUrl=" + withdrawUrl + ", settleBankCode=" + settleBankCode + ", settleCardNo="
				+ settleCardNo + ", settlePhone=" + settlePhone + ", bankName=" + bankName + ", settleCardBankName="
				+ settleCardBankName + ", service=" + service + ", fee=" + fee + ", cash=" + cash + ", coupon=" + coupon
				+ ", agency_id=" + agency_id + ", provider_id=" + provider_id + ", trade_no=" + trade_no
				+ ", expire_minutes=" + expire_minutes + ", shop_id=" + shop_id + ", counter_id=" + counter_id
				+ ", operator_id=" + operator_id + ", desc=" + desc + ", coupon_tag=" + coupon_tag + ", nonce_str="
				+ nonce_str + ", trade_barcode=" + trade_barcode + ", bank_branch=" + bank_branch + ", bank_province="
				+ bank_province + ", bank_city=" + bank_city + ", bank_card=" + bank_card + ", card_user=" + card_user
				+ ", user_type=" + user_type + ", card_type=" + card_type + ", pay_info=" + pay_info + ", pay_code="
				+ pay_code + ", subMerNo=" + subMerNo + ", merName=" + merName + ", merState=" + merState + ", merCity="
				+ merCity + ", merAddress=" + merAddress + ", certType=" + certType + ", certId=" + certId
				+ ", accountId=" + accountId + ", accountName=" + accountName + ", openBankName=" + openBankName
				+ ", openBankCode=" + openBankCode + ", openBankState=" + openBankState + ", openBankCity="
				+ openBankCity + ", operFlag=" + operFlag + ", t0drawFee=" + t0drawFee + ", t0drawRate=" + t0drawRate
				+ ", t1consFee=" + t1consFee + ", t1consRate=" + t1consRate + ", cardAttribute=" + cardAttribute
				+ ", accType=" + accType + ", orderNo=" + orderNo + ", orderAmount=" + orderAmount + ", retCode="
				+ retCode + ", retMsg=" + retMsg + ", transStatus=" + transStatus + ", telNo=" + telNo + ", accNo="
				+ accNo + ", cardPassword=" + cardPassword + ", valiDate=" + valiDate + ", merReserved=" + merReserved
				+ ", cardOwner=" + cardOwner + ", certNo=" + certNo + ", payerIp=" + payerIp + ", cardType=" + cardType
				+ ", methodname=" + methodname + ", srcAmt=" + srcAmt + ", accountNumber=" + accountNumber
				+ ", holderName=" + holderName + ", cvv2=" + cvv2 + ", expired=" + expired + ", fastpayFee="
				+ fastpayFee + ", agencyType=" + agencyType + ", bizOrderNumber=" + bizOrderNumber + ", smsCode="
				+ smsCode + ", extraFee=" + extraFee + ", txn=" + txn + ", subMer=" + subMer + ", register=" + register
				+ ", merchantName=" + merchantName + ", shortName=" + shortName + ", city=" + city + ", name=" + name
				+ ", bankaccountType=" + bankaccountType + ", merchantAddress=" + merchantAddress + ", servicePhone="
				+ servicePhone + ", orgCode=" + orgCode + ", merchantType=" + merchantType + ", category=" + category
				+ ", corpmanName=" + corpmanName + ", corpmanId=" + corpmanId + ", corpmanPhone=" + corpmanPhone
				+ ", corpmanMobile=" + corpmanMobile + ", corpmanEmail=" + corpmanEmail + ", bankaccountNo="
				+ bankaccountNo + ", bankaccountName=" + bankaccountName + ", autoCus=" + autoCus + ", bankaccProp="
				+ bankaccProp + ", certCode=" + certCode + ", bankbranchNo=" + bankbranchNo + ", defaultAcc="
				+ defaultAcc + ", merchantId=" + merchantId + ", handleType=" + handleType + ", cycleValue="
				+ cycleValue + ", allotFlag=" + allotFlag + ", busiCode=" + busiCode + ", futureRateType="
				+ futureRateType + ", futureRateValue=" + futureRateValue + ", futureMinAmount=" + futureMinAmount
				+ ", futureMaxAmount=" + futureMaxAmount + ", fixAmount=" + fixAmount + ", bankCardType=" + bankCardType
				+ ", certificateType=" + certificateType + ", certificateNo=" + certificateNo + ", mobilePhone="
				+ mobilePhone + ", valid=" + valid + ", pin=" + pin + ", bankBranch=" + bankBranch + ", province="
				+ province + ", userId=" + userId + ", childMerchantId=" + childMerchantId + ", bindId=" + bindId
				+ ", reckonCurrency=" + reckonCurrency + ", fcCardNo=" + fcCardNo + ", userFee=" + userFee
				+ ", productCategory=" + productCategory + ", notify_url=" + notify_url + ", productDesc=" + productDesc
				+ ", oriReqMsgId=" + oriReqMsgId + ", validateCode=" + validateCode + ", mobileNo=" + mobileNo
				+ ", frontUrl=" + frontUrl + ", backUrl=" + backUrl + ", licenseNo=" + licenseNo + ", taxRegisterNo="
				+ taxRegisterNo + ", settingSettCard=" + settingSettCard + ", addrType=" + addrType + ", contactType="
				+ contactType + ", childEnter=" + childEnter + ", mcc=" + mcc + ", licenseType=" + licenseType
				+ ", authPayDir=" + authPayDir + ", scribeAppid=" + scribeAppid + ", contactMan=" + contactMan
				+ ", email=" + email + ", licenseBeginDate=" + licenseBeginDate + ", licenseEndDate=" + licenseEndDate
				+ ", licenseRange=" + licenseRange + ", merchNo=" + merchNo + ", balanceAmount=" + balanceAmount
				+ ", freezeAmount=" + freezeAmount + ", business_code=" + business_code + ", user_id=" + user_id
				+ ", DF_type=" + DF_type + ", account_no=" + account_no + ", e_user_code=" + e_user_code
				+ ", bank_code=" + bank_code + ", account_type=" + account_type + ", allot_flag=" + allot_flag
				+ ", bank_name=" + bank_name + ", bank_type=" + bank_type + ", account_prop=" + account_prop
				+ ", terminal_no=" + terminal_no + ", protocol=" + protocol + ", protocol_userid=" + protocol_userid
				+ ", id_type=" + id_type + ", ID=" + ID + ", tel=" + tel + ", extra_fee=" + extra_fee
				+ ", account_name=" + account_name + ", deviceType=" + deviceType + ", deviceId=" + deviceId
				+ ", userIP=" + userIP + ", bindValid=" + bindValid + ", Query_sn=" + Query_sn + ", Query_remark="
				+ Query_remark + ", reckonCurrent=" + reckonCurrent + ", beginCreateDate=" + beginCreateDate
				+ ", endCreateDate=" + endCreateDate + ", page=" + page + ", bindUrl=" + bindUrl + ", payStatus="
				+ payStatus + ", size=" + size + ", oriPayMsgId=" + oriPayMsgId + ", chnSerialNo=" + chnSerialNo
				+ ", categoryUnion=" + categoryUnion + ", inviteMerNo=" + inviteMerNo + ", subMerchantNo="
				+ subMerchantNo + "]";
	}

	
	
}
