package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_swiftpass_order")
public class SwiftpassOrder implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**表外键*/
	@Id
	@Column(name="id")
	private int id;
   
    /**第三方订单号*/
    @Column(name="tird_order_code")
    private String tirdOrderCode;
    
    /**公众账号ID*/
    @Column(name="openid")
    private String openid;
    
    /**第三方商户号*/
    @Column(name="third_mch_id")
    private String thirdMchId;
    
    /**商户号*/
    @Column(name="pre_mch_id")
    private String preMchId;
    
    /**子商户号*/
    @Column(name="mch_id")
    private String mchId;
    
    /**设备编号*/
    @Column(name="device_info")
    private String deviceInfo;
    
    /**平台订单号*/
    @Column(name="order_code")
    private String orderCode;
    
    /**商户订单号*/
    @Column(name="mch_order_code")
    private String mchOrderCode;
    
    /**用户标识*/
    @Column(name="sub_appid")
    private String subAppId;
    
    /**退款类型*/
    @Column(name="refund_type")
    private String refundType;
    
    /**交易类型*/
    @Column(name="service")
    private String service;
    
    /**交易状态*/
    @Column(name="status")
    private String status;
    
    /**付款银行*/
    @Column(name="bank_type")
    private String bankType;
    
    /**货币种类*/
    @Column(name="money_type")
    private String moneyType;
    
    /**总金额*/
    @Column(name="total_fee")
    private String totalFee;
    
    /**企业红包金额*/
    @Column(name="enterprise_red_envelope")
    private String entRedEnvelope;
    
    /**商户退款单号*/
    @Column(name="out_refund_no")
    private String outrefundNo;
    
    /**平台退款单号*/
    @Column(name="refund_id")
    private String refundId;
     
    /**退款金额*/
    @Column(name="refund_fee")
    private String refundFee;
    
    /**手续费*/
    @Column(name="extra_fee")
    private String extraFee;
    
    /**费率*/
    @Column(name="rate")
    private String rate;
    
    /**终端类型*/
    @Column(name="mch_create_type")
    private String mchCreateType;
    
    /**退款状态*/
    @Column(name="refund_status")
    private String refundStatus;
    
    /**商品名称*/
    @Column(name="mch_name")
    private String mchName;
    
    /**失手金额*/
    @Column(name="rela_feel")
    private String relaFeel;
    
    /**门店编号*/
    @Column(name="mch_num")
    private String mchNum;

    @Column(name="create_time")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id =id;
	}

	public String getTirdOrderCode() {
		return tirdOrderCode;
	}

	public void setTirdOrderCode(String tirdOrderCode) {
		this.tirdOrderCode = tirdOrderCode;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getThirdMchId() {
		return thirdMchId;
	}

	public void setThirdMchId(String thirdMchId) {
		this.thirdMchId = thirdMchId;
	}

	public String getPreMchId() {
		return preMchId;
	}

	public void setPreMchId(String preMchId) {
		this.preMchId = preMchId;
	}

	public String getMchId() {
		return mchId;
	}

	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getMchOrderCode() {
		return mchOrderCode;
	}

	public void setMchOrderCode(String mchOrderCode) {
		this.mchOrderCode = mchOrderCode;
	}

	public String getSubAppId() {
		return subAppId;
	}

	public void setSubAppId(String subAppId) {
		this.subAppId = subAppId;
	}

	public String getRefundType() {
		return refundType;
	}

	public void setRefundType(String refundType) {
		this.refundType = refundType;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBankType() {
		return bankType;
	}
	public void setBankType(String bankType) {
		this.bankType = bankType;
	}

	public String getMoneyType() {
		return moneyType;
	}

	public void setMoneyType(String moneyType) {
		this.moneyType = moneyType;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getEntRedEnvelope() {
		return entRedEnvelope;
	}

	public void setEntRedEnvelope(String entRedEnvelope) {
		this.entRedEnvelope = entRedEnvelope;
	}

	public String getOutrefundNo() {
		return outrefundNo;
	}

	public void setOutrefundNo(String outrefundNo) {
		this.outrefundNo = outrefundNo;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	public String getRefundFee() {
		return refundFee;
	}

	public void setRefundFee(String refundFee) {
		this.refundFee = refundFee;
	}

	public String getExtraFee() {
		return extraFee;
	}

	public void setExtraFee(String extraFee) {
		this.extraFee = extraFee;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getMchCreateType() {
		return mchCreateType;
	}

	public void setMchcreate_type(String mchCreateType) {
		this.mchCreateType = mchCreateType;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getMchName() {
		return mchName;
	}

	public void setMchName(String mchName) {
		this.mchName = mchName;
	}

	public String getRelaFeel() {
		return relaFeel;
	}

	public void setRelaFeel(String relaFeel) {
		this.relaFeel = relaFeel;
	}

	public String getMchNum() {
		return mchNum;
	}

	public void setMchNum(String mchNum) {
		this.mchNum = mchNum;
	}

	public void setMchCreateType(String mchCreateType) {
		this.mchCreateType = mchCreateType;
	}

	@Override
	public String toString() {
		return "SwiftpassOrder [id=" + id + ", tirdOrderCode=" + tirdOrderCode + ", openid=" + openid + ", thirdMchId="
				+ thirdMchId + ", preMchId=" + preMchId + ", mchId=" + mchId + ", deviceInfo=" + deviceInfo
				+ ", orderCode=" + orderCode + ", mchOrderCode=" + mchOrderCode + ", subAppId=" + subAppId
				+ ", refundType=" + refundType + ", service=" + service + ", status=" + status + ", bankType="
				+ bankType + ", moneyType=" + moneyType + ", totalFee=" + totalFee + ", entRedEnvelope="
				+ entRedEnvelope + ", outrefundNo=" + outrefundNo + ", refundId=" + refundId + ", refundFee="
				+ refundFee + ", extraFee=" + extraFee + ", rate=" + rate + ", mchCreateType=" + mchCreateType
				+ ", refundStatus=" + refundStatus + ", mchName=" + mchName + ", relaFeel=" + relaFeel + ", mchNum="
				+ mchNum + ", createTime=" + createTime + "]";
	}
  
}
