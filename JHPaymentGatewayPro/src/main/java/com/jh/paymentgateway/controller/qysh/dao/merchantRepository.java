package com.jh.paymentgateway.controller.qysh.dao;



import com.jh.paymentgateway.controller.qysh.pojo.merchant_copy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface merchantRepository extends JpaRepository<merchant_copy, Long>, JpaSpecificationExecutor<merchant_copy> {

    @Query("select me  from  merchant_copy me")
    public List<merchant_copy> getAll();

    @Query("select distinct me.province  from  merchant_copy me")
    public List<merchant_copy> getAllProvince();

    @Query("select distinct me.city  from  merchant_copy me where me.province=:province")
    public List<merchant_copy> getAllByCity(@Param("province")String province);

    @Query("select me from merchant_copy me where me.province=:province and me.city=:city")
    public List<merchant_copy> getAllByProvinceAndCity(@Param("province")String province,@Param("city")String city);

    @Query("select m from merchant_copy m where m.city=:city and m.county like %:county%")
    List<merchant_copy> getByCityAndCounty(@Param("city") String city, @Param("county") String county);

    @Query("select m from merchant_copy m where m.province=:province and m.county like %:county%")
    List<merchant_copy> getByProvinceAndCounty(@Param("province") String province, @Param("county") String county);

    @Query("select m from merchant_copy m where m.province=:province and m.city like %:city%")
    List<merchant_copy> getByProvinceAndCityLike(@Param("province") String province, @Param("city") String city);
}
