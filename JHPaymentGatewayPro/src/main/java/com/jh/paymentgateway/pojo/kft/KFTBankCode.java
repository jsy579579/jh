package com.jh.paymentgateway.pojo.kft;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author zhangchaofeng
 * @date 2019/4/19
 * @description 快付通银行代号
 */
@Table(name = "t_bank_code")
@Entity
public class KFTBankCode implements Serializable {

    private static final long serialVersionUID = 1687414397874099499L;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
