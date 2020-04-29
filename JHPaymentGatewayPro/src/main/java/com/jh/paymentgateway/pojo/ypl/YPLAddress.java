package com.jh.paymentgateway.pojo.ypl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
@Entity
@Table(name="t_ypl_address")
public class YPLAddress implements Serializable {

     @Column(name="name")
    private String   name ;
     @Id
    @Column(name = "address_id")
    private String   addressId;
    @Column(name = "parent_id")
    private String parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
