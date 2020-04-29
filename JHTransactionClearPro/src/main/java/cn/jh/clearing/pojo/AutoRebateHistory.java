package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_auto_rebate_history")
public class AutoRebateHistory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9151974659506129575L;
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "rebate_config_id")
	private Long rebateConfigId;
	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRebateConfigId() {
		return rebateConfigId;
	}

	public void setRebateConfigId(Long rebateConfigId) {
		this.rebateConfigId = rebateConfigId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "AutoRebateHistory [id=" + id + ", userId=" + userId + ", rebateConfigId=" + rebateConfigId
				+ ", createTime=" + createTime + "]";
	}
}
