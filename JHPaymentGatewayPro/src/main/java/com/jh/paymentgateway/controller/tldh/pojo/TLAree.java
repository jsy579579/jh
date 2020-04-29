package com.jh.paymentgateway.controller.tldh.pojo;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_tlt_aree")
public class TLAree implements Serializable {


    private static final long serialVersionUID = 8742877093221906901L;
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "province")
    private String province;

    @Column(name = "city_code")
    private String cityCode;

    @Column(name = "city")
    private String city;

    @Column(name = "county_code")
    private String countyCode;

    @Column(name = "cointy")
    private String cointy;

    @Override
    public String toString() {
        return "TLAree{" +
                "id=" + id +
                ", provinceCode='" + provinceCode + '\'' +
                ", province='" + province + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", city='" + city + '\'' +
                ", countyCode='" + countyCode + '\'' +
                ", cointy='" + cointy + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getCointy() {
        return cointy;
    }

    public void setCointy(String cointy) {
        this.cointy = cointy;
    }
}
