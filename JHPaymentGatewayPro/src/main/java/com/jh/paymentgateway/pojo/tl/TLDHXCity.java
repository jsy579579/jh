package com.jh.paymentgateway.pojo.tl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author keke
 * @date 2019/08/06
 * @description  通联代还城市编号实体类
 */
@Entity
@Table(name = "t_tldhx_city")
public class TLDHXCity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PKID")
    private long pkId;

    @Column(name = "CITY_NAME")
    private String cityName;

    @Column(name = "PID")
    private String pid;

    @Column(name = "SHOW_INDEX")
    private String showIndex;

    @Column(name = "NOTE")
    private String note;

    public long getPkId() {
        return pkId;
    }

    public void setPkId(long pkId) {
        this.pkId = pkId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getShowIndex() {
        return showIndex;
    }

    public void setShowIndex(String showIndex) {
        this.showIndex = showIndex;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "TLDHXCity{" +
                "pkId=" + pkId +
                ", cityName='" + cityName + '\'' +
                ", pid='" + pid + '\'' +
                ", showIndex='" + showIndex + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
