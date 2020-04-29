package com.jh.paymentgateway.pojo.apdh;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_ap_address")
public class APDHCityCode  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "city")
    private String city;

    @Column(name = "city_code")
    private String cityCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "APDHCityCode{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", cityCode='" + cityCode + '\'' +
                '}';
    }
}
