package com.jh.paymentgateway.controller.qysh.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.transaction.Transactional;
import java.io.Serializable;

@Entity
@Table(name = "t_qysh_merchant_copy1_copy1")
public class ABC implements Serializable {

    private static final long serialVersionUID = -8318662477782113592L;
    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;

    @Override
    public String toString() {
        return "ABC{" +
                "id=" + id +
                ", industryType='" + industryType + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
