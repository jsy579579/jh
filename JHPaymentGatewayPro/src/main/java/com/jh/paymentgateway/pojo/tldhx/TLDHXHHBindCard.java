package com.jh.paymentgateway.pojo.tldhx;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_tldhxhh_bindcard")
public class TLDHXHHBindCard {
	private static final long serialVersionUID = 154L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private long id;

    @Column(name = "open_cardid")
    private String openCardid;

    @Override
    public String toString() {
        return "TLDHXHHBindCard{" +
                "id=" + id +
                ", openCardid='" + openCardid + '\'' +
                ", phone='" + phone + '\'' +
                ", bankCard='" + bankCard + '\'' +
                ", idCard='" + idCard + '\'' +
                ", status='" + status + '\'' +
                ", bindingNum='" + bindingNum + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public String getOpenCardid() {
        return openCardid;
    }

    public void setOpenCardid(String openCardid) {
        this.openCardid = openCardid;
    }

    @Column(name = "phone")
	private String phone;

	@Column(name = "bank_card")
	private String bankCard;

	@Column(name = "idcard")
	private String idCard;

	@Column(name = "status")
	private String status;

	@Column(name = "binding_num")
	private String bindingNum;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBindingNum() {
		return bindingNum;
	}

	public void setBindingNum(String bindingNum) {
		this.bindingNum = bindingNum;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "create_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
