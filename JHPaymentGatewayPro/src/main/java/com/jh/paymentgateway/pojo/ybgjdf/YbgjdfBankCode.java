package com.jh.paymentgateway.pojo.ybgjdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_ybgjdf_bankcode")
public class YbgjdfBankCode implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_code")
    private String bankCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    @Override
    public String toString() {
        return "YbgjdfBankCode{" +
                "id=" + id +
                ", bankName='" + bankName + '\'' +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }
}
