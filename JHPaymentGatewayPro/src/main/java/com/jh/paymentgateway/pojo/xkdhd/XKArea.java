package com.jh.paymentgateway.pojo.xkdhd;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="t_xk_area")
public class XKArea  implements Serializable {
    @Id
    @Column(name="area_code")
    private String areaCode;

    @Column(name="province ")
    private String province;
    @Column(name="city ")
    private String city;

     @Column(name="parent_id ")
    private String parentId;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
