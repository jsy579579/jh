package com.jh.paymentgateway.controller.qysh.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "t_qysh_bankbranch")
public class QYSHBankBranch implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
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
