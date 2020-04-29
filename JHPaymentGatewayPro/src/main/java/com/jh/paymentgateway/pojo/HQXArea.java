package com.jh.paymentgateway.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="t_hqx_area")
public class HQXArea implements Serializable {
    private static final long serialVersionUID = 3413947979067389449L;
   @Id
   @Column(name="REGION_ID")
   private long REGION_ID;

   @Column(name="REGION_CODE")
   private  String  REGION_CODE ;

   @Column(name="REGION_NAME")
   private  String  REGION_NAME ;

    @Column(name="PARENT_ID")
   private  String  PARENT_ID ;

    @Column(name="REGION_TYPE")
   private  String  REGION_TYPE ;

    public long getREGION_ID() {
        return REGION_ID;
    }

    public void setREGION_ID(long REGION_ID) {
        this.REGION_ID = REGION_ID;
    }

    public String getREGION_CODE() {
        return REGION_CODE;
    }

    public void setREGION_CODE(String REGION_CODE) {
        this.REGION_CODE = REGION_CODE;
    }

    public String getREGION_NAME() {
        return REGION_NAME;
    }

    public void setREGION_NAME(String REGION_NAME) {
        this.REGION_NAME = REGION_NAME;
    }

    public String getPARENT_ID() {
        return PARENT_ID;
    }

    public void setPARENT_ID(String PARENT_ID) {
        this.PARENT_ID = PARENT_ID;
    }

    public String getREGION_TYPE() {
        return REGION_TYPE;
    }

    public void setREGION_TYPE(String REGION_TYPE) {
        this.REGION_TYPE = REGION_TYPE;
    }
}
