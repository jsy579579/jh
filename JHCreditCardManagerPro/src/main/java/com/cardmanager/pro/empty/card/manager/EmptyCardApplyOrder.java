package com.cardmanager.pro.empty.card.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="t_empty_card_apply_order",indexes= {
		@Index(columnList="user_id",name="idx_user_id"),
		@Index(columnList="brand_id",name="idx_brand_id"),
		@Index(columnList="phone",name="idx_phone"),
		@Index(columnList="name",name="idx_name"),
		@Index(columnList="bank_name",name="idx_bank_name"),
		@Index(columnList="id_card",name="idx_id_card"),
		@Index(columnList="credit_card_number",name="idx_credit_card_number"),
		@Index(columnList="order_status",name="idx_order_status")
})
public class EmptyCardApplyOrder implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	@Column(name="user_id")
	private String userId;
//	缴纳手续费订单
	@Column(name="paycharge_order_code")
	private String paychargeOrderCode;
//	申请人手机号
	@Column(name="phone")
	private String phone;
//	申请人名称
	@Column(name="name")
	private String name;
//	申请银行名称
	@Column(name="bank_name")
	private String bankName;
//	身份证号
	@Column(name="id_card")
	private String idCard;
//	第二联系人手机号
	@Column(name="second_phone")
	private String secondPhone;
//	第二联系人姓名
	@Column(name="second_name")
	private String secondName;
//	贴牌号
	@Column(name="brand_id")
	private String brandId;
	@Column(name="version")
	private String version;
//	信用卡号
	@Column(name="credit_card_number")
	private String creditCardNumber;
//	执行日期
	@Column(name="execute_date")
	private String executeDate;
//	任务金额
	@Column(name="task_amount",scale=2)
	private BigDecimal taskAmount = BigDecimal.ZERO;
//	预留金额
	@Column(name="reserved_amount",scale=2)
	private BigDecimal reservedAmount = BigDecimal.ZERO;
//	总手续费
	@Column(name="total_service_charge",scale=2)
	private BigDecimal totalServiceCharge = BigDecimal.ZERO;
//	费率
	@Column(name="rate",scale=4)
	private BigDecimal rate = BigDecimal.ZERO;
//	单笔还款手续费
	@Column(name="service_charge",scale=2)
	private BigDecimal serviceCharge = BigDecimal.ZERO;
//	消费金额
	@Column(name="consumed_amount",scale=2)
	private BigDecimal consumedAmount = BigDecimal.ZERO;
//	还款金额
	@Column(name="repaymented_amount",scale=2)
	private BigDecimal repaymentedAmount = BigDecimal.ZERO;
//	未使用手续费
	@Column(name="residue_service_charge",scale=2)
	private BigDecimal residueServiceCharge = BigDecimal.ZERO;
//	欠款金额
	@Column(name="debt_amount",scale=2)
	private BigDecimal debtAmount = BigDecimal.ZERO;
//	每日还款笔数
	@Column(name="day_repayment_count")
	private int dayRepaymentCount = 0;
//	总还款笔数
	@Column(name="all_repayment_count")
	private int allRepaymentCount = 0;
	@Column(name="order_status")
//	0:待审核  1:审核通过,待缴手续费 2:已拒绝 3:已缴手续费,任务执行生成  4:执行完成  5:超时关闭 6:提前终止任务
	private int orderStatus = 0;
//	订单关闭时间
	@Column(name="close_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
	private Date closeTime = new Date(System.currentTimeMillis()+1*24*60*60*1000);
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
	private Date createTime = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getSecondPhone() {
		return secondPhone;
	}

	public void setSecondPhone(String secondPhone) {
		this.secondPhone = secondPhone;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}

	public BigDecimal getTaskAmount() {
		return taskAmount;
	}

	public void setTaskAmount(BigDecimal taskAmount) {
		this.taskAmount = taskAmount;
	}

	public BigDecimal getReservedAmount() {
		return reservedAmount;
	}

	public void setReservedAmount(BigDecimal reservedAmount) {
		this.reservedAmount = reservedAmount;
	}

	public BigDecimal getTotalServiceCharge() {
		return totalServiceCharge;
	}

	public void setTotalServiceCharge(BigDecimal totalServiceCharge) {
		this.totalServiceCharge = totalServiceCharge;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(BigDecimal serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public int getDayRepaymentCount() {
		return dayRepaymentCount;
	}

	public void setDayRepaymentCount(int dayRepaymentCount) {
		this.dayRepaymentCount = dayRepaymentCount;
	}

	public int getAllRepaymentCount() {
		return allRepaymentCount;
	}

	public void setAllRepaymentCount(int allRepaymentCount) {
		this.allRepaymentCount = allRepaymentCount;
	}

	public int getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public BigDecimal getConsumedAmount() {
		return consumedAmount;
	}

	public void setConsumedAmount(BigDecimal consumedAmount) {
		this.consumedAmount = consumedAmount;
	}

	public BigDecimal getResidueServiceCharge() {
		return residueServiceCharge;
	}

	public void setResidueServiceCharge(BigDecimal residueServiceCharge) {
		this.residueServiceCharge = residueServiceCharge;
	}

	public String getPaychargeOrderCode() {
		return paychargeOrderCode;
	}

	public void setPaychargeOrderCode(String paychargeOrderCode) {
		this.paychargeOrderCode = paychargeOrderCode;
	}

	public BigDecimal getRepaymentedAmount() {
		return repaymentedAmount;
	}

	public void setRepaymentedAmount(BigDecimal repaymentedAmount) {
		this.repaymentedAmount = repaymentedAmount;
	}

	public BigDecimal getDebtAmount() {
		return debtAmount;
	}

	public void setDebtAmount(BigDecimal debtAmount) {
		this.debtAmount = debtAmount;
	}
	
}
