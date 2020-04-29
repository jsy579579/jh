package cn.jh.clearing.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class UserRealtion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7357876677250663875L;
	
	private Long id;
	private Long firstUserId;
	private String firstUserPhone;
	private Integer firstUserGrade = 0;
	private Integer realNameStatus = 0;
	private Long preUserId;
	private String preUserPhone;
	private Integer preUserGrade = 0;
	private Integer level = 0;
	private BigDecimal rate = BigDecimal.ONE;
	private String firstUserName;
	private String preUserName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getFirstUserId() {
		return firstUserId;
	}
	public void setFirstUserId(Long firstUserId) {
		this.firstUserId = firstUserId;
	}
	public String getFirstUserPhone() {
		return firstUserPhone;
	}
	public void setFirstUserPhone(String firstUserPhone) {
		this.firstUserPhone = firstUserPhone;
	}
	public Integer getFirstUserGrade() {
		return firstUserGrade;
	}
	public void setFirstUserGrade(Integer firstUserGrade) {
		this.firstUserGrade = firstUserGrade;
	}
	public Integer getRealNameStatus() {
		return realNameStatus;
	}
	public void setRealNameStatus(Integer realNameStatus) {
		this.realNameStatus = realNameStatus;
	}
	public Long getPreUserId() {
		return preUserId;
	}
	public void setPreUserId(Long preUserId) {
		this.preUserId = preUserId;
	}
	public String getPreUserPhone() {
		return preUserPhone;
	}
	public void setPreUserPhone(String preUserPhone) {
		this.preUserPhone = preUserPhone;
	}
	public Integer getPreUserGrade() {
		return preUserGrade;
	}
	public void setPreUserGrade(Integer preUserGrade) {
		this.preUserGrade = preUserGrade;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public String getFirstUserName() {
		return firstUserName;
	}
	public void setFirstUserName(String firstUserName) {
		this.firstUserName = firstUserName;
	}
	public String getPreUserName() {
		return preUserName;
	}
	public void setPreUserName(String preUserName) {
		this.preUserName = preUserName;
	}
	@Override
	public String toString() {
		return "UserRealtion [id=" + id + ", firstUserId=" + firstUserId + ", firstUserPhone=" + firstUserPhone
				+ ", firstUserGrade=" + firstUserGrade + ", realNameStatus=" + realNameStatus + ", preUserId="
				+ preUserId + ", preUserPhone=" + preUserPhone + ", preUserGrade=" + preUserGrade + ", level=" + level
				+ ", rate=" + rate + ", firstUserName=" + firstUserName + ", preUserName=" + preUserName + "]";
	}

	
	
}
