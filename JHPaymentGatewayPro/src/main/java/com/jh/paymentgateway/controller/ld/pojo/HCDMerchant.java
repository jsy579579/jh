package com.jh.paymentgateway.controller.ld.pojo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="t_international_company")
public class HCDMerchant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name="ID")
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="ADRESS")
    private String address;

    @Column(name="BUSINESS_LICENCE")
    private String businessLicence;

    @Column(name="province")
    private String province;

    @Column(name="city")
    private String city;

    @Column(name="COMPANY_NAME")
    private String companyName;

    @Column(name="COMPANY_SHORT_NAME")
    private String conmpanyShortName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBusinessLicence() {
        return businessLicence;
    }

    public void setBusinessLicence(String businessLicence) {
        this.businessLicence = businessLicence;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getConmpanyShortName() {
        return conmpanyShortName;
    }

    public void setConmpanyShortName(String conmpanyShortName) {
        this.conmpanyShortName = conmpanyShortName;
    }

    @Override
    public String toString() {
        return "HCDMerchant{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", businessLicence='" + businessLicence + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", companyName='" + companyName + '\'' +
                ", conmpanyShortName='" + conmpanyShortName + '\'' +
                '}';
    }
}
