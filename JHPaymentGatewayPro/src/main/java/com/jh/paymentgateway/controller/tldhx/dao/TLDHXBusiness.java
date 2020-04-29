package com.jh.paymentgateway.controller.tldhx.dao;


import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXArea;
import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMcc;
import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXMerchant;

import java.util.List;

public interface TLDHXBusiness {

    // 查询城市对应的地区码
    public TLDHXArea getAllByArea(String area);

    //查询所有的省份
    List<TLDHXMerchant> getAllProvincial();

    //查询相应省份的市区

    public List<TLDHXMerchant> getAllByProvincial(String provincial);

    //获取所有MCC码
    public List<TLDHXMcc> getAllMcc();


    //更
    public TLDHXArea getmccBycity(String city);

    //删除
    public TLDHXMerchant deleteByUserAndPointIndecs(String city);
}
