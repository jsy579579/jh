package com.jh.paymentgateway.controller.ld.pojo;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "t_ldquick_bankbranch")
public class LDBankBranch implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Column(name = "bankbranch_name")
    private String bankbranchName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBankbranchName() {
        return bankbranchName;
    }

    public void setBankbranchName(String bankbranchName) {
        this.bankbranchName = bankbranchName;
    }

    public String getBankbranchNo() {
        return bankbranchNo;
    }

    public void setBankbranchNo(String bankbranchNo) {
        this.bankbranchNo = bankbranchNo;
    }

    @Column(name = "bankbranch_no")
    private String bankbranchNo;
}
