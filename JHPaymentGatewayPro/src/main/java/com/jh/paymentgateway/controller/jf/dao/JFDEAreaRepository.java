package com.jh.paymentgateway.controller.jf.dao;


import com.jh.paymentgateway.controller.jf.pojo.JFDEArea;
import com.jh.paymentgateway.controller.jf.pojo.JFDEMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JFDEAreaRepository extends JpaRepository<JFDEArea,Long>, JpaSpecificationExecutor<JFDEArea> {
    //查询城市对应的地区码
    @Query("SELECT me from JFDEArea me where me.area=:area")
    public JFDEArea getAllByArea(@Param("area")String area);


    @Query("select me from JFDEArea me where me.area=:city")
    public JFDEArea getmccBycity(@Param("city")String city);
}
