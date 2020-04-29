package com.jh.paymentgateway.pojo.hx;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "t_hxdh_address")
public class HXDHAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;

    @Column(name = "mcc")
    private String mcc;

    @Transient
    private Map<String, String> mccNamesAndCode;

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

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public Map<String, String> getMccNamesAndCode() {
        return mccNamesAndCode;
    }

    public void setMccNamesAndCode(Map<String, String> mccNamesAndCode) {
        this.mccNamesAndCode = mccNamesAndCode;
    }

    @Override
    public String toString() {
        return "HXDHAddress{" +
                "id=" + id +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", mcc='" + mcc + '\'' +
                ", mccNamesAndCode=" + mccNamesAndCode +
                '}';
    }
}
