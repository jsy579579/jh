package com.jh.paymentgateway.pojo;

import javax.persistence.*;

/**
 * @title: CJHKLRChannelWhite
 * @projectName: DDSH
 * @description: TODO
 * @author: huhao
 * @date: 2019/12/5 12:31
 */
@Entity(name = "t_cjhk_lr_channel_white")
public class CJHKLRChannelWhite {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "channel_type")
    private String channelType;

    @Column(name = "channel_code")
    private String channelCode;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private String createTime;

    @Override
    public String toString() {
        return "CJHKLRChannelWhite{" +
                "id=" + id +
                ", channelType='" + channelType + '\'' +
                ", channelCode='" + channelCode + '\'' +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
