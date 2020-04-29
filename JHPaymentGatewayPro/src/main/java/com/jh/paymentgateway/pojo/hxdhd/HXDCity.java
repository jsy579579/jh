package com.jh.paymentgateway.pojo.hxdhd;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @title: HXDCity
 * @projectName: juhepayment
 * @description: TODO
 * @author: huhao
 * @date: 2019/9/23 11:44
 */
@Entity
@Table(name = "t_hxd_city")
public class HXDCity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "city_name")
    private String cityName;
    @Column(name = "pid")
    private int pid;

    public HXDCity(Integer id, String cityName, int pid) {
        this.id = id;
        this.cityName = cityName;
        this.pid = pid;
    }

    public HXDCity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "HXDCity{" +
                "id=" + id +
                ", cityName='" + cityName + '\'' +
                ", pid=" + pid +
                '}';
    }
}
