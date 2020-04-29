package com.jh.paymentgateway.controller.ldd.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_ldd_area")
public class LDDArea implements Serializable {

    private static final long serialVersionUID = -4793711538046335021L;
    @Id
    @Column(name = "id")
    private long id;

    //省
    @Column(name = "province")
    private String province;
    //市
    @Column(name = "city")
    private String city;
    //地区码
    @Column(name = "city_code")
    private String cityCode;

    //商户类型
    @Column(name = "company_code")
    private String companyCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @Override
    public String toString() {
        return "LDDArea{" +
                "id=" + id +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
