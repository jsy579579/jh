package com.jh.paymentgateway.pojo.apdh;


import javax.persistence.*;

@Entity
@Table(name = "t_ap_ips")
public class APDHIps {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start_ip")
    private String startIp;

    @Column(name = "end_ip")
    private String endIp;

    @Column(name = "province")
    private  String province;

    @Column(name = "city")
    private String city;

    @Column(name = "isp")
    private String isp;

    @Column(name = "start_ip1")
    private String startIp1;

    @Column(name = "end_ip1")
    private String endIp1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStartIp() {
        return startIp;
    }

    public void setStartIp(String startIp) {
        this.startIp = startIp;
    }

    public String getEndIp() {
        return endIp;
    }

    public void setEndIp(String endIp) {
        this.endIp = endIp;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getStartIp1() {
        return startIp1;
    }

    public void setStartIp1(String startIp1) {
        this.startIp1 = startIp1;
    }

    public String getEndIp1() {
        return endIp1;
    }

    public void setEndIp1(String endIp1) {
        this.endIp1 = endIp1;
    }
}
