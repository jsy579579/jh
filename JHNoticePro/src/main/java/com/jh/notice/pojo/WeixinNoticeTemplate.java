package com.jh.notice.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
@Table(name="t_weixin_notice_template")
public class WeixinNoticeTemplate implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="biz_type")
	private String bizType;
	
	@Column(name="template_id")
	private String templateId;
	
	@Column(name="template_content")
	private String templateContent;
	
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getBizType() {
		return bizType;
	}


	public void setBizType(String bizType) {
		this.bizType = bizType;
	}


	public String getTemplateId() {
		return templateId;
	}


	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}


	public String getTemplateContent() {
		return templateContent;
	}


	public void setTemplateContent(String templateContent) {
		this.templateContent = templateContent;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	
}
