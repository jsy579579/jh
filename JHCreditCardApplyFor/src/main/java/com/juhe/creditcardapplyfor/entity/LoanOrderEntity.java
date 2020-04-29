package com.juhe.creditcardapplyfor.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Administrator
 * @title: LoanOrderEntity
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002311:17
 */

@Entity
@Table(name="loan_order")
public class LoanOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //配置主键生成策略,自动增长
    @Column(name = "id")
    private Integer id;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "oem_channel_id")
    private Long oemChannelId;

    @Column(name = "client_no")
    private String clientNo;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "user_price")
    private BigDecimal userPrice;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "status")
    private String status;

    @Column(name = "trans_no")
    private String transNO;

    @Override
    public String toString() {
        return "LoanOrderEntity{" +
                "id=" + id +
                ", mobile='" + mobile + '\'' +
                ", oemChannelId=" + oemChannelId +
                ", clientNo='" + clientNo + '\'' +
                ", amount=" + amount +
                ", userPrice=" + userPrice +
                ", createTime='" + createTime + '\'' +
                ", status='" + status + '\'' +
                ", transNO='" + transNO + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getOemChannelId() {
        return oemChannelId;
    }

    public void setOemChannelId(Long oemChannelId) {
        this.oemChannelId = oemChannelId;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getUserPrice() {
        return userPrice;
    }

    public void setUserPrice(BigDecimal userPrice) {
        this.userPrice = userPrice;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransNO() {
        return transNO;
    }

    public void setTransNO(String transNO) {
        this.transNO = transNO;
    }
}
