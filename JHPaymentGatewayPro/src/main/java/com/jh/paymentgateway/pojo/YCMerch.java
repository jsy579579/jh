package com.jh.paymentgateway.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_yc_merchantno")
public class YCMerch implements Serializable {


	private static final long serialVersionUID = 6806427882012887136L;
	@Id
	@Column(name = "t_m_number")
	private String tMNnumber;

	@Column(name = "t_m_name")
	private String tMName;

	@Column(name = "area")
	private String area;

	public String gettMNnumber() {
		return tMNnumber;
	}

	public void settMNnumber(String tMNnumber) {
		this.tMNnumber = tMNnumber;
	}

	public String gettMName() {
		return tMName;
	}

	public void settMName(String tMName) {
		this.tMName = tMName;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
}
