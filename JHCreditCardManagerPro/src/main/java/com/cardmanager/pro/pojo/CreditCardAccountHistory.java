package com.cardmanager.pro.pojo;

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

import com.fasterxml.jackson.annotation.JsonFormat;
@Entity
@Table(name="t_credit_card_account_history",indexes= {
		@Index(columnList="credit_card_account_id",name="idx_credit_card_account_id",unique=true),
		@Index(columnList="task_id",name="idx_task_id"),
		@Index(columnList="add_or_sub",name="idx_add_or_sub")
})
public class CreditCardAccountHistory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4224322799113213573L;
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	@Column(name="id")
	private Long id;
	@Column(name="credit_card_account_id")
	private Long creditCardAccountId;
	@Column(name="task_id")
	private String taskId;
	@Column(name="add_or_sub")
	private Integer addOrSub;
	@Column(name="amount",scale=2)
	private BigDecimal amount;
	@Column(name="sum_amount",scale=2)
	private BigDecimal sumBlance;
	@Column(name="description")
	private String description;
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime = new Date();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getSumBlance() {
		return sumBlance;
	}
	public void setSumBlance(BigDecimal sumBlance) {
		this.sumBlance = sumBlance;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Integer getAddOrSub() {
		return addOrSub;
	}
	public void setAddOrSub(Integer addOrSub) {
		this.addOrSub = addOrSub;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public Long getCreditCardAccountId() {
		return creditCardAccountId;
	}
	public void setCreditCardAccountId(Long creditCardAccountId) {
		this.creditCardAccountId = creditCardAccountId;
	}
	@Override
	public String toString() {
		return "CreditCardAccountHistory [id=" + id + ", creditCardAccountId=" + creditCardAccountId + ", taskId="
				+ taskId + ", addOrSub=" + addOrSub + ", amount=" + amount + ", sumBlance=" + sumBlance
				+ ", description=" + description + ", createTime=" + createTime + "]";
	}
	
	

}
