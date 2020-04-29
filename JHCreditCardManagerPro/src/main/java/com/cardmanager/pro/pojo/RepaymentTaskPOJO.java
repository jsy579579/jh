package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
@Entity
@Table(name="t_repayment_task",indexes= {
		@Index(columnList="repayment_task_id",name="idx_repayment_task_id",unique=true),
		@Index(columnList="user_id",name="idx_user_id"),
		@Index(columnList="brand_id",name="idx_brand_id"),
		@Index(columnList="credit_card_number",name="idx_credit_card_number"),
		@Index(columnList="order_code",name="idx_order_code"),
		@Index(columnList="task_type",name="idx_task_type"),
		@Index(columnList="task_status",name="idx_task_status"),
		@Index(columnList="order_status",name="idx_order_status"),
		@Index(columnList="execute_date",name="idx_execute_date"),
		@Index(columnList="execute_date_time",name="idx_execute_date_time"),
		@Index(columnList="create_time",name="idx_create_time"),
		@Index(columnList="version",name="idx_version"),
		@Index(columnList="task_type,task_status,execute_date_time,execute_date,version",name="idx_tteev")
})
public class RepaymentTaskPOJO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -225601194918776218L;
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name="user_id")
	private String userId;
	@Column(name="brand_id")
	private String brandId;
	@Column(name="repayment_task_id")
	private String repaymentTaskId;
	@Column(name="credit_card_number")
	private String creditCardNumber;
	@Column(name="order_code")
	private String orderCode = "0";
	@Column(name="amount",scale=2)
	private BigDecimal amount = BigDecimal.ZERO;
	@Column(name="real_amount",scale=2)
	private BigDecimal realAmount = BigDecimal.ZERO;
	@Column(name="rate",scale=4)
	private BigDecimal rate = BigDecimal.ZERO;
	@Column(name="service_charge",scale=2)
	private BigDecimal serviceCharge = BigDecimal.ZERO;
	@Column(name="total_service_charge",scale=2)
	private BigDecimal totalServiceCharge = BigDecimal.ZERO;
	@Column(name="return_service_charge",scale=2)
	private BigDecimal returnServiceCharge = BigDecimal.ZERO;
	@Column(name="channel_id")
	private String channelId;
	@Column(name="channel_tag")
	private String channelTag;
	@Column(name="execute_date")
	private String executeDate;
	@Column(name="execute_date_time")
	private String executeDateTime;
	@Column(name="description")
	private String description;
	@Column(name="return_message")
	private String returnMessage;
	@Column(name="error_message")
	private String errorMessage;
	@Column(name="task_type")
	private Integer taskType = Integer.valueOf(2);
	@Column(name="task_status")
	private Integer taskStatus = Integer.valueOf(0);
	@Column(name="order_status")
	private Integer orderStatus = Integer.valueOf(0);
	@Column(name="version")
	private String version = "1";
	@Transient
//	private ConsumeTaskVO[] consumeTaskVOs = new ConsumeTaskVO[index];
	private List<ConsumeTaskVO> consumeTaskVOs = new ArrayList<ConsumeTaskVO>();
	@Transient
	private String channelName = "";
	@Column(name="create_time")
	private String createTime;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getRepaymentTaskId() {
		return repaymentTaskId;
	}

	public void setRepaymentTaskId(String repaymentTaskId) {
		this.repaymentTaskId = repaymentTaskId;
	}

	public String getExecuteDateTime() {
		return executeDateTime;
	}

	public void setExecuteDateTime(String executeDateTime) {
		this.executeDateTime = executeDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(BigDecimal serviceCharge) {
		this.serviceCharge = serviceCharge;
	}

	public BigDecimal getTotalServiceCharge() {
		return totalServiceCharge;
	}

	public void setTotalServiceCharge(BigDecimal totalServiceCharge) {
		this.totalServiceCharge = totalServiceCharge;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Integer getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(Integer taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public BigDecimal getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(BigDecimal realAmount) {
		this.realAmount = realAmount;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}

	public Integer getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setConsumeTaskVOs(List<ConsumeTaskVO> consumeTaskVOs) {
		this.consumeTaskVOs = consumeTaskVOs;
	}

	public List<ConsumeTaskVO> getConsumeTaskVOs() {
		return consumeTaskVOs;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelTag() {
		return channelTag;
	}

	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}

	public BigDecimal getReturnServiceCharge() {
		return returnServiceCharge;
	}

	public void setReturnServiceCharge(BigDecimal returnServiceCharge) {
		this.returnServiceCharge = returnServiceCharge;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	@Override
	public String toString() {
		return "RepaymentTaskPOJO [id=" + id + ", userId=" + userId + ", brandId=" + brandId + ", repaymentTaskId="
				+ repaymentTaskId + ", creditCardNumber=" + creditCardNumber + ", orderCode=" + orderCode + ", amount="
				+ amount + ", realAmount=" + realAmount + ", rate=" + rate + ", serviceCharge=" + serviceCharge
				+ ", totalServiceCharge=" + totalServiceCharge + ", returnServiceCharge=" + returnServiceCharge
				+ ", channelId=" + channelId + ", channelTag=" + channelTag + ", executeDate=" + executeDate
				+ ", executeDateTime=" + executeDateTime + ", description=" + description + ", returnMessage="
				+ returnMessage + ", errorMessage=" + errorMessage + ", taskType=" + taskType + ", taskStatus="
				+ taskStatus + ", orderStatus=" + orderStatus + ", version=" + version + ", consumeTaskVOs="
				+ consumeTaskVOs + ", channelName=" + channelName + ", createTime=" + createTime + "]";
	}

	
	
}
