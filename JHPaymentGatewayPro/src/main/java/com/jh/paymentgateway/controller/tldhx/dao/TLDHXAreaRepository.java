package com.jh.paymentgateway.controller.tldhx.dao;


import com.jh.paymentgateway.controller.tldhx.pojo.TLDHXArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLDHXAreaRepository extends JpaRepository<TLDHXArea,Long>, JpaSpecificationExecutor<TLDHXArea> {
    //查询城市对应的地区码
    @Query("SELECT me from TLDHXArea me where me.area=:area")
    public TLDHXArea getAllByArea(@Param("area") String area);


    @Query("select me from TLDHXArea me where me.area=:city")
    public TLDHXArea getmccBycity(@Param("city") String city);
}
