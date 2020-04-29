package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="t_repayment_bill",indexes= {
		@Index(columnList="user_id",name="idx_user_id"),
		@Index(columnList="credit_card_number",name="idx_credit_card_number"),
		@Index(columnList="version",name="idx_version"),
		@Index(columnList="task_status",name="idx_task_status"),
		@Index(columnList="create_time",name="idx_create_time")
})
public class RepaymentBill implements Serializable {

	/**
	 * <p>Description: </p>
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name="user_id")
	private String userId;
	@Column(name="credit_card_number")
	private String creditCardNumber;
	@Column(name="version")
	private String version;
	@Column(name="rate",scale=4)
	private BigDecimal rate = BigDecimal.ZERO;
//	单笔手续费
	@Column(name="service_charge",scale=2)
	private BigDecimal serviceCharge = BigDecimal.ZERO;
//	总手续费
	@Column(name="total_service_charge",scale=2)
	private BigDecimal totalServiceCharge = BigDecimal.ZERO;
//	已支付手续费
	@Column(name="used_charge",scale=2)
	private BigDecimal usedCharge = BigDecimal.ZERO;
//	预留金额
	@Column(name="reserved_amount",scale=2)
	private BigDecimal reservedAmount = BigDecimal.ZERO;
//	还款金额
	@Column(name="task_amount",scale=2)
	private BigDecimal taskAmount = BigDecimal.ZERO;
//	还款笔数
	@Column(name="task_count")
	private int taskCount = 0;
//	消费成功的金额
	@Column(name="consumed_amount",scale=2)
	private BigDecimal consumedAmount = BigDecimal.ZERO;
//	还款成功金额
	@Column(name="repaymented_amount",scale=2)
	private BigDecimal repaymentedAmount = BigDecimal.ZERO;
//	还款执行笔数
	@Column(name="repaymented_count")
	private int repaymentedCount = 0;
//	还款成功笔数
	@Column(name="repaymented_success_count")
	private int repaymentedSuccessCount = 0;
//	0:未执行  1:执行完毕  2:执行中 3:已关闭  4:失败
	@Column(name="task_status")
	private int taskStatus = 0;
	@Column(name="create_time")
	private String createTime;
	@Transient
	private String orderCode;
	@Transient
	private int autoRepayment = 0;
	@Transient
	private String channelName;
	@Column(name="last_execute_date_time")
	private String lastExecuteDateTime;
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
	public String getCreditCardNumber() {
		return creditCardNumber;
	}
	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public BigDecimal getTaskAmount() {
		return taskAmount;
	}
	public void setTaskAmount(BigDecimal taskAmount) {
		this.taskAmount = taskAmount;
	}
	public int getTaskCount() {
		return taskCount;
	}
	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
	public BigDecimal getRepaymentedAmount() {
		return repaymentedAmount;
	}
	public void setRepaymentedAmount(BigDecimal repaymentedAmount) {
		this.repaymentedAmount = repaymentedAmount;
	}
	public int getRepaymentedCount() {
		return repaymentedCount;
	}
	public void setRepaymentedCount(int repaymentedCount) {
		this.repaymentedCount = repaymentedCount;
	}
	public int getRepaymentedSuccessCount() {
		return repaymentedSuccessCount;
	}
	public void setRepaymentedSuccessCount(int repaymentedSuccessCount) {
		this.repaymentedSuccessCount = repaymentedSuccessCount;
	}
	public int getTaskStatus() {
		return taskStatus;
	}
	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public BigDecimal getReservedAmount() {
		return reservedAmount;
	}
	public void setReservedAmount(BigDecimal reservedAmount) {
		this.reservedAmount = reservedAmount;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public BigDecimal getTotalServiceCharge() {
		return totalServiceCharge;
	}
	public void setTotalServiceCharge(BigDecimal totalServiceCharge) {
		this.totalServiceCharge = totalServiceCharge;
	}
	public BigDecimal getUsedCharge() {
		return usedCharge;
	}
	public void setUsedCharge(BigDecimal usedCharge) {
		this.usedCharge = usedCharge;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getLastExecuteDateTime() {
		return lastExecuteDateTime;
	}
	public void setLastExecuteDateTime(String lastExecuteDateTime) {
		this.lastExecuteDateTime = lastExecuteDateTime;
	}
	public int getAutoRepayment() {
		return autoRepayment;
	}
	public void setAutoRepayment(int autoRepayment) {
		this.autoRepayment = autoRepayment;
	}
	public BigDecimal getConsumedAmount() {
		return consumedAmount;
	}
	public void setConsumedAmount(BigDecimal consumedAmount) {
		this.consumedAmount = consumedAmount;
	}
	@Override
	public String toString() {
		return "RepaymentBill [id=" + id + ", userId=" + userId + ", creditCardNumber=" + creditCardNumber
				+ ", version=" + version + ", rate=" + rate + ", serviceCharge=" + serviceCharge
				+ ", totalServiceCharge=" + totalServiceCharge + ", usedCharge=" + usedCharge + ", reservedAmount="
				+ reservedAmount + ", taskAmount=" + taskAmount + ", taskCount=" + taskCount + ", consumedAmount="
				+ consumedAmount + ", repaymentedAmount=" + repaymentedAmount + ", repaymentedCount=" + repaymentedCount
				+ ", repaymentedSuccessCount=" + repaymentedSuccessCount + ", taskStatus=" + taskStatus
				+ ", createTime=" + createTime + ", orderCode=" + orderCode + ", autoRepayment=" + autoRepayment
				+ ", channelName=" + channelName + ", lastExecuteDateTime=" + lastExecuteDateTime + "]";
	}

}
