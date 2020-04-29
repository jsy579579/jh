package com.jh.paymentgateway.controller.tldh.pojo;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_hqt_bank_bin")
public class TLBankcode implements Serializable {

    private static final long serialVersionUID = -2262648605812751307L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;
    @Column(name = "bin")
    private String bin;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bank_code")
    private String bankCode;

    @Override
    public String toString() {
        return "TLBankcode{" +
                "id=" + id +
                ", bin='" + bin + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
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
}
