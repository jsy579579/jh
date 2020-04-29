package com.jh.paymentgateway.controller.hqk.hqkUntil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5016944159848569429L;

	private List<RepayPlanList> repayPlanList;
	private List<String> orderIds;
	private String merchantType;
	private String chanid;
	private String ordersn;
	private String txn;
	private String merchantName;
	private String cardNo;
	private String smsCode;
	private String dateTime;
	private String joinType;
	private String callBackUrl;
	private String oriReqMsgId;

	private String repayPlanJson;
	private String bankcardNumb;
	private String bankcardName;
	private String bankcardCode;
	private String userOrderId;
	private String tieCardId;
	private String channelType;
	private String provName;
	private String cityName;
	private String deviceId;
	private String repayModeFlag;
	private String handleType;
	private String futureRateType;

	private String tradeTime;
	private String transferTime;
	private String tradeMoney;
	private String transferMoney;
	private String rateMoney;
	private String repayOrderId;
	private String transferRepayOrderId;
	private String repayOrderFlag;

	public List<String> getOrderIds() {
		return orderIds;
	}

	public void setOrderIds(List<String> orderIds) {
		this.orderIds = orderIds;
	}

	public String getFutureRateType() {
		return futureRateType;
	}

	public void setFutureRateType(String futureRateType) {
		this.futureRateType = futureRateType;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public List<RepayPlanList> getRepayPlanList() {
		return repayPlanList;
	}

	public void setRepayPlanList(List<RepayPlanList> repayPlanList) {
		this.repayPlanList = repayPlanList;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getRepayPlanJson() {
		return repayPlanJson;
	}

	public void setRepayPlanJson(String repayPlanJson) {
		this.repayPlanJson = repayPlanJson;
	}

	public String getBankcardNumb() {
		return bankcardNumb;
	}

	public void setBankcardNumb(String bankcardNumb) {
		this.bankcardNumb = bankcardNumb;
	}

	public String getBankcardName() {
		return bankcardName;
	}

	public void setBankcardName(String bankcardName) {
		this.bankcardName = bankcardName;
	}

	public String getBankcardCode() {
		return bankcardCode;
	}

	public void setBankcardCode(String bankcardCode) {
		this.bankcardCode = bankcardCode;
	}

	public String getUserOrderId() {
		return userOrderId;
	}

	public void setUserOrderId(String userOrderId) {
		this.userOrderId = userOrderId;
	}

	public String getTieCardId() {
		return tieCardId;
	}

	public void setTieCardId(String tieCardId) {
		this.tieCardId = tieCardId;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getProvName() {
		return provName;
	}

	public void setProvName(String provName) {
		this.provName = provName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRepayModeFlag() {
		return repayModeFlag;
	}

	public void setRepayModeFlag(String repayModeFlag) {
		this.repayModeFlag = repayModeFlag;
	}

	public String getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}

	public String getTransferTime() {
		return transferTime;
	}

	public void setTransferTime(String transferTime) {
		this.transferTime = transferTime;
	}

	public String getTradeMoney() {
		return tradeMoney;
	}

	public void setTradeMoney(String tradeMoney) {
		this.tradeMoney = tradeMoney;
	}

	public String getTransferMoney() {
		return transferMoney;
	}

	public void setTransferMoney(String transferMoney) {
		this.transferMoney = transferMoney;
	}

	public String getRateMoney() {
		return rateMoney;
	}

	public void setRateMoney(String rateMoney) {
		this.rateMoney = rateMoney;
	}

	public String getRepayOrderId() {
		return repayOrderId;
	}

	public void setRepayOrderId(String repayOrderId) {
		this.repayOrderId = repayOrderId;
	}

	public String getTransferRepayOrderId() {
		return transferRepayOrderId;
	}

	public void setTransferRepayOrderId(String transferRepayOrderId) {
		this.transferRepayOrderId = transferRepayOrderId;
	}

	public String getRepayOrderFlag() {
		return repayOrderFlag;
	}

	public void setRepayOrderFlag(String repayOrderFlag) {
		this.repayOrderFlag = repayOrderFlag;
	}

	private String bindId;// 绑卡ID（签约编号）
	private String userId;//
	private String userIP;//
	private String mcc;//
	private String chantype;

	public String getUserIP() {
		return userIP;
	}

	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}

	@NotNull
	private String trade_time;

	@NotNull
	private String transfer_time;

	@NotNull
	private String trade_amount;

	@NotNull
	private String transfer_amount;

	private String repayPlanId;
	private String cooperator_item_id;
	private String cooperator_repay_order_id;
	// 交易类型
	private String transcode;
	private String ExpDate;
	// 商户号
	private String merchno;
	private String accountname;
	private String merchNo;
	private String validateCode;

	// 商户订单号
	private String dsorderid;

	private String createtime;

	// 注册号或统一信用代码
	private String regno;

	// 公司名称
	private String compayname;
	private String subMerchantNo;

	// 企业法人
	private String frname;

	// 备注
	private String remark;

	// 版本号
	private String version;

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
	// private String username;
	private String userName;

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
	private String accounttype;// 账户类型 00 银行卡 01 存折
	private String accountProperty;// 账户属性 00 对私 01 对公

	private String provinceCode;// 省份编码
	private String cityCode;// 城市编码

	private String productName;// 商户名称
	private String productDesc;// 商户描述
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

	// 商旅快捷支付接口参数
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
	public String settleBankCard;
	public String phone;
	public String expireDate;
	public String withdrawUrl;
	public String settleBankCode;
	public String settleCardNo;
	public String settlePhone;
	public String bankName;
	public String settleBankName;
	public String SettleCardBankName;

	public String name;
	public String pwd;
	public String type;
	public String methodname;
	public String username;

	// 商户号
	private String merchantId;
	// 订单号
	private String corp_flow_no;
	// 回显地址
	private String url_return;
	// 异步通知
	private String notify_url;
	// 下发快捷费率
	private String rate;
	// 固定手续费
	private String userFee;
	// 付款方银行卡号
	private String accountNo;
	// 付款方身份证号
	private String certificateNo;
	// 付款方手机号
	private String phoneNo;
	// 收款方银行卡号
	private String settleAccountNo;
	// 积分标识
	private String integralFlag;

	// 异步通知参数
	private String reqMsgId;
	private String respType;
	private String respCode;
	private String respMsg;
	private String notifyTime;

	// 支付宝
	private String service;
	private String agency_id;
	private String provider_id;
	private String trade_no;
	private Integer fee;
	private String expire_minutes;
	private String shop_id;
	private String counter_id;
	private String operator_id;
	private String desc;
	private String coupon_tag;
	private String nonce_str;

	private String bank_branch;
	private String bank_name;
	private String bank_province;
	private String bank_city;
	private String bank_card;
	private String card_user;
	private String user_type;
	private String card_type;
	private String account;
	private String refund_no;
	private Integer back;
	private String user_name;
	private String user_id;

	// 电信话费代付
	private String merid;
	// 3、 carrier运营商，1=电信，2=联通，3=移动，目前必须填 1
	private String carrier;
	private String timestamp;
	// 5、 paytype 支付方式，1=微信 ，2=支付宝
	private String paytype;

	// 公众号(原生)
	private String pay_type;
	private String merchant_id;
	private String mch_trade_id;
	private String mch_refund_id;
	private Integer refund_fee;
	private String body;
	private String sub_openid;
	private String sub_appid;
	private Integer total_fee;
	private String spbill_create_ip;
	private String callback_url;

	// 云付进件
	private String merchantNo;
	private String pointType;
	private String bankCard;
	private String idCard;
	private String provinceNo;
	private String cityNo;
	private String districtNo;
	private String address;
	private String feeRate;
	private String singleFee;

	// 智能付
	private String futureRateValue;
	private String futureMaxAmount;
	private String futureMinAmount;
	private String fixAmount;

	private String bank_card_id;
	private String longitude;
	private String latitude;
	private String cost;
	private String channel;
	private String channel_type;
	private String province_name;
	private String city_name;
	private String device_id;
	private String union_callback_url;
	// private String repayItemList;
	List<RepayItemVO> repayItemList;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBank_card_id() {
		return bank_card_id;
	}

	public void setBank_card_id(String bank_card_id) {
		this.bank_card_id = bank_card_id;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getChannel_type() {
		return channel_type;
	}

	public void setChannel_type(String channel_type) {
		this.channel_type = channel_type;
	}

	public String getProvince_name() {
		return province_name;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getUnion_callback_url() {
		return union_callback_url;
	}

	public void setUnion_callback_url(String union_callback_url) {
		this.union_callback_url = union_callback_url;
	}

	public String getRepay_mode() {
		return repay_mode;
	}

	public void setRepay_mode(String repay_mode) {
		this.repay_mode = repay_mode;
	}

	public List<RepayItemVO> getRepayItemList() {
		return repayItemList;
	}

	public void setRepayItemList(List<RepayItemVO> repayItemList) {
		this.repayItemList = repayItemList;
	}

	public String getBindId() {
		return bindId;
	}

	public void setBindId(String bindId) {
		this.bindId = bindId;
	}

	public String getChantype() {
		return chantype;
	}

	public void setChantype(String chantype) {
		this.chantype = chantype;
	}

	public String getTrade_time() {
		return trade_time;
	}

	public void setTrade_time(String trade_time) {
		this.trade_time = trade_time;
	}

	public String getTransfer_time() {
		return transfer_time;
	}

	public void setTransfer_time(String transfer_time) {
		this.transfer_time = transfer_time;
	}

	public String getTrade_amount() {
		return trade_amount;
	}

	public void setTrade_amount(String trade_amount) {
		this.trade_amount = trade_amount;
	}

	public String getTransfer_amount() {
		return transfer_amount;
	}

	public void setTransfer_amount(String transfer_amount) {
		this.transfer_amount = transfer_amount;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public String getCooperator_repay_order_id() {
		return cooperator_repay_order_id;
	}

	public void setCooperator_repay_order_id(String cooperator_repay_order_id) {
		this.cooperator_repay_order_id = cooperator_repay_order_id;
	}

	public String getRepayPlanId() {
		return repayPlanId;
	}

	public void setRepayPlanId(String repayPlanId) {
		this.repayPlanId = repayPlanId;
	}

	public String getCooperator_item_id() {
		return cooperator_item_id;
	}

	public void setCooperator_item_id(String cooperator_item_id) {
		this.cooperator_item_id = cooperator_item_id;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getPointType() {
		return pointType;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getProvinceNo() {
		return provinceNo;
	}

	public void setProvinceNo(String provinceNo) {
		this.provinceNo = provinceNo;
	}

	public String getCityNo() {
		return cityNo;
	}

	public void setCityNo(String cityNo) {
		this.cityNo = cityNo;
	}

	public String getDistrictNo() {
		return districtNo;
	}

	public void setDistrictNo(String districtNo) {
		this.districtNo = districtNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFeeRate() {
		return feeRate;
	}

	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}

	public String getSingleFee() {
		return singleFee;
	}

	public void setSingleFee(String singleFee) {
		this.singleFee = singleFee;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPay_type() {
		return pay_type;
	}

	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getMch_refund_id() {
		return mch_refund_id;
	}

	public void setMch_refund_id(String mch_refund_id) {
		this.mch_refund_id = mch_refund_id;
	}

	public Integer getRefund_fee() {
		return refund_fee;
	}

	public void setRefund_fee(Integer refund_fee) {
		this.refund_fee = refund_fee;
	}

	public String getMch_trade_id() {
		return mch_trade_id;
	}

	public void setMch_trade_id(String mch_trade_id) {
		this.mch_trade_id = mch_trade_id;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSub_openid() {
		return sub_openid;
	}

	public void setSub_openid(String sub_openid) {
		this.sub_openid = sub_openid;
	}

	public String getSub_appid() {
		return sub_appid;
	}

	public void setSub_appid(String sub_appid) {
		this.sub_appid = sub_appid;
	}

	public Integer getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(Integer total_fee) {
		this.total_fee = total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}

	public String getCallback_url() {
		return callback_url;
	}

	public void setCallback_url(String callback_url) {
		this.callback_url = callback_url;
	}

	public String getBank_branch() {
		return bank_branch;
	}

	public void setBank_branch(String bank_branch) {
		this.bank_branch = bank_branch;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRefund_no() {
		return refund_no;
	}

	public void setRefund_no(String refund_no) {
		this.refund_no = refund_no;
	}

	public Integer getBack() {
		return back;
	}

	public void setBack(Integer back) {
		this.back = back;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
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

	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
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

	public String getReqMsgId() {
		return reqMsgId;
	}

	public void setReqMsgId(String reqMsgId) {
		this.reqMsgId = reqMsgId;
	}

	public String getRespType() {
		return respType;
	}

	public void setRespType(String respType) {
		this.respType = respType;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getRespMsg() {
		return respMsg;
	}

	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}

	public String getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(String notifyTime) {
		this.notifyTime = notifyTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getCorp_flow_no() {
		return corp_flow_no;
	}

	public void setCorp_flow_no(String corp_flow_no) {
		this.corp_flow_no = corp_flow_no;
	}

	public String getUrl_return() {
		return url_return;
	}

	public void setUrl_return(String url_return) {
		this.url_return = url_return;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getUserFee() {
		return userFee;
	}

	public void setUserFee(String userFee) {
		this.userFee = userFee;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getCertificateNo() {
		return certificateNo;
	}

	public void setCertificateNo(String certificateNo) {
		this.certificateNo = certificateNo;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getSettleAccountNo() {
		return settleAccountNo;
	}

	public void setSettleAccountNo(String settleAccountNo) {
		this.settleAccountNo = settleAccountNo;
	}

	public String getIntegralFlag() {
		return integralFlag;
	}

	public void setIntegralFlag(String integralFlag) {
		this.integralFlag = integralFlag;
	}

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

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

	private String transtype;//
	private String currency;//
	private String paytime;//
	private String signType;//
	private String sign_type;//

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

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}

	public String getDsorderid() {
		return dsorderid;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
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

	public String getSubMerchantNo() {
		return subMerchantNo;
	}

	public void setSubMerchantNo(String subMerchantNo) {
		this.subMerchantNo = subMerchantNo;
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

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
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

	public String getSettleBankCard() {
		return settleBankCard;
	}

	public void setSettleBankCard(String settleBankCard) {
		this.settleBankCard = settleBankCard;
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

	public String getSettleBankName() {
		return settleBankName;
	}

	public void setSettleBankName(String settleBankName) {
		this.settleBankName = settleBankName;
	}

	public String getSettleCardBankName() {
		return SettleCardBankName;
	}

	public void setSettleCardBankName(String settleCardBankName) {
		SettleCardBankName = settleCardBankName;
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

	/**
	 * 卡属性 (B.对公C.对私)
	 */
	private String cardAttribute;
	/**
	 * CREDIT：贷记卡 DEBIT：借记卡(暂时只支持信用卡) DEPOSIT：存折 BUZACC：对公账户
	 *
	 * 账号类型
	 */
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
	private String province;
	private String city;
	private String deviceType;
	private String repayMode;
	private String repay_mode;
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
	private String status;

	// private String transStatus;

	private String sign;

	public String getRepayMode() {
		return repayMode;
	}

	public void setRepayMode(String repayMode) {
		this.repayMode = repayMode;
	}

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

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
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

	public String getMerchNo() {
		return merchNo;
	}

	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
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

	public String getMerid() {
		return merid;
	}

	public void setMerid(String merid) {
		this.merid = merid;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getPaytype() {
		return paytype;
	}

	public void setPaytype(String paytype) {
		this.paytype = paytype;
	}

	public String getFutureRateValue() {
		return futureRateValue;
	}

	public void setFutureRateValue(String futureRateValue) {
		this.futureRateValue = futureRateValue;
	}

	public String getFutureMaxAmount() {
		return futureMaxAmount;
	}

	public void setFutureMaxAmount(String futureMaxAmount) {
		this.futureMaxAmount = futureMaxAmount;
	}

	public String getFutureMinAmount() {
		return futureMinAmount;
	}

	public void setFutureMinAmount(String futureMinAmount) {
		this.futureMinAmount = futureMinAmount;
	}

	public String getFixAmount() {
		return fixAmount;
	}

	public void setFixAmount(String fixAmount) {
		this.fixAmount = fixAmount;
	}

	public String getOriReqMsgId() {
		return oriReqMsgId;
	}

	public void setOriReqMsgId(String oriReqMsgId) {
		this.oriReqMsgId = oriReqMsgId;
	}

	public String getHandleType() {
		return handleType;
	}

	public void setHandleType(String handleType) {
		this.handleType = handleType;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	public String getTxn() {
		return txn;
	}

	public void setTxn(String txn) {
		this.txn = txn;
	}

	public String getChanid() {
		return chanid;
	}

	public void setChanid(String chanid) {
		this.chanid = chanid;
	}
	
}
