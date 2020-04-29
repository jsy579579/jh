package com.jh.paymentgateway.controller.hqk.hqkUntil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(Include.NON_NULL)//1.将该标记放在属性上，如果该属性为NULL则不参与序列化  2.如果放在类上边,那对这个类的全部属性起作用
public class Trans implements Serializable {

	/**
	 * 序列化版本号
	 * 根据类名、接口名、成员方法及属性等来生成一个64位的哈希字段
	 */
	private static final long serialVersionUID = 5016944159848569429L;

	// 交易类型
	private String transcode;

	// 商户号
	private String merchno;
	
	//商户密钥
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
	
	
	private String licenseno;//	否	驾驶证号码（扣车主本人分时需要填写）
	private String licensePhone;//	否	此车辆在车管所登记的手机号码（扣车主本人分时需要填写）
	
	
	private String amount;//金额
	private String accountnum;//银行账户号
	private String accounttype;//账户类型 00 银行卡  01 存折
	private String accountname;//账户名称
	private String accountProperty;//账户属性 00 对私 01 对公
	
	private BigDecimal currAccountBalance;//当前账户余额
	private BigDecimal frozenamount;//冻结金额
	
	private String provinceCode;//省份编码
	private String cityCode;//城市编码
	
	
	private BigDecimal oriTransAmount;//原始交易金额
	private BigDecimal oriTransFee;//原始交易手续费
	private BigDecimal oriTransTotalAmount;//原始交易总金额
	
	private String linkorderid;//
	private String billtype;//
	
	
	private String requestUrl;//
	private String requestMethod;//
	private String requestCharset;//
	private String contextPath;//
	private Map<String, Object> parameterMap;//
	
	private String productName;//商户名称
	private String appid;//微信appid
	private String openid;//用户openid
	
	private String formDataInfo;//主装表单信息
	private String returnUrl;//
	private String notifyUrl;//
	private String status;//
	private String transtype;//
	private String currency;//
	private String paytime;//
	private String signType;//
	private String message;// 通道返回信息
	
	
	//润联data
	private String materialNo;//材料单号
	private String materialObj;//材料对象
	private String materialType;//材料类型
	private String materialNum;//材料序号
	private String contEmail;//联系人邮箱
	private String contName;//联系人姓名
	private String contPhone;//contPhone
	private String orderDate;//订单日期
	private String phoneNo;//银行卡预留手机号
	private String customerName;//持卡人姓名
	private String acctNo;//卡号
	private String cvn2;//CVN2
	private String expDate;//卡有效期
	private String terminalId;//商户机具终端编号
	private String accessObject;//接入对象
	private String signature;//验签字段
	private String commodityName;//商户名
	private String product;//产品信息
	

	
	//商旅快捷支付接口参数
	public String agentId;//代理商号
	public String merId;//商户号
	public String orderId;//订单号
	public String txnTime;//交易时间
	public String txnAmt;//交易金额
	public String settleAmt;//结算金额
	//下游收下游的费率
	public String txnRate;
	//下游收下游的单价
	public String siglePrice;
	//每笔返回给下游的费用
	public String backFee;
	public String realName;//真实姓名
	public String idCardNo;//身份证号
	public String bankCardNo;//银行卡号
	public String bankCode;//银行代码
	public String phone;//消费卡手机号
	public String expireDate;//过期日期
	public String withdrawUrl;//到账通知地址
	public String settleBankCode;//结算银行代码
	public String settleCardNo;//结算银行卡
	public String settlePhone;//结算手机号
	public String bankName;//消费卡银行名称
	public String settleCardBankName;//结算卡银行名称
	
	
	// KbPayData
	//请求ip
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

	private String operFlag;// 操作标识A.添加	M.修改
	private String t0drawFee;// 单笔D0提现交易手续费
	private String t0drawRate;// D0提现交易手续费扣率
	private String t1consFee;// 单笔消费交易手续费
	private String t1consRate;// 消费交易手续费扣率
	//卡属性 (B.对公C.对私)
	private String cardAttribute;
	/**
	 * CREDIT：贷记卡 DEBIT：借记卡(暂时只支持信用卡) DEPOSIT：存折 BUZACC：对公账户
	 * 账号类型
	 */
	private String accType;//账号类型
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
	
	//KbData end
	
	
	
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

	/*public RunLianProduct getProduct() {
		return product;
	}

	public void setProduct(RunLianProduct product) {
		this.product = product;
	}*/
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
	//商旅
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
	
	//KbData start
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

	//KbData end
	

}
