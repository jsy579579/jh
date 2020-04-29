package com.jh.paymentgateway.controller.tldhx.pojo;




import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "t_jf_merchant")
public class TLDHXMerchant implements Serializable {

    private static final long serialVersionUID = -217768553862033708L;

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "merchant")
    private String merchant;

    @Column(name = "provincial")
    private  String provincial;

    @Column(name = "city")
    private  String city;

    @Column(name = "code")
    String code;
    @Override
    public String toString() {
        return "TLDHXMerchant{" +
                "id=" + id +
                ", merchant='" + merchant + '\'' +
                ", provincial='" + provincial + '\'' +
                ", city='" + city + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getProvincial() {
        return provincial;
    }

    public void setProvincial(String provincial) {
        this.provincial = provincial;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
