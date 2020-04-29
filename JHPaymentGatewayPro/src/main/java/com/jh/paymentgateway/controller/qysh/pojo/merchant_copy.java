package com.jh.paymentgateway.controller.qysh.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_qysh_merchant")
public class merchant_copy implements Serializable {

    private static final long serialVersionUID = -4793711538046335021L;
    @Id
    @Column(name = "id")
    private long id;

    //具体商户名称
    @Column(name = "merabbreviation")
    private String merabbreviation;
    //消费类型
    @Column(name = "industry_type")
    private String industryType;
    //省
    @Column(name = "province")
    private String province;

    //市
    @Column(name = "city")
    private String city;
    //区
    @Column(name = "county")
    private String county;
    //具体地址
    @Column(name = "details_address")
    private String detailsAddress;
    //消费编码
    @Column(name = "businesslicense")
    private String businesslicense;

    @Column(name = "legal_name")
    private String legalName;
    public String getMerabbreviation() {
        return merabbreviation;
    }

    public void setMerabbreviation(String merabbreviation) {
        this.merabbreviation = merabbreviation;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getDetailsAddress() {
        return detailsAddress;
    }

    public void setDetailsAddress(String detailsAddress) {
        this.detailsAddress = detailsAddress;
    }

    public String getBusinesslicense() {
        return businesslicense;
    }

    public void setBusinesslicense(String businesslicense) {
        this.businesslicense = businesslicense;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "merchant_copy{" +
                "id=" + id +
                ", merabbreviation='" + merabbreviation + '\'' +
                ", industryType='" + industryType + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", detailsAddress='" + detailsAddress + '\'' +
                ", businesslicense='" + businesslicense + '\'' +
                ", legalName='" + legalName + '\'' +
                '}';
    }
}
