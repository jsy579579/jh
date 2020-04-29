package com.jh.paymentgateway.controller.jf.dao;


import com.jh.paymentgateway.controller.jf.pojo.JFDEArea;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMcc;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMerchant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JFDEBusiness {

    // 查询城市对应的地区码
    public JFDEArea getAllByArea(String area);

    //查询所有的省份
    List<JFDEMerchant> getAllProvincial();

    //查询相应省份的市区

    public List<JFDEMerchant> getAllByProvincial(String provincial);

    //获取所有MCC码
    public List<JFDEMcc> getAllMcc();


    //更
    public JFDEArea getmccBycity(String city);

    //删除
    public JFDEMerchant deleteByUserAndPointIndecs(String city);
}
