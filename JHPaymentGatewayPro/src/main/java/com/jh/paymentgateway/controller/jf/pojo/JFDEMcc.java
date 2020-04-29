package com.jh.paymentgateway.controller.jf.pojo;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_jf_mcc")
public class JFDEMcc implements Serializable {

    private static final long serialVersionUID = 415630068660003605L;

    @Id
    @Column(name = "id")
    private  long id;

    @Override
    public String toString() {
        return "JFDEMcc{" +
                "id=" + id +
                ", mccCode='" + mccCode + '\'' +
                ", mccName='" + mccName + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMccCode() {
        return mccCode;
    }

    public void setMccCode(String mccCode) {
        this.mccCode = mccCode;
    }

    public String getMccName() {
        return mccName;
    }

    public void setMccName(String mccName) {
        this.mccName = mccName;
    }

    @Column(name = "mcc_code")
    private String mccCode;

    @Column(name = "mcc_name")
    private String mccName;
}
