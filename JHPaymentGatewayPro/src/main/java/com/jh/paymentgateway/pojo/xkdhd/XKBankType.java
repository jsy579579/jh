package com.jh.paymentgateway.pojo.xkdhd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
@Table(name="t_xk_banktype")
@Entity
public class XKBankType implements Serializable {
    @Id
    @Column(name="bank_type")
    private String bankType;

    @Column(name="bank_name")
    private String bankName;

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
