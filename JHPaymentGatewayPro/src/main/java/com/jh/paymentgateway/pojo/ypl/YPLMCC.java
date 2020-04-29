package com.jh.paymentgateway.pojo.ypl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="t_ypl_mcc")
public class YPLMCC implements Serializable {
     @Id
     @Column(name="mcc_id")
    private String mccId;

    @Column(name="name")
    private String name;

    @Column(name="parent")
    private String parent;

    public String getMccId() {
        return mccId;
    }

    public void setMccId(String mccId) {
        this.mccId = mccId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
