package com.jh.paymentgateway.pojo.kft;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhangchaofeng
 * @date 2019/5/8
 * @description 快付通城市代号
 */
@Table(name = "t_kft_address")
@Entity
public class KFTAddress implements Serializable {
    private static final long serialVersionUID = -3062733799840885381L;

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "code")
    private String code;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
