package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KFTAddressRepository extends JpaRepository<KFTAddress,String> {

    @Query("select kftAddress.code from KFTAddress kftAddress where kftAddress.city=?1")
    String findCodeByProvinceAndCityName(String cityName);

}
