package com.jh.paymentgateway.controller.qysh.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_hcdehk_merchant")
public class HCDE implements Serializable {

    private static final long serialVersionUID = 5924242294719174232L;
    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "adree")
    private String adree;

    @Column(name = "business_licence")
    private String business_licence;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private  String city;

    @Override
    public String toString() {
        return "HCDE{" +
                "id=" + id +
                ", adree='" + adree + '\'' +
                ", business_licence='" + business_licence + '\'' +
                ", companyName='" + companyName + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "status")
    private String status;

    public long getId() {
        return id;
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

    public String getBusiness_licence() {
        return business_licence;
    }

    public void setBusiness_licence(String business_licence) {
        this.business_licence = business_licence;
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
