package com.jh.user.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "t_news")
public class News implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private long id;

	@Column(name = "brand_id")
	private String brandId;
	
	//分类
	@Column(name = "classifi_cation")
	private String classifiCation;
	
	//标题
	@Column(name = "title")
	private String title;
	
	//内容
	@Column(name = "content")
	private String content;
	
	//预览图
	@Column(name = "low_source_id")
	private String lowSourceId;
	
	//预览图
	@Column(name = "low_source")
	private String lowSource;
	
	//开关
	@Column(name = "on_off")
	private String onOff;
	
	//摘要
	@Column(name = "remark")
	private String remark;
	
	//预览人数
	@Column(name = "preview_number")
	private String previewNumber;
	
	//发布人
	@Column(name = "publisher")
	private String publisher;
	
	//备用字段1
	@Column(name = "spare1")
	private String spare1;
	
	//备用字段2
	@Column(name = "spare2")
	private String spare2;
	
	//备用字段3
	@Column(name = "spare3")
	private String spare3;
	
	//更新时间
	@Column(name="update_time")
	private String updateTime;
	
	
	@Column(name="create_time")
	@JsonFormat(timezone = "GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLowSource() {
		return lowSource;
	}

	public void setLowSource(String lowSource) {
		this.lowSource = lowSource;
	}

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPreviewNumber() {
		return previewNumber;
	}

	public void setPreviewNumber(String previewNumber) {
		this.previewNumber = previewNumber;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getLowSourceId() {
		return lowSourceId;
	}

	public void setLowSourceId(String lowSourceId) {
		this.lowSourceId = lowSourceId;
	}

	public String getClassifiCation() {
		return classifiCation;
	}

	public void setClassifiCation(String classifiCation) {
		this.classifiCation = classifiCation;
	}

	public String getSpare1() {
		return spare1;
	}

	public void setSpare1(String spare1) {
		this.spare1 = spare1;
	}

	public String getSpare2() {
		return spare2;
	}

	public void setSpare2(String spare2) {
		this.spare2 = spare2;
	}

	public String getSpare3() {
		return spare3;
	}

	public void setSpare3(String spare3) {
		this.spare3 = spare3;
	}

	@Override
	public String toString() {
		return "News [id=" + id + ", brandId=" + brandId + ", classifiCation=" + classifiCation + ", title=" + title
				+ ", content=" + content + ", lowSourceId=" + lowSourceId + ", lowSource=" + lowSource + ", onOff="
				+ onOff + ", remark=" + remark + ", previewNumber=" + previewNumber + ", publisher=" + publisher
				+ ", spare1=" + spare1 + ", spare2=" + spare2 + ", spare3=" + spare3 + ", updateTime=" + updateTime
				+ ", createTime=" + createTime + "]";
	}

	

	
	
	

	
	
}
