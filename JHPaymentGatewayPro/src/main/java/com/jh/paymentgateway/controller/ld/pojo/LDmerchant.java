package com.jh.paymentgateway.controller.ld.pojo;


import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "t_qysh_merchant")
public class LDmerchant implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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

    @Override
    public String toString() {
        return "QYSHbankbranch{" +
                "merabbreviation='" + merabbreviation + '\'' +
                ", industryType='" + industryType + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", detailsAddress='" + detailsAddress + '\'' +
                ", businesslicense='" + businesslicense + '\'' +
                ", legalName='" + legalName + '\'' +
                '}';
    }

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
}
