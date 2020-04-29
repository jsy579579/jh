package com.cardmanager.pro.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

@Entity
@Table(name="t_consume_task",indexes= {
		@Index(columnList="consume_task_id",name="idx_consume_task_id",unique=true),
		@Index(columnList="user_id",name="idx_user_id"),
		@Index(columnList="brand_id",name="idx_brand_id"),
		@Index(columnList="repayment_task_id",name="idx_repayment_task_id"),
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
public class ConsumeTaskPOJO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2000830266118696831L;
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
	@Column(name="consume_task_id")
	private String consumeTaskId;
	@Column(name="order_code")
	private String orderCode = "0";
	@Column(name="amount",scale=2)
	private BigDecimal amount = BigDecimal.ZERO;
	@Column(name="real_amount",scale=2)
	private BigDecimal realAmount = BigDecimal.ZERO;
	@Column(name="service_charge",scale=2)
	private BigDecimal serviceCharge = BigDecimal.ZERO;
	@Column(name="channel_id")
	private String channelId;
	@Column(name="channel_tag")
	private String channelTag;
	@Column(name="description")
	private String description;
	@Column(name="task_type")
	private Integer taskType = Integer.valueOf(2);
	@Column(name="task_status")
	private Integer taskStatus = Integer.valueOf(0);
	@Column(name="order_status")
	private Integer orderStatus = Integer.valueOf(0);
	@Column(name="return_message",length=500)
	private String returnMessage;
	@Column(name="error_message",length=500)
	private String errorMessage;
	@Column(name="version")
	private String version = "1";
	@Column(name="execute_date")
	private String executeDate;
	@Column(name="execute_date_time")
	private String executeDateTime;
	@Column(name="create_time")
	private String createTime;
	@Transient
	private String versionName;
	@Transient
	private String userName;

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRepaymentTaskId() {
		return repaymentTaskId;
	}

	public void setRepaymentTaskId(String repaymentTaskId) {
		this.repaymentTaskId = repaymentTaskId;
	}

	public String getConsumeTaskId() {
		return consumeTaskId;
	}

	public void setConsumeTaskId(String consumeTaskId) {
		this.consumeTaskId = consumeTaskId;
	}

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

	public BigDecimal getRealAmount() {
		return realAmount;
	}

	public void setRealAmount(BigDecimal realAmount) {
		this.realAmount = realAmount;
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

	public BigDecimal getServiceCharge() {
		return serviceCharge;
	}

	public void setServiceCharge(BigDecimal serviceCharge) {
		this.serviceCharge = serviceCharge;
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

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	@Override
	public String toString() {
		return "ConsumeTaskPOJO [id=" + id + ", userId=" + userId + ", brandId=" + brandId + ", repaymentTaskId="
				+ repaymentTaskId + ", creditCardNumber=" + creditCardNumber + ", consumeTaskId=" + consumeTaskId
				+ ", orderCode=" + orderCode + ", amount=" + amount + ", realAmount=" + realAmount + ", serviceCharge="
				+ serviceCharge + ", channelId=" + channelId + ", channelTag=" + channelTag + ", description="
				+ description + ", taskType=" + taskType + ", taskStatus=" + taskStatus + ", orderStatus=" + orderStatus
				+ ", returnMessage=" + returnMessage + ", errorMessage=" + errorMessage + ", version=" + version
				+ ", executeDate=" + executeDate + ", executeDateTime=" + executeDateTime + ", createTime=" + createTime
				+ "]";
	}

}
