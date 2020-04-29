package com.juhe.creditcardapplyfor.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author huhao
 * @title: ConversionEntity
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002319:10
 */

@Entity
@Table(name="conversion")
public class ConversionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //配置主键生成策略,自动增长
    @Column(name = "id")
    private Integer id;

    @Column(name = "oem_channel_id")
    private String oemChannelId;

    @Column(name = "channel_tag_id")
    private String channelTagId;
    @Column(name = "client_no")
    private String clientNo;

    @Column(name = "trans_no")
    private String transNo;

    @Column(name = "user_price")
    private BigDecimal userPrice;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "status")
    private String status;

    @Override
    public String toString() {
        return "ConversionEntity{" +
                "id=" + id +
                ", oemChannelId='" + oemChannelId + '\'' +
                ", channelTagId='" + channelTagId + '\'' +
                ", clientNo='" + clientNo + '\'' +
                ", transNo='" + transNo + '\'' +
                ", userPrice=" + userPrice +
                ", createTime='" + createTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOemChannelId() {
        return oemChannelId;
    }

    public void setOemChannelId(String oemChannelId) {
        this.oemChannelId = oemChannelId;
    }

    public String getChannelTagId() {
        return channelTagId;
    }

    public void setChannelTagId(String channelTagId) {
        this.channelTagId = channelTagId;
    }

    public String getClientNo() {
        return clientNo;
    }

    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    public String getTransNo() {
        return transNo;
    }

    public void setTransNo(String transNo) {
        this.transNo = transNo;
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
}
