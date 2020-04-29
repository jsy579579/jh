package com.jh.paymentgateway.controller.hc.pojo;


import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "t_hcdehk_merchant")
public class HCDEmerchant implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    //具体商户名称
    @Column(name = "adree")
    private String adree;
//消费类型
    @Column(name = "business_licence")
    private String businessLicence;
//省
    @Column(name = "company_name")
    private String companyName;

    //市
    @Column(name = "province")
    private String province;
    //区
    @Column(name = "city")
    private String city;
    //具体地址


    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "HCDEmerchant{" +
                "id=" + id +
                ", adree='" + adree + '\'' +
                ", businessLicence='" + businessLicence + '\'' +
                ", companyName='" + companyName + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAdree() {
        return adree;
    }

    public void setAdree(String adree) {
        this.adree = adree;
    }

    public String getBusinessLicence() {
        return businessLicence;
    }

    public void setBusinessLicence(String businessLicence) {
        this.businessLicence = businessLicence;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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
}
