package com.juhe.creditcardapplyfor.bo;



import java.io.Serializable;

/**
 * @author huhao
 * @title: CardRecordBO
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/20 00209:20
 */


public class CardRecordBO implements Serializable {


    private static final long serialVersionUID = 1L;

    private String name;                    //申请人姓名
    private String mobile;                  //申请人手机号
    private String idCard;                  //申请人身份证
    private Long stationChannelId;          //站点通道id
    private Long stationBankCardChannelId;  //卡种通道id

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public Long getStationChannelId() {
        return stationChannelId;
    }

    public void setStationChannelId(Long stationChannelId) {
        this.stationChannelId = stationChannelId;
    }

    public Long getStationBankCardChannelId() {
        return stationBankCardChannelId;
    }

    public void setStationBankCardChannelId(Long stationBankCardChannelId) {
        this.stationBankCardChannelId = stationBankCardChannelId;
    }

    @Override
    public String toString() {
        return "CardRecordBO{" +
                "name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", idCard='" + idCard + '\'' +
                ", stationChannelId=" + stationChannelId +
                ", stationBankCardChannelId=" + stationBankCardChannelId +
                '}';
    }
}
