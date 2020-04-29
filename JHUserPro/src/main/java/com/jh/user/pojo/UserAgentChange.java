package com.jh.user.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

/**代理关系变动*/
@Entity
@Table(name="t_user_agent_change")
public class UserAgentChange implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private long id;
	
	@Column(name="user_id")
	private long userId;
	
	/**老的上级代理*/
	@Column(name="old_agent")
	private long oldAgent;
	
	/**新的上级代理*/
	@Column(name="new_agent")
	private long newAgent;
	
	@Column(name="remark")
	private String remark;
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getOldAgent() {
		return oldAgent;
	}

	public void setOldAgent(long oldAgent) {
		this.oldAgent = oldAgent;
	}

	public long getNewAgent() {
		return newAgent;
	}

	public void setNewAgent(long newAgent) {
		this.newAgent = newAgent;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
	
}
