package com.jh.paymentgateway.controller.xs.pojo;


import javax.persistence.*;
import java.io.Serializable;

@Table(name = "t_xsxe_address")
@Entity
public class TSXsxeAddress implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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


}
