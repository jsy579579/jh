package com.jh.paymentgateway.controller.tldhx.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "t_jf_area")
public class TLDHXArea implements Serializable {


    private static final long serialVersionUID = 6094999362945442778L;



    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "area_code")
    private String area_code;

    @Column(name = "area")
    private String area;

    @Column(name = "provincial_code")
    private String provincial_code;

    @Column(name = "level")
    private String level;
    @Override
    public String toString() {
        return "TLDHXArea{" +
                "id=" + id +
                ", area_code='" + area_code + '\'' +
                ", area='" + area + '\'' +
                ", provincial_code='" + provincial_code + '\'' +
                ", level='" + level + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getArea_code() {
        return area_code;
    }

    public void setArea_code(String area_code) {
        this.area_code = area_code;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getProvincial_code() {
        return provincial_code;
    }

    public void setProvincial_code(String provincial_code) {
        this.provincial_code = provincial_code;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}
